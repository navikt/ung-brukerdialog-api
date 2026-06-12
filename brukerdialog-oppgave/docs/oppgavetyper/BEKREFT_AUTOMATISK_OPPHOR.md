# BEKREFT_AUTOMATISK_OPPHOR

Bruker informeres om at ungdomsprogramytelsen opphører automatisk og får mulighet til å gi en kommentar innen 14 dager.

## Ytelse

Ungdomsytelse

## Trigger

Systemet oppdager at ytelsen vil opphøre automatisk siden maks antall dager på programmet er nådd. Oppgaven opprettes automatisk.

## Varsel til bruker

> Din ungdomsprogramytelse opphører automatisk. Du kan gi oss en kommentar innen 14 dager.

Lenken peker til oppgavesiden hos deltakerfrontenden.

## Data

Kontrakt: [`BekreftAutomatiskOpphorOppgavetypeDataDto`](../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/automatiskopphor/BekreftAutomatiskOpphorOppgavetypeDataDto.java)

| Felt | Type | Beskrivelse |
|------|------|-------------|
| `sluttdato` | `LocalDate` | Datoen ytelsen opphører |
| `maxDato` | `LocalDate` | Siste mulige dato deltaker kan kommentere innen |

## Implementasjon

| Klasse | Beskrivelse |
|--------|-------------|
| [`BekreftAutomatiskOpphorOppgavelInnholdUtleder`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/automatiskopphor/BekreftAutomatiskOpphorOppgavelInnholdUtleder.java) | Utleder varseltekst og lenke |
| [`BekreftAutomatiskOpphorOppgaveDataEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/automatiskopphor/BekreftAutomatiskOpphorOppgaveDataEntitet.java) | JPA-entitet for oppgavedata |
| [`BekreftAutomatiskOpphorOppgaveDataMapperFraDtoTilEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/automatiskopphor/BekreftAutomatiskOpphorOppgaveDataMapperFraDtoTilEntitet.java) | Mapper fra DTO til entitet |
| [`BekreftAutomatiskOpphorOppgaveDataMapperFraEntitetTilDto`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/automatiskopphor/BekreftAutomatiskOpphorOppgaveDataMapperFraEntitetTilDto.java) | Mapper fra entitet til DTO |
