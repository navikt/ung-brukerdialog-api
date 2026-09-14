# BEKREFT_AKTIVITET

Bruker kan uttale seg om at aktivitetsvilkåret ikke er oppfylt.

## Ytelse

Aktivitetspenger

## Trigger

Saksbehandler foreslår avslag eller opphør av aktivitetsvilkåret, og ung-sak oppretter en
etterlysning av typen `UTTALELSE_AKTIVITET` som blir til denne oppgaven.

## Varsel til bruker

> Varsel om nye opplysninger – Aktivitetsvilkåret

Lenken peker til oppgavesiden hos aktivitetspenger-innsyn.

## Data

Kontrakt: [`BekreftAktivitetOppgavetypeDataDto`](../../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/aktivitet/BekreftAktivitetOppgavetypeDataDto.java)

| Felt | Type | Beskrivelse |
|------|------|-------------|
| `fom` | `LocalDate` | Startdato for perioden det varsles om |
| `tom` | `LocalDate` | Sluttdato for perioden det varsles om |
| `ikkeOppfyltÅrsak` | `AktivitetsvilkåretIkkeOppfyltÅrsak` | Årsak til at aktivitetsvilkåret tas opp til vurdering |
| `ikkeOppfyltÅrsakFritekstbeskrivelse` | `String` | Saksbehandlers fritekstbeskrivelse av årsaken |
| `kilde` | `AktivitetsavklaringKildeType` | Påkrevd. Hvor saksbehandler har fått opplysningene fra |
| `kildeFritekst` | `String` | Påkrevd når kilde = `ANNET` |

Opphørsvarianten [`BekreftAktivitetOpphørOppgavetypeDataDto`](../../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/aktivitet/BekreftAktivitetOpph%C3%B8rOppgavetypeDataDto.java)
har de samme feltene, men uten `tom`. Begge lagres i samme tabell; `tom is null` er det som
skiller opphør fra avslag.

> **Foreløpig kodeverk.** Ung-sak har ennå ingen spesifikke årsaker for dette vilkåret –
> `AktivitetsvilkåretIkkeOppfyltÅrsak` har kun `ANNET`, der saksbehandler må navngi årsaken i
> fritekst. Varselteksten er tilsvarende generisk: «du oppfyller ikke aktivitetsvilkåret», og
> selve forklaringen kommer i «Årsak»-avsnittet. Når fag lander de faktiske årsakene må både
> ung-sak-enumet, kontraktkopien og [`bekreft_aktivitet.hbs`](../../pdf/src/main/resources/handlebars/tekstfragmenter/aktivitet/bekreft_aktivitet.hbs)
> utvides – slik andre livsoppholdsytelser har én setning per ytelse.
> `AVKORTET` finnes i ung-sak, men er utelatt fra kontrakten: den skal aldri varsles om.

Kildene er `BRUKER` (deltakeren selv), `NAV` (Navs egne systemer og registre) og `ANNET`
(fritekst).

## Implementasjon

| Klasse | Beskrivelse |
|--------|-------------|
| [`BekreftAktivitetOppgaveInnholdUtleder`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/aktivitet/BekreftAktivitetOppgaveInnholdUtleder.java) | Utleder varseltekst og lenke |
| [`BekreftAktivitetOppgaveDataEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/aktivitet/BekreftAktivitetOppgaveDataEntitet.java) | JPA-entitet for oppgavedata |
| [`BekreftAktivitetOppgaveDataMapperFraDtoTilEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/aktivitet/BekreftAktivitetOppgaveDataMapperFraDtoTilEntitet.java) | Mapper fra DTO til entitet |
| [`BekreftAktivitetOppgaveDataMapperFraEntitetTilDto`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/aktivitet/BekreftAktivitetOppgaveDataMapperFraEntitetTilDto.java) | Mapper fra entitet til DTO |
| [`bekreft_aktivitet.hbs`](../../pdf/src/main/resources/handlebars/tekstfragmenter/aktivitet/bekreft_aktivitet.hbs) | Tekstfragment for varsel og journalført dokument |
