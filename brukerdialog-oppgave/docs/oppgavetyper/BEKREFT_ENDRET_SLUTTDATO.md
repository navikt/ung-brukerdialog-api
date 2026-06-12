# BEKREFT_ENDRET_SLUTTDATO

Bruker bekrefter at sluttdatoen i ungdomsprogrammet er endret.

## Ytelse

Ungdomsytelse

## Trigger

Veileder på ungdomsprogrammet endrer sluttdatoen på programmet. Ung-sak kaller for å opprette oppgaven etter å ha mottatt en hendelse fra veileder-appen.

## Varsel til bruker

> Se og gi tilbakemelding på endret sluttdato i ungdomsprogrammet

Lenken peker til oppgavesiden hos deltakerfrontenden.

## Data

Kontrakt: [`EndretSluttdatoDataDto`](../../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/endretsluttdato/EndretSluttdatoDataDto.java)

| Felt | Type | Påkrevd | Beskrivelse |
|------|------|---------|-------------|
| `nySluttdato` | `LocalDate` | Ja | Den nye sluttdatoen |
| `forrigeSluttdato` | `LocalDate` | Nei | Sluttdatoen som ble erstattet (kan mangle hvis det ikke var satt en) |

## Implementasjon

| Klasse | Beskrivelse |
|--------|-------------|
| [`EndretSluttdatoOppgavelInnholdUtleder`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretsluttdato/EndretSluttdatoOppgavelInnholdUtleder.java) | Utleder varseltekst og lenke |
| [`EndretSluttdatoOppgaveDataEntitet`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretsluttdato/EndretSluttdatoOppgaveDataEntitet.java) | JPA-entitet for oppgavedata |
| [`EndretSluttdatoOppgaveDataMapperFraDtoTilEntitet`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretsluttdato/EndretSluttdatoOppgaveDataMapperFraDtoTilEntitet.java) | Mapper fra DTO til entitet |
| [`EndretSluttdatoOppgaveDataMapperFraEntitetTilDto`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretsluttdato/EndretSluttdatoOppgaveDataMapperFraEntitetTilDto.java) | Mapper fra entitet til DTO |
