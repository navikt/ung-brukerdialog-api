# BEKREFT_ENDRET_PERIODE

Bruker kan uttale seg om en endring i ungdomsprogrammet. Brukes når én hendelse medfører endring i både start- og sluttdato, fjerning av periode, eller andre endringer.

## Ytelse

Ungdomsytelse

## Trigger

Veileder på ungdomsprogrammet gjør en endring som påvirker hele perioden, f.eks. endrer både start og slutt samtidig, eller fjerner en periode. Ung-sak kaller for å opprette oppgaven etter å ha mottatt en hendelse fra veileder-appen.

## Varsel til bruker

> Se og gi tilbakemelding på endret periode i ungdomsprogrammet

Lenken peker til oppgavesiden hos deltakerfrontenden.

## Data

Kontrakt: [`EndretPeriodeDataDto`](../../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/endretperiode/EndretPeriodeDataDto.java)

| Felt | Type | Påkrevd | Beskrivelse |
|------|------|---------|-------------|
| `nyPeriode` | `PeriodeDTO` | Nei | Den nye perioden (fom + tom) |
| `forrigePeriode` | `PeriodeDTO` | Nei | Perioden som ble erstattet |
| `endringer` | `Set<PeriodeEndringType>` | Ja | Hvilke endringer som er gjort (maks 4) |

### Mulige endringer (`PeriodeEndringType`)

| Verdi | Beskrivelse |
|-------|-------------|
| `ENDRET_STARTDATO` | Startdatoen er endret |
| `ENDRET_SLUTTDATO` | Sluttdatoen er endret |
| `FJERNET_PERIODE` | Perioden er fjernet |
| `ANDRE_ENDRINGER` | Andre endringer som ikke passer de andre kategoriene |

## Implementasjon

| Klasse | Beskrivelse |
|--------|-------------|
| [`EndretPeriodeOppgavelInnholdUtleder`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretperiode/EndretPeriodeOppgavelInnholdUtleder.java) | Utleder varseltekst og lenke |
| [`EndretPeriodeOppgaveDataEntitet`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretperiode/EndretPeriodeOppgaveDataEntitet.java) | JPA-entitet for oppgavedata |
| [`EndretPeriodeOppgaveDataMapperFraDtoTilEntitet`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretperiode/EndretPeriodeOppgaveDataMapperFraDtoTilEntitet.java) | Mapper fra DTO til entitet |
| [`EndretPeriodeOppgaveDataMapperFraEntitetTilDto`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/endretperiode/EndretPeriodeOppgaveDataMapperFraEntitetTilDto.java) | Mapper fra entitet til DTO |
