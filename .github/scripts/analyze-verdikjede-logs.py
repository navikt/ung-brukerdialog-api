#!/usr/bin/env python3
"""Analyser verdikjede-testlogger.

Leser loggfiler fra LOG_DIR, finner alle JPS_XXXXXXX testsaker,
og genererer en kombinert, kronologisk loggfil per testcase i OUT_DIR.
"""

import json
import os
import re
import sys
from collections import defaultdict
from pathlib import Path

LOG_DIR = Path(os.environ.get("LOG_DIR", "ung-logs"))
OUT_DIR = Path(os.environ.get("OUT_DIR", "analysert"))

# Loggfiler som ikke inneholder relevante testdata
SKIP_FILES = {
    "kafka.log",
    "postgresk9verdikjede.log",
    "sif-abac-pdp.log",
    "create-kafka-topics.log",
}

SYSLOG_MONTHS = {
    "Jan": "01", "Feb": "02", "Mar": "03", "Apr": "04",
    "May": "05", "Jun": "06", "Jul": "07", "Aug": "08",
    "Sep": "09", "Oct": "10", "Nov": "11", "Dec": "12",
}

# GHA job log timestamp: "2026-03-10T15:19:23.3442573Z logs..."
GHA_TS_RE = re.compile(r"^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+Z) (.+)$")


def to_utc(ts: str) -> str:
    """Konverter +01:00 timestamp til UTC (Z-suffiks)."""
    m = re.match(r"(\d{4}-\d{2}-\d{2}T)(\d{2})(:)(\d{2}:\d{2})(.*)\+01:00$", ts)
    if m:
        prefix, h, colon, rest, frac = m.groups()
        return f"{prefix}{int(h) - 1:02d}{colon}{rest}{frac}Z"
    return ts


def detect_year(log_dir: Path) -> str:
    """Prøv å detektere årstall fra JSON-logger."""
    for path in log_dir.glob("*.log"):
        try:
            with open(path, errors="replace") as f:
                for raw in f:
                    raw = raw.strip()
                    if not raw.startswith("{"):
                        continue
                    try:
                        obj = json.loads(raw)
                        ts = obj.get("@timestamp", "")
                        m = re.match(r"^(\d{4})-", ts)
                        if m:
                            return m.group(1)
                    except (json.JSONDecodeError, KeyError):
                        pass
        except OSError:
            pass
    return str(__import__("datetime").date.today().year)


def parse_entries(log_dir: Path, year: str) -> list[tuple[str, str, str]]:
    """
    Les alle loggfiler og returner liste av (ts_utc, kilde, tekst).
    """
    entries = []

    for path in sorted(log_dir.glob("*.log")):
        if path.name in SKIP_FILES or path.name.startswith("buildx_"):
            continue

        source = path.stem

        try:
            with open(path, errors="replace") as f:
                raw_lines = f.readlines()
        except OSError as e:
            print(f"  Advarsel: kunne ikke lese {path.name}: {e}", file=sys.stderr)
            continue

        for raw in raw_lines:
            raw = raw.strip()
            if not raw:
                continue

            # GHA job log format: "2026-03-10T15:19:23.3442573Z rest..."
            m = GHA_TS_RE.match(raw)
            if m:
                ts, rest = m.groups()
                entries.append((ts, source, f"[{source}] {rest}"))
                continue

            # JSON-format
            if raw.startswith("{"):
                try:
                    obj = json.loads(raw)
                    ts = to_utc(obj.get("@timestamp", "unknown"))
                    msg = obj.get("message", raw)
                    logger = obj.get("logger_name", "")
                    short = logger.split(".")[-1] if logger else ""
                    tag = f" [{short}]" if short else ""
                    # Inkluder callId (inneholder JPS_XXXXXXX test-ID) i teksten
                    call_id = obj.get("callId", "")
                    call_tag = f" callId={call_id}" if call_id else ""
                    entries.append((ts, source, f"[{source}]{tag}{call_tag} {msg}"))
                    continue
                except (json.JSONDecodeError, KeyError):
                    pass

            # Fritekst: "YYYY-MM-DD HH:MM:SS,mmm ..."
            m = re.match(r"^(\d{4}-\d{2}-\d{2}) (\d{2}:\d{2}:\d{2})[,.](\d+) (.+)$", raw)
            if m:
                date, time_s, ms, rest = m.groups()
                ts = to_utc(f"{date}T{time_s}.{ms}+01:00")
                entries.append((ts, source, f"[{source}] {rest}"))
                continue

            # Syslog CEF: "<14>Mar 10 16:19:30 hostname appname: CEF:..."
            m = re.match(r"^<\d+>(\w{3}) +(\d+) (\d{2}:\d{2}:\d{2}) \S+ \S+: (CEF:.+)$", raw)
            if m:
                mon_str, day, time_s, cef = m.groups()
                mon = SYSLOG_MONTHS.get(mon_str, "01")
                ts = to_utc(f"{year}-{mon}-{int(day):02d}T{time_s}+01:00")
                entries.append((ts, source, f"[{source}] {cef}"))
                continue

    return entries


# Maven Surefire failure line (with JPS_ in method name, e.g. for parameterized/display names):
#   "ClassName.methodName__JPS_310161608  Time elapsed: ... <<< FAILURE!"
# OR without JPS_ (when surefire strips the suffix):
#   "ClassName.methodName -- Time elapsed: ... <<< FAILURE!"
FAILURE_WITH_JPS_RE = re.compile(r"JPS_(\d+).*(?:<<<\s*(?:FAILURE|ERROR)|FAILED)", re.IGNORECASE)
# Captures "ClassName.shortMethodName" from failure lines without JPS_
FAILURE_NO_JPS_RE = re.compile(
    r"\[ERROR\]\s+([\w$.]+\.[\w$]+(?:__[\w.$]+)?)\s+(?:--|Time elapsed).*<<<\s*(?:FAILURE|ERROR)",
    re.IGNORECASE,
)
JPS_IN_TEXT_RE = re.compile(r"JPS_(\d+)")


def find_failing_jps_ids(log_dir: Path) -> set[str]:
    """
    Les GHA job-loggene og finn JPS_XXXXXXX tester som eksplisitt feilet.

    To strategier:
    1. Direkte: JPS_-ID på samme linje som FAILURE/FAILED.
    2. Indirekte: hent klasse.metode fra failure-linje, søk så etter en linje
       som inneholder både det metodenavnet og ein JPS_-ID.

    Returnerer tom mengde hvis ingen feilindikatorer finnes.
    """
    failing: set[str] = set()
    failed_methods: set[str] = set()  # e.g. "EtterlysInntektrapporteringTest.inntekt_i_register_..."

    for path in sorted(log_dir.glob("gha-*.log")):
        try:
            lines = path.read_text(errors="replace").splitlines()
        except OSError:
            continue

        for line in lines:
            # Strategi 1: JPS_-ID er direkte på failure-linjen
            m = FAILURE_WITH_JPS_RE.search(line)
            if m:
                failing.add(m.group(1))
                continue

            # Strategi 2: samle opp metode-/klassenavn fra failure-linjer uten JPS_
            m = FAILURE_NO_JPS_RE.search(line)
            if m:
                # Hent bare siste to ledd: KlasseNavn.metodenavn
                parts = m.group(1).rsplit(".", 1)
                if len(parts) == 2:
                    failed_methods.add(parts[1])  # bare metodenavnet er unikt nok

    if failed_methods:
        # Andre pass: finn JPS_-IDer koblet til de feilende metodene
        for path in sorted(log_dir.glob("gha-*.log")):
            try:
                lines = path.read_text(errors="replace").splitlines()
            except OSError:
                continue
            for line in lines:
                if any(method in line for method in failed_methods):
                    for m in JPS_IN_TEXT_RE.finditer(line):
                        failing.add(m.group(1))

    return failing


def find_test_cases(entries: list[tuple[str, str, str]]) -> dict[str, set[str]]:
    """
    Finn alle JPS_XXXXXXX testsaker og tilknyttede saksnummere.
    Returns {journalpost_id: {saksnummer, ...}}
    """
    JPS_RE = re.compile(r"JPS_(\d+)")
    SNR_RE = re.compile(r"saksnummer[='\"\s:]+([A-Z]{2}[A-Za-z0-9]{2,8})\b")
    JP_RE = re.compile(r"(?:journalpostId|JournalpostId)[<='\"\s:]+(\d{7,})\b", re.IGNORECASE)

    jps_ids: set[str] = set()
    jps_to_snr: dict[str, set[str]] = defaultdict(set)
    jp_to_snr: dict[str, set[str]] = defaultdict(set)

    for _, _, text in entries:
        jps_matches = {m.group(1) for m in JPS_RE.finditer(text)}
        snr_matches = {m.group(1) for m in SNR_RE.finditer(text)}
        jp_matches = {m.group(1) for m in JP_RE.finditer(text)}

        jps_ids.update(jps_matches)

        for jps in jps_matches:
            jps_to_snr[jps].update(snr_matches)
        for jp in jp_matches:
            jp_to_snr[jp].update(snr_matches)

    # JPS ID = journalpost ID, så overfør saksnumre funnet via journalpostId-referanser
    for jps_id in jps_ids:
        if jps_id in jp_to_snr:
            jps_to_snr[jps_id].update(jp_to_snr[jps_id])

    return {jps: snrs for jps, snrs in jps_to_snr.items() if jps in jps_ids}


def write_case_file(
    jps_id: str,
    saksnummere: set[str],
    all_entries: list[tuple[str, str, str]],
) -> int:
    snr_label = "_".join(sorted(saksnummere)) if saksnummere else "ukjent"
    out_path = OUT_DIR / f"JPS_{jps_id}_{snr_label}.txt"

    terms = [f"JPS_{jps_id}"] + [re.escape(s) for s in saksnummere]
    pattern = re.compile("|".join(terms), re.IGNORECASE)

    matching = [(ts, src, text) for ts, src, text in all_entries if pattern.search(text)]
    matching.sort(key=lambda x: x[0])

    # Dedup audit-innslag (kloner av /behandlinger/alle o.l.)
    audit_seen: set[str] = set()
    deduped = []
    for ts, src, text in matching:
        if src == "audit.nais":
            req = re.search(r"request=([^ ]+)", text)
            act = re.search(r"act=([^ ]+)", text)
            key = f"{act.group(1) if act else '?'}:{req.group(1) if req else text[:40]}"
            if key in audit_seen:
                continue
            audit_seen.add(key)
        deduped.append((ts, src, text))

    with open(out_path, "w") as f:
        f.write(f"# Logganalyse: JPS_{jps_id} / saksnummer: {snr_label}\n")
        f.write(f"# Antall linjer: {len(deduped)}\n")
        f.write(f"# Kilder: {', '.join(sorted({s for _, s, _ in deduped}))}\n\n")
        for ts, _, text in deduped:
            f.write(f"{ts} {text}\n")

    print(f"  JPS_{jps_id} ({snr_label}): {len(deduped)} linjer -> {out_path.name}")
    return len(deduped)


def main() -> int:
    if not LOG_DIR.exists():
        print(f"Feil: loggmappen '{LOG_DIR}' finnes ikke", file=sys.stderr)
        return 1

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    print(f"Leser logger fra {LOG_DIR} ...")
    year = detect_year(LOG_DIR)
    print(f"  Detektert årstall: {year}")

    entries = parse_entries(LOG_DIR, year)
    print(f"  Totalt {len(entries)} logglinjer fra {len({s for _, s, _ in entries})} kilder")

    print("Finner feilende tester ...")
    failing_ids = find_failing_jps_ids(LOG_DIR)
    if failing_ids:
        print(f"  Feilende tester: {', '.join(f'JPS_{j}' for j in sorted(failing_ids))}")
    else:
        print("  Ingen eksplisitte feilindikatorer funnet i GHA-logger – analyserer alle testsaker")

    print("Finner alle testsaker ...")
    all_test_cases = find_test_cases(entries)

    if not all_test_cases:
        print("  Ingen JPS_XXXXXXX testsaker funnet i loggene")
        (OUT_DIR / "ingen-testsaker.txt").write_text("Ingen JPS-testsaker funnet i loggene.\n")
        return 0

    # Filtrer til kun feilende, eller alle hvis vi ikke fant noen feilindikatorer
    if failing_ids:
        test_cases = {jps: snrs for jps, snrs in all_test_cases.items() if jps in failing_ids}
        # Legg til feilende IDer som ikke ble funnet i loggene (kan ha minimalt data)
        for jps_id in failing_ids:
            if jps_id not in test_cases:
                test_cases[jps_id] = set()
        print(f"  Filtrerer til {len(test_cases)} feilende testcase(r) (av {len(all_test_cases)} totalt)")
    else:
        test_cases = all_test_cases
        print(f"  Analyserer alle {len(test_cases)} testcase(r)")

    print("\nGenererer analysefiler ...")
    total = 0
    for jps_id in sorted(test_cases):
        total += write_case_file(jps_id, test_cases[jps_id], entries)

    # Skriv sammendragsfil
    summary_path = OUT_DIR / "sammendrag.txt"
    with open(summary_path, "w") as f:
        f.write("# Sammendrag av verdikjede-logganalyse\n")
        if failing_ids:
            f.write(f"# Viser kun {len(test_cases)} feilende test(er)\n\n")
        else:
            f.write(f"# Viser alle {len(test_cases)} test(er) (ingen feilindikatorer funnet)\n\n")
        for jps_id in sorted(test_cases):
            snr = ", ".join(sorted(test_cases[jps_id])) or "ukjent"
            f.write(f"JPS_{jps_id}  saksnummer: {snr}\n")

    print(f"\nFerdig. {len(test_cases)} filer + sammendrag i {OUT_DIR}/")
    return 0


if __name__ == "__main__":
    sys.exit(main())
