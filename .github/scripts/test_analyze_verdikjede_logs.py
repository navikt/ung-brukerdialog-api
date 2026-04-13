#!/usr/bin/env python3
"""Tester for analyze-verdikjede-logs.py – write_case_file seed-pattern."""

import csv
import importlib.util
import tempfile
from pathlib import Path

import pytest

# analyze-verdikjede-logs.py har bindestrek i filnavn og kan ikke importeres direkte
_script_path = Path(__file__).parent / "analyze-verdikjede-logs.py"
_spec = importlib.util.spec_from_file_location("analyze_verdikjede_logs", _script_path)
script = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(script)

LogEntry = script.LogEntry
write_case_file = script.write_case_file


# ---------------------------------------------------------------------------
# Hjelpe-funksjoner
# ---------------------------------------------------------------------------

def _entry(text: str, ts: str = "2026-03-13T14:10:00.000Z", source: str = "gha-verdikjede-test") -> LogEntry:
    return LogEntry(ts=ts, source=source, loglevel="INFO", text=text, melding=text)


def _run_write(jps_id: str, test_name: str, entries: list[LogEntry],
               deltakelse_ids: set[str] | None = None) -> list[list[str]]:
    """Kaller write_case_file og returnerer CSV-radene (uten header)."""
    with tempfile.TemporaryDirectory() as tmp:
        script.OUT_DIR = Path(tmp)
        write_case_file(jps_id, test_name, entries, deltakelse_ids)
        files = list(Path(tmp).glob("*.csv"))
        assert len(files) == 1, f"Forventet 1 CSV-fil, fant {len(files)}"
        with open(files[0], newline="", encoding="utf-8") as f:
            rows = list(csv.reader(f))
        return rows[1:]  # dropp header


# ---------------------------------------------------------------------------
# Tester
# ---------------------------------------------------------------------------

METHOD_NAME = "inntekt_i_register_uten_rapportert_inntekt_trigger_ny_vurdering_av_inntektskontroll"
JPS_ID = "31315910"


class TestSeedPatternInkludererTestnavn:
    """Alle logginnslag som inneholder testmetodenavnet skal være med i CSV-en,
    uavhengig av JPS-suffiks."""

    def test_innslag_med_eksakt_jps_id_er_med(self):
        """Innslag med __JPS_31315910 skal alltid inkluderes."""
        entries = [
            _entry(f"[gha] TestCase-X.{METHOD_NAME}__JPS_{JPS_ID}, ] - Venter på at fagsak skal ha 2 behandlinger"),
        ]
        rows = _run_write(JPS_ID, METHOD_NAME, entries)
        assert len(rows) == 1

    def test_innslag_uten_jps_suffiks_er_med(self):
        """Innslag der testnavn mangler __JPS-suffiks skal inkluderes via testnavnet."""
        entries = [
            _entry(f"[gha] TestCase-X.{METHOD_NAME}, , ] - Venter på at fagsak skal ha 2 behandlinger"),
        ]
        rows = _run_write(JPS_ID, METHOD_NAME, entries)
        assert len(rows) == 1

    def test_innslag_med_annen_jps_id_er_med(self):
        """Innslag med samme testnavn men annen JPS-ID (annen journalpost) skal inkluderes."""
        annen_jps = "31315943"
        entries = [
            _entry(f"[gha] TestCase-X.{METHOD_NAME}__JPS_{annen_jps}, ] - Valgt sak/behandling"),
        ]
        rows = _run_write(JPS_ID, METHOD_NAME, entries)
        assert len(rows) == 1

    def test_alle_varianter_av_samme_testnavn_er_med(self):
        """Kombinasjon: med JPS, uten JPS, og annen JPS – alle skal være med."""
        annen_jps = "31315943"
        entries = [
            _entry(f"[gha] TestCase-X.{METHOD_NAME}__JPS_{JPS_ID}, ] - Venter på at fagsak skal ha 2 behandlinger",
                   ts="2026-03-13T14:10:34.000Z"),
            _entry(f"[gha] TestCase-X.{METHOD_NAME}, ] - Henter deltakelse",
                   ts="2026-03-13T14:10:35.000Z"),
            _entry(f"[gha] TestCase-X.{METHOD_NAME}__JPS_{annen_jps}, ] - Valgt sak/behandling",
                   ts="2026-03-13T14:10:36.000Z"),
        ]
        rows = _run_write(JPS_ID, METHOD_NAME, entries)
        assert len(rows) == 3

    def test_innslag_fra_annen_testmetode_er_ikke_med(self):
        """Innslag for en helt annen testmetode skal IKKE inkluderes."""
        annen_metode = "helt_annen_testmetode_som_ikke_skal_vaere_med"
        entries = [
            _entry(f"[gha] TestCase-X.{METHOD_NAME}__JPS_{JPS_ID}, ] - relevant"),
            _entry(f"[gha] TestCase-X.{annen_metode}__JPS_99999, ] - ikke relevant"),
        ]
        rows = _run_write(JPS_ID, METHOD_NAME, entries)
        assert len(rows) == 1
        assert "relevant" in rows[0][-1]

    def test_uten_testnavn_brukes_kun_jps_id(self):
        """Når test_name er tom streng, skal bare JPS-ID brukes som seed."""
        entries = [
            _entry(f"[gha] TestCase-X.{METHOD_NAME}__JPS_{JPS_ID}, ] - treffer jps"),
            _entry(f"[gha] TestCase-X.{METHOD_NAME}, ] - treffer ikke uten testnavn"),
        ]
        rows = _run_write(JPS_ID, "", entries)
        assert len(rows) == 1
        assert "treffer jps" in rows[0][-1]

    def test_deltakelse_id_kombinert_med_testnavn(self):
        """deltakelseId skal fortsatt fungere i kombinasjon med testnavn."""
        deltakelse_id = "42"
        entries = [
            _entry(f"[gha] TestCase-X.{METHOD_NAME}__JPS_{JPS_ID}, ] - via jps"),
            _entry(f"[gha] TestCase-X.{METHOD_NAME}, ] - via testnavn"),
            _entry(f"[ung-deltakelse-opplyser] deltakelseId={deltakelse_id} Henter data", source="ung-deltakelse-opplyser"),
            _entry(f"[ung-deltakelse-opplyser] irrelevant melding", source="ung-deltakelse-opplyser"),
        ]
        rows = _run_write(JPS_ID, METHOD_NAME, entries, deltakelse_ids={deltakelse_id})
        meldinger = [r[-1] for r in rows]
        assert any("via jps" in m for m in meldinger)
        assert any("via testnavn" in m for m in meldinger)
        assert any("deltakelseId" in m for m in meldinger)
        assert not any("irrelevant" in m for m in meldinger)
