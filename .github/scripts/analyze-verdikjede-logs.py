#!/usr/bin/env python3
"""Analyser verdikjede-testlogger.

Leser loggfiler fra LOG_DIR, finner alle JPS_XXXXXXX testsaker,
og genererer en kombinert, kronologisk loggfil per testcase i OUT_DIR.
"""

import csv
import json
import os
import re
import sys
from collections import defaultdict
from dataclasses import dataclass
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

# Log level extraction from plain text: "[INFO ]", "[ERROR]", "[WARN]" etc.
LOGLEVEL_RE = re.compile(r"\[(INFO|WARN(?:ING)?|ERROR|DEBUG|TRACE)\s*\]", re.IGNORECASE)
# prosesstask type: "prosessTaskType=TYPE" or "taskType=TYPE"
PROSESSTASK_RE = re.compile(r"\b(?:prosessTaskType|taskType)[=:\s]+([^\s,\]\[]+)", re.IGNORECASE)
# behandlingId: "behandlingId=12345" or "behandlingsId=12345"
BEHANDLING_ID_RE = re.compile(r"\bbehandling(?:s)?id[=:\s]+(\d+)", re.IGNORECASE)
# steg: "steg=STEG_X"
STEG_RE = re.compile(r"\bsteg[=:\s]+([^\s,\]\[]+)", re.IGNORECASE)
# Test name in log entries: "TestCase-...ClassName.methodName__JPS_id"
TESTCASE_RE = re.compile(r"TestCase-[\w.]+\.([\w_$]+?)__JPS_(\d+)")
# deltakelseId: "Opprettet ny ung deltagelse med id 12345"
DELTAKELSE_ID_RE = re.compile(r"Opprettet ny ung deltagelse med id[=:\s]+(\d+)", re.IGNORECASE)
# fagsakId: "fagsakId=12345" or "fagsakId: 12345"
FAGSAK_ID_RE = re.compile(r"\bfagsakId[=:\s]+(\d+)", re.IGNORECASE)
# saksnummer: "saksnummer=GS2024" etc.
SAKSNUMMER_RE = re.compile(r"\bsaksnummer[='\"\s:]+([A-Z]{2}[A-Za-z0-9]{2,8})\b")

# Tjenester der logginnslag kobles via saksnummer/fagsakId/behandlingId
LINKED_SERVICES = frozenset({"k9-abakus", "ung-sak", "ung-tilbake", "k9-oppdrag"})


@dataclass
class LogEntry:
    ts: str
    source: str
    loglevel: str
    text: str
    melding: str = ""
    prosesstask: str = ""
    behandling_id: str = ""
    steg: str = ""


def _extract_loglevel(text: str, json_obj: dict | None = None) -> str:
    if json_obj is not None:
        level = json_obj.get("level", json_obj.get("log_level", json_obj.get("log.level", "")))
        if level:
            return str(level).upper()
    m = LOGLEVEL_RE.search(text)
    return m.group(1).upper() if m else ""


def _extract_prosesstask(text: str, json_obj: dict | None = None) -> str:
    if json_obj is not None:
        for key in ("prosessTaskType", "taskType", "task_type"):
            val = json_obj.get(key, "")
            if val:
                return str(val)
        mdc = json_obj.get("mdc", {})
        if isinstance(mdc, dict):
            for key in ("prosessTaskType", "taskType"):
                val = mdc.get(key, "")
                if val:
                    return str(val)
    m = PROSESSTASK_RE.search(text)
    return m.group(1) if m else ""


def _extract_behandling_id(text: str, json_obj: dict | None = None) -> str:
    if json_obj is not None:
        for key in ("behandlingId", "behandlingsId", "behandling_id"):
            val = json_obj.get(key, "")
            if val:
                return str(val)
        mdc = json_obj.get("mdc", {})
        if isinstance(mdc, dict):
            for key in ("behandlingId", "behandlingsId"):
                val = mdc.get(key, "")
                if val:
                    return str(val)
    m = BEHANDLING_ID_RE.search(text)
    return m.group(1) if m else ""


def _extract_steg(text: str, json_obj: dict | None = None) -> str:
    if json_obj is not None:
        for key in ("steg", "prosessSteg"):
            val = json_obj.get(key, "")
            if val:
                return str(val)
        mdc = json_obj.get("mdc", {})
        if isinstance(mdc, dict):
            for key in ("steg", "prosessSteg"):
                val = mdc.get(key, "")
                if val:
                    return str(val)
    m = STEG_RE.search(text)
    return m.group(1) if m else ""


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


def parse_entries(log_dir: Path, year: str) -> list[LogEntry]:
    """
    Les alle loggfiler og returner liste av LogEntry.
    """
    entries = []

    for path in sorted(log_dir.glob("*.log")):
        if path.name in SKIP_FILES or path.name.startswith("buildx_"):
            continue

        # Skip VTP logs
        if "vtp" in path.stem.lower():
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
                entries.append(LogEntry(
                    ts=ts,
                    source=source,
                    loglevel=_extract_loglevel(rest),
                    text=f"[{source}] {rest}",
                    melding=rest,
                    prosesstask=_extract_prosesstask(rest),
                    behandling_id=_extract_behandling_id(rest),
                    steg=_extract_steg(rest),
                ))
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
                    text = f"[{source}]{tag}{call_tag} {msg}"
                    entries.append(LogEntry(
                        ts=ts,
                        source=source,
                        loglevel=_extract_loglevel(text, obj),
                        text=text,
                        melding=msg,
                        prosesstask=_extract_prosesstask(text, obj),
                        behandling_id=_extract_behandling_id(text, obj),
                        steg=_extract_steg(text, obj),
                    ))
                    continue
                except (json.JSONDecodeError, KeyError):
                    pass

            # Fritekst: "YYYY-MM-DD HH:MM:SS,mmm ..."
            m = re.match(r"^(\d{4}-\d{2}-\d{2}) (\d{2}:\d{2}:\d{2})[,.](\d+) (.+)$", raw)
            if m:
                date, time_s, ms, rest = m.groups()
                ts = to_utc(f"{date}T{time_s}.{ms}+01:00")
                text = f"[{source}] {rest}"
                entries.append(LogEntry(
                    ts=ts,
                    source=source,
                    loglevel=_extract_loglevel(rest),
                    text=text,
                    melding=rest,
                    prosesstask=_extract_prosesstask(rest),
                    behandling_id=_extract_behandling_id(rest),
                    steg=_extract_steg(rest),
                ))
                continue

            # Syslog CEF: "<14>Mar 10 16:19:30 hostname appname: CEF:..."
            m = re.match(r"^<\d+>(\w{3}) +(\d+) (\d{2}:\d{2}:\d{2}) \S+ \S+: (CEF:.+)$", raw)
            if m:
                mon_str, day, time_s, cef = m.groups()
                mon = SYSLOG_MONTHS.get(mon_str, "01")
                ts = to_utc(f"{year}-{mon}-{int(day):02d}T{time_s}+01:00")
                entries.append(LogEntry(
                    ts=ts,
                    source=source,
                    loglevel="",
                    text=f"[{source}] {cef}",
                    melding=cef,
                ))
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


def find_test_names(entries: list[LogEntry]) -> dict[str, str]:
    """
    Finn testmetodenavnet for hvert JPS_XXXXXXX ID.
    Leter etter mønster "TestCase-Klasse.metodenavn__JPS_id" i logglinjer.
    Returns {jps_id: test_method_name}
    """
    jps_to_name: dict[str, str] = {}
    for entry in entries:
        for m in TESTCASE_RE.finditer(entry.text):
            method_name, jps_id = m.group(1), m.group(2)
            if jps_id not in jps_to_name:
                jps_to_name[jps_id] = method_name
    return jps_to_name


def find_deltakelse_ids_for_jps(log_dir: Path) -> dict[str, set[str]]:
    """
    Les GHA-logger og finn deltakelseId for hvert JPS_id.

    Finn linjer som inneholder både testnavnet og "Opprettet ny ung deltagelse med id X".
    Første pass: bygg opp {testmetode → jps_id} fra TESTCASE_RE-mønsteret.
    Andre pass: finn deltakelseId fra linjer som inneholder testmetodenavnet.
    Returns {jps_id: {deltakelse_id, ...}}
    """
    jps_to_deltakelse: dict[str, set[str]] = defaultdict(set)

    all_lines: list[str] = []
    for path in sorted(log_dir.glob("gha-*.log")):
        try:
            all_lines.extend(path.read_text(errors="replace").splitlines())
        except OSError:
            continue

    # Første pass: bygg {test_method_name → {jps_id, ...}} fra TESTCASE_RE
    method_to_jps: dict[str, set[str]] = defaultdict(set)
    for line in all_lines:
        for m in TESTCASE_RE.finditer(line):
            method_name, jps_id = m.group(1), m.group(2)
            method_to_jps[method_name].add(jps_id)

    # Andre pass: finn deltakelse-linjer som inneholder testmetodenavnet eller JPS_id
    method_pattern = (
        re.compile("|".join(re.escape(name) for name in method_to_jps))
        if method_to_jps
        else None
    )
    for line in all_lines:
        m = DELTAKELSE_ID_RE.search(line)
        if not m:
            continue
        deltakelse_id = m.group(1)

        for jps_id in JPS_IN_TEXT_RE.findall(line):
            jps_to_deltakelse[jps_id].add(deltakelse_id)

        if method_pattern:
            for match in method_pattern.finditer(line):
                for jps_id in method_to_jps[match.group(0)]:
                    jps_to_deltakelse[jps_id].add(deltakelse_id)

    return dict(jps_to_deltakelse)


def find_test_cases(entries: list[LogEntry]) -> dict[str, set[str]]:
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

    for entry in entries:
        text = entry.text
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
    test_name: str,
    all_entries: list[LogEntry],
    deltakelse_ids: set[str] | None = None,
) -> int:
    if deltakelse_ids is None:
        deltakelse_ids = set()

    name_part = f"_{test_name}" if test_name else ""
    out_path = OUT_DIR / f"JPS_{jps_id}{name_part}.csv"

    # Bygg seed-pattern fra JPS-ID, testnavn og deltakelseId-er.
    # Testnavn inkluderes slik at alle logginnslag for testmetoden fanges opp,
    # uavhengig av om de har JPS-suffiks eller tilhører en annen journalpost.
    parts = [rf"JPS_{jps_id}\b"]
    if test_name:
        parts.append(re.escape(test_name))
    if deltakelse_ids:
        parts.extend(rf"\b{re.escape(did)}\b" for did in deltakelse_ids)
    seed_pattern = re.compile("|".join(parts))

    # Steg 1+2: finn innslag via JPS_id og deltakelseId (inkl. ung-deltakelse-opplyser)
    seed_indices: set[int] = set()
    for i, entry in enumerate(all_entries):
        if seed_pattern.search(entry.text):
            seed_indices.add(i)

    # Steg 3: hent saksnummer, fagsakId og behandlingId fra ung-sak-innslag
    saksnumre: set[str] = set()
    fagsak_ids: set[str] = set()
    behandling_ids: set[str] = set()
    for i in seed_indices:
        entry = all_entries[i]
        if "ung-sak" not in entry.source:
            continue
        for m in SAKSNUMMER_RE.finditer(entry.text):
            saksnumre.add(m.group(1))
        for m in FAGSAK_ID_RE.finditer(entry.text):
            fagsak_ids.add(m.group(1))
        if entry.behandling_id:
            behandling_ids.add(entry.behandling_id)
        for m in BEHANDLING_ID_RE.finditer(entry.text):
            behandling_ids.add(m.group(1))

    # Steg 4: knytt inn logginnslag fra tjenester via saksnummer/fagsakId/behandlingId
    link_ids = saksnumre | fagsak_ids | behandling_ids
    if link_ids:
        link_pattern = re.compile(
            "|".join(rf"\b{re.escape(lid)}\b" for lid in link_ids)
        )
        for i, entry in enumerate(all_entries):
            if i in seed_indices:
                continue
            if any(svc in entry.source for svc in LINKED_SERVICES) and link_pattern.search(entry.text):
                seed_indices.add(i)

    matching = sorted((all_entries[i] for i in seed_indices), key=lambda e: e.ts)

    with open(out_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["tidspunkt", "app", "loglevel", "prosesstask", "behandlingId", "steg", "melding"])
        for e in matching:
            writer.writerow([e.ts, e.source, e.loglevel, e.prosesstask, e.behandling_id, e.steg, e.melding])

    print(f"  JPS_{jps_id} ({test_name or 'ukjent'}): {len(matching)} linjer -> {out_path.name}")
    return len(matching)


def main() -> int:
    if not LOG_DIR.exists():
        print(f"Feil: loggmappen '{LOG_DIR}' finnes ikke", file=sys.stderr)
        return 1

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    print(f"Leser logger fra {LOG_DIR} ...")
    year = detect_year(LOG_DIR)
    print(f"  Detektert årstall: {year}")

    entries = parse_entries(LOG_DIR, year)
    print(f"  Totalt {len(entries)} logglinjer fra {len({e.source for e in entries})} kilder")

    print("Finner feilende tester ...")
    failing_ids = find_failing_jps_ids(LOG_DIR)
    if failing_ids:
        print(f"  Feilende tester: {', '.join(f'JPS_{j}' for j in sorted(failing_ids))}")
    else:
        print("  Ingen eksplisitte feilindikatorer funnet i GHA-logger – analyserer alle testsaker")

    print("Finner alle testsaker ...")
    all_test_cases = find_test_cases(entries)
    test_names = find_test_names(entries)

    print("Finner deltakelseId fra GHA-logger ...")
    jps_to_deltakelse = find_deltakelse_ids_for_jps(LOG_DIR)
    if jps_to_deltakelse:
        total_deltakelse = sum(len(v) for v in jps_to_deltakelse.values())
        print(f"  Fant {total_deltakelse} deltakelseId-kobling(er) for {len(jps_to_deltakelse)} testcase(r)")

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
        test_name = test_names.get(jps_id, "")
        total += write_case_file(jps_id, test_name, entries, jps_to_deltakelse.get(jps_id, set()))

    # Skriv sammendragsfil
    summary_path = OUT_DIR / "sammendrag.txt"
    with open(summary_path, "w") as f:
        f.write("# Sammendrag av verdikjede-logganalyse\n")
        if failing_ids:
            f.write(f"# Viser kun {len(test_cases)} feilende test(er)\n\n")
        else:
            f.write(f"# Viser alle {len(test_cases)} test(er) (ingen feilindikatorer funnet)\n\n")
        for jps_id in sorted(test_cases):
            test_name = test_names.get(jps_id, "ukjent")
            f.write(f"JPS_{jps_id}  testnavn: {test_name}\n")

    print(f"\nFerdig. {len(test_cases)} filer + sammendrag i {OUT_DIR}/")
    return 0


if __name__ == "__main__":
    sys.exit(main())
