# BEKREFT_ENDRET_STARTDATO

Bruker bekrefter at startdatoen i ungdomsprogrammet er endret.

## Ytelse

Ungdomsytelse

## Trigger

Veileder endrer startdatoen i ungdomsprogrammet. Ung-sak kaller for å opprette oppgaven etter å ha mottatt en hendelse fra veileder-appen.

## Varsel til bruker

> Se og gi tilbakemelding på endret startdato i ungdomsprogrammet

Lenken peker til oppgavesiden hos deltakerfrontenden.

## Data

Kontrakt: [`EndretStartdatoDataDto`](../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/endretstartdato/EndretStartdatoDataDto.java)

| Felt | Type | Beskrivelse |
|------|------|-------------|
| `nyStartdato` | `LocalDate` | Den nye startdatoen |
| `forrigeStartdato` | `LocalDate` | Startdatoen som ble erstattet |

## Implementasjon

| Klasse | Beskrivelse |
|--------|-------------|
| [`EndretStartdatoOppgavelInnholdUtleder`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretstartdato/EndretStartdatoOppgavelInnholdUtleder.java) | Utleder varseltekst og lenke |
| [`EndretStartdatoOppgaveDataEntitet`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretstartdato/EndretStartdatoOppgaveDataEntitet.java) | JPA-entitet for oppgavedata |
| [`EndretStartdatoOppgaveDataMapperFraDtoTilEntitet`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretstartdato/EndretStartdatoOppgaveDataMapperFraDtoTilEntitet.java) | Mapper fra DTO til entitet |
| [`EndretStartdatoOppgaveDataMapperFraEntitetTilDto`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretstartdato/EndretStartdatoOppgaveDataMapperFraEntitetTilDto.java) | Mapper fra entitet til DTO |
