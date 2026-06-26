# RAPPORTER_INNTEKT

Bruker rapporterer inntekt for en gitt periode. Dette er en periodisk oppgave som opprettes for hver rapporteringsperiode.

## Ytelse

Ungdomsytelse, Aktivitetspenger

## Trigger

Ung-sak oppretter oppgaven automatisk ved starten av en ny rapporteringsperiode (typisk månedlig). `kreverPeriode` er satt til `true`, som betyr at `fomDato` og `tomDato` må oppgis ved statusendring via `EndreOppgaveStatusDto`.

## Varsel til bruker

> Du har fått en oppgave om å registrere inntekten din for {måned} dersom du har det.

Lenken peker til oppgavesiden hos deltakerfrontenden (ungdomsytelse) eller aktivitetspenger-innsyn.

## Data

Kontrakt: [`InntektsrapporteringOppgavetypeDataDto`](../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/inntektsrapportering/InntektsrapporteringOppgavetypeDataDto.java)

| Felt | Type | Beskrivelse |
|------|------|-------------|
| `fraOgMed` | `LocalDate` | Startdato for rapporteringsperioden |
| `tilOgMed` | `LocalDate` | Sluttdato for rapporteringsperioden |
| `gjelderDelerAvMåned` | `Boolean` | Om perioden dekker hele eller deler av måneden |

## Implementasjon

| Klasse | Beskrivelse |
|--------|-------------|
| [`InntektsrapporteringOppgavelInnholdUtleder`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/oppgave/inntektsrapportering/InntektsrapporteringOppgavelInnholdUtleder.java) | Utleder varseltekst og lenke (per ytelse) |
| [`InntektsrapporteringOppgaveDataEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/oppgave/inntektsrapportering/InntektsrapporteringOppgaveDataEntitet.java) | JPA-entitet for oppgavedata |
| [`InntektsrapporteringOppgaveDataMapperFraDtoTilEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/oppgave/inntektsrapportering/InntektsrapporteringOppgaveDataMapperFraDtoTilEntitet.java) | Mapper fra DTO til entitet |
| [`InntektsrapporteringOppgaveDataMapperFraEntitetTilDto`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/oppgave/inntektsrapportering/InntektsrapporteringOppgaveDataMapperFraEntitetTilDto.java) | Mapper fra entitet til DTO |
