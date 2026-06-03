# BEKREFT_AVVIK_REGISTERINNTEKT

Bruker kan uttale seg om inntekt som er hentet fra register (Skatteetaten/A-ordningen) og eventuell ytelse. Brukes når registrert inntekt avviker fra det bruker har rapportert.

## Ytelse

Ungdomsytelse, Aktivitetspenger

## Trigger

Systemet oppdager avvik mellom rapportert inntekt og inntekt fra register for en gitt måned ved månedsvis kontroll av inntekt. Oppgaven opprettes automatisk for den aktuelle måneden.

## Varsel til bruker

> Du har fått en oppgave om å bekrefte inntekten din for {måned}

Lenken peker til oppgavesiden hos deltakerfrontenden (ungdomsytelse) eller aktivitetspenger-innsyn.

## Lovreferanse

- [§ 13 fjerde ledd i Arbeidsmarkedsloven](https://lovdata.no/dokument/NL/lov/2004-12-10-76)
- [§ 11 i Forskrift om forsøk med ungdomsprogram og ungdomsprogramytelse (gjelder fra 1. august 2025)](https://lovdata.no/dokument/LTI/forskrift/2025-06-20-1182)

## Data

Kontrakt: [`KontrollerRegisterinntektOppgavetypeDataDto`](../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/kontrollerregisterinntekt/KontrollerRegisterinntektOppgavetypeDataDto.java)

| Felt | Type | Beskrivelse |
|------|------|-------------|
| `fraOgMed` | `LocalDate` | Startdato for perioden inntekten gjelder |
| `tilOgMed` | `LocalDate` | Sluttdato for perioden inntekten gjelder |
| `registerinntekt` | `RegisterinntektDTO` | Inntektsdata hentet fra register |
| `gjelderDelerAvMåned` | `Boolean` | Om perioden dekker hele eller deler av måneden |

### `RegisterinntektDTO`

Inneholder feltene `arbeidOgFrilans` (`ArbeidOgFrilansRegisterInntektDTO`) og `ytelse` (`YtelseRegisterInntektDTO`).

## Implementasjon

| Klasse | Beskrivelse |
|--------|-------------|
| [`KontrollerRegisterinntektOppgavelInnholdUtleder`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/kontrollerregisterinntekt/KontrollerRegisterinntektOppgavelInnholdUtleder.java) | Utleder varseltekst og tittel |
| [`KontrollerRegisterinntektOppgaveDataEntitet`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/kontrollerregisterinntekt/KontrollerRegisterinntektOppgaveDataEntitet.java) | JPA-entitet for oppgavedata |
| [`KontrollerRegisterinntektOppgaveDataMapperFraDtoTilEntitet`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/kontrollerregisterinntekt/KontrollerRegisterinntektOppgaveDataMapperFraDtoTilEntitet.java) | Mapper fra DTO til entitet |
| [`KontrollerRegisterinntektOppgaveDataMapperFraEntitetTilDto`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/kontrollerregisterinntekt/KontrollerRegisterinntektOppgaveDataMapperFraEntitetTilDto.java) | Mapper fra entitet til DTO |
