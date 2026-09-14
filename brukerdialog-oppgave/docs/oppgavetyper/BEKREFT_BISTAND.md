# BEKREFT_BISTAND

Bruker kan uttale seg om at bistandsvilkåret ikke er oppfylt – at Nav ikke har et vedtak etter
navloven § 14 a om behov for bistand til å komme i arbeid.

## Ytelse

Aktivitetspenger

## Trigger

Saksbehandler foreslår avslag eller opphør av bistandsvilkåret, og ung-sak oppretter en
etterlysning av typen `UTTALELSE_BISTAND` som blir til denne oppgaven.

## Varsel til bruker

> Varsel om nye opplysninger – Behov for bistand

Lenken peker til oppgavesiden hos aktivitetspenger-innsyn.

## Data

Kontrakt: [`BekreftBistandOppgavetypeDataDto`](../../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/bistand/BekreftBistandOppgavetypeDataDto.java)

| Felt | Type | Beskrivelse |
|------|------|-------------|
| `fom` | `LocalDate` | Startdato for perioden det varsles om |
| `tom` | `LocalDate` | Sluttdato for perioden det varsles om |
| `ikkeOppfyltÅrsak` | `BistandsvilkårIkkeOppfyltÅrsak` | Årsak til at bistandsvilkåret tas opp til vurdering |
| `ikkeOppfyltÅrsakFritekstbeskrivelse` | `String` | Saksbehandlers fritekstbeskrivelse av årsaken |
| `kilde` | `BistandsavklaringKildeType` | Påkrevd. Hvor saksbehandler har fått opplysningene fra |
| `kildeFritekst` | `String` | Påkrevd når kilde = `ANNET` |

Opphørsvarianten [`BekreftBistandOpphørOppgavetypeDataDto`](../../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/bistand/BekreftBistandOpph%C3%B8rOppgavetypeDataDto.java)
har de samme feltene, men uten `tom`. Begge lagres i samme tabell; `tom is null` er det som
skiller opphør fra avslag.

Til forskjell fra bosted finnes ingen registerkilde for bistandsbehov, så
`BistandsavklaringKildeType` har kun `BRUKER` og `ANNET`.

## Implementasjon

| Klasse | Beskrivelse |
|--------|-------------|
| [`BekreftBistandOppgaveInnholdUtleder`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/bistand/BekreftBistandOppgaveInnholdUtleder.java) | Utleder varseltekst og lenke |
| [`BekreftBistandOppgaveDataEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/bistand/BekreftBistandOppgaveDataEntitet.java) | JPA-entitet for oppgavedata |
| [`BekreftBistandOppgaveDataMapperFraDtoTilEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/bistand/BekreftBistandOppgaveDataMapperFraDtoTilEntitet.java) | Mapper fra DTO til entitet |
| [`BekreftBistandOppgaveDataMapperFraEntitetTilDto`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/bistand/BekreftBistandOppgaveDataMapperFraEntitetTilDto.java) | Mapper fra entitet til DTO |
| [`bekreft_bistand.hbs`](../../pdf/src/main/resources/handlebars/tekstfragmenter/bistand/bekreft_bistand.hbs) | Tekstfragment for varsel og journalført dokument |
