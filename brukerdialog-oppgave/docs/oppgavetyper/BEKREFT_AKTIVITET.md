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

Kontrakt: [`BekreftAktivitetOppgavetypeDataDto`](../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/aktivitet/BekreftAktivitetOppgavetypeDataDto.java)

| Felt | Type | Beskrivelse |
|------|------|-------------|
| `fom` | `LocalDate` | Startdato for perioden det varsles om |
| `tom` | `LocalDate` | Sluttdato for perioden det varsles om |
| `ikkeOppfyltÅrsak` | `AktivitetsvilkåretIkkeOppfyltÅrsak` | Årsak til at aktivitetsvilkåret tas opp til vurdering |
| `ikkeOppfyltÅrsakFritekstbeskrivelse` | `String` | Saksbehandlers fritekstbeskrivelse av årsaken |
| `kilde` | `AktivitetsavklaringKildeType` | Påkrevd. Hvor saksbehandler har fått opplysningene fra |
| `kildeFritekst` | `String` | Påkrevd når kilde = `ANNET` |

Opphørsvarianten [`BekreftAktivitetOpphørOppgavetypeDataDto`](../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/aktivitet/BekreftAktivitetOpph%C3%B8rOppgavetypeDataDto.java)
har de samme feltene, men uten `tom`. Begge lagres i samme tabell; `tom is null` er det som
skiller opphør fra avslag.

Hovedsetningen i varselet avhenger av årsaken (eksempel for opphør):

| Årsak | Hovedsetning |
|-------|--------------|
| `FRAVÆR_FRA_PLANLAGT_AKTIVITET` | «Vi har fått opplysninger om at du fra DATO ikke lenger deltar i aktivitetene som er planlagt for deg. Du må følge aktivitetsplanen din for å få aktivitetspenger.» |
| `AVSTÅR_FRA_TILBUD_OM_ARBEID_UTEN_RIMELIG_GRUNN` | «Vi har fått opplysninger om at du DATO ikke startet i en jobb som du kunne ha startet i. Du kan miste retten til aktivitetspenger fra DATO siden du ikke startet i denne jobben.» |
| `ANNET`, `UDEFINERT` | Generisk «du er ikke i aktivitet»; forklaringen kommer i «Årsak»-avsnittet |

Ved avslått periode brukes «i perioden FOM til TOM» i stedet for «fra DATO».
`AVKORTET` finnes i ung-sak, men er utelatt fra kontrakten: den skal aldri varsles om.

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
