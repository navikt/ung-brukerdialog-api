# SØK_YTELSE

Bruker oppfordres til å søke om ungdomsprogramytelsen.

## Ytelse

Ungdomsytelse

## Trigger

Ung-deltakelse-opplyser (veileder-appen) oppretter oppgaven når en deltaker registreres i ungdomsprogrammet.

## Varsel til bruker

> Søk om ungdomsprogramytelsen

Lenken peker til forsiden av deltakerfrontenden.

## Data

Kontrakt: [`SøkYtelseOppgavetypeDataDto`](../../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/søkytelse/SøkYtelseOppgavetypeDataDto.java)

| Felt | Type | Beskrivelse |
|------|------|-------------|
| `fomDato` | `LocalDate` | Fra og med dato for når ytelsen kan søkes |

## Implementasjon

| Klasse | Beskrivelse |
|--------|-------------|
| [`SøkYtelseOppgavelInnholdUtleder`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/oppgave/søkytelse/SøkYtelseOppgavelInnholdUtleder.java) | Utleder varseltekst og lenke |
| [`SøkYtelseOppgaveDataEntitet`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/oppgave/søkytelse/SøkYtelseOppgaveDataEntitet.java) | JPA-entitet for oppgavedata |
| [`SøkYtelseOppgaveDataMapperFraDtoTilEntitet`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/oppgave/søkytelse/SøkYtelseOppgaveDataMapperFraDtoTilEntitet.java) | Mapper fra DTO til entitet |
| [`SøkYtelseOppgaveDataMapperFraEntitetTilDto`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/oppgave/søkytelse/SøkYtelseOppgaveDataMapperFraEntitetTilDto.java) | Mapper fra entitet til DTO |
