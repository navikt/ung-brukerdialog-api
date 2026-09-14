# BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER

Bruker kan uttale seg om at vilkåret om andre livsoppholdsytelser ikke er oppfylt – at bruker
mottar en annen ytelse til livsopphold, og derfor ikke kan få aktivitetspenger for den samme
perioden.

## Ytelse

Aktivitetspenger

## Trigger

Saksbehandler foreslår avslag eller opphør av vilkåret om andre livsoppholdsytelser, og ung-sak
oppretter en etterlysning av typen `UTTALELSE_ANDRE_LIVSOPPHOLDSYTELSER` som blir til denne
oppgaven.

## Varsel til bruker

> Varsel om nye opplysninger – Andre ytelser til livsopphold

Lenken peker til oppgavesiden hos aktivitetspenger-innsyn.

## Data

Kontrakt: [`BekreftAndreLivsoppholdsytelserOppgavetypeDataDto`](../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/livsopphold/BekreftAndreLivsoppholdsytelserOppgavetypeDataDto.java)

| Felt | Type | Beskrivelse |
|------|------|-------------|
| `fom` | `LocalDate` | Startdato for perioden det varsles om |
| `tom` | `LocalDate` | Sluttdato for perioden det varsles om |
| `ikkeOppfyltÅrsak` | `AndreLivsoppholdsytelserIkkeOppfyltÅrsak` | Hvilken ytelse bruker mottar |
| `ikkeOppfyltÅrsakFritekstbeskrivelse` | `String` | Saksbehandlers fritekstbeskrivelse av årsaken |
| `kilde` | `AndreLivsoppholdsytelserAvklaringKildeType` | Påkrevd. Hvor saksbehandler har fått opplysningene fra |
| `kildeFritekst` | `String` | Påkrevd når kilde = `ANNET` |

Opphørsvarianten [`BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto`](../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/livsopphold/BekreftAndreLivsoppholdsytelserOpph%C3%B8rOppgavetypeDataDto.java)
har de samme feltene, men uten `tom`. Begge lagres i samme tabell; `tom is null` er det som
skiller opphør fra avslag.

`AndreLivsoppholdsytelserIkkeOppfyltÅrsak` navngir ytelsen i selve koden – `MOTTAR_DAGPENGER`,
`MOTTAR_UFØRETRYGD` og så videre – slik at varselet kan si hvilken ytelse det gjelder uten
fritekst. `MOTTAR_ANNEN_YTELSE` er den generiske koden, og der navngis ytelsen i fritekst.
`AVKORTET` finnes i ung-sak, men er utelatt fra kontrakten: den skal aldri varsles om.

Kildene er `BRUKER` (deltakeren selv), `NAV` (Navs egne systemer og registre) og `ANNET`
(fritekst) – til forskjell fra bistand, der det ikke finnes noen registerkilde.

## Implementasjon

| Klasse | Beskrivelse |
|--------|-------------|
| [`BekreftAndreLivsoppholdsytelserOppgaveInnholdUtleder`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/livsopphold/BekreftAndreLivsoppholdsytelserOppgaveInnholdUtleder.java) | Utleder varseltekst og lenke |
| [`BekreftAndreLivsoppholdsytelserOppgaveDataEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/livsopphold/BekreftAndreLivsoppholdsytelserOppgaveDataEntitet.java) | JPA-entitet for oppgavedata |
| [`BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraDtoTilEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/livsopphold/BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraDtoTilEntitet.java) | Mapper fra DTO til entitet |
| [`BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraEntitetTilDto`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/livsopphold/BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraEntitetTilDto.java) | Mapper fra entitet til DTO |
| [`bekreft_andre_livsoppholdsytelser.hbs`](../../pdf/src/main/resources/handlebars/tekstfragmenter/livsopphold/bekreft_andre_livsoppholdsytelser.hbs) | Tekstfragment for varsel og journalført dokument |
