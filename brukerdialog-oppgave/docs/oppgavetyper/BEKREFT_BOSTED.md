# BEKREFT_BOSTED

Bruker bekrefter at de er bosatt i Trondheim for å motta aktivitetspenger.

## Ytelse

Aktivitetspenger

## Trigger

Systemet oppretter oppgaven når det er behov for å avklare bosted for en periode.

## Varsel til bruker

> Du har fått en oppgave om å bekrefte bosted for aktivitetspenger

Lenken peker til oppgavesiden hos aktivitetspenger-innsyn.

## Data

Kontrakt: [`BekreftBostedOppgavetypeDataDto`](../../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/bosted/BekreftBostedOppgavetypeDataDto.java)

| Felt | Type | Beskrivelse                                            |
|------|------|--------------------------------------------------------|
| `fom` | `LocalDate` | Startdato for perioden bosted gjelder                  |
| `tom` | `LocalDate` | Sluttdato for perioden bosted gjelder                  |
| `erBosattITrondheim` | `Boolean` | Brukers svar: er bosatt i Trondheim eller ikke         |
| `ikkeOppfyltÅrsak` | `BostedsvilkårIkkeOppfyltÅrsak` | Årsak til at bostedsvilkåret tas opp til vurdering     |
| `ikkeOppfyltÅrsakFritekstbeskrivelse` | `String` | Fritekst, utfylt kun når årsak = `ANNET`               |
| `kilde` | `BostedsavklaringKildeType` | Påkrevd. Hvor saksbehandler har fått opplysningene fra |
| `kildeFritekst` | `String` | Påkrevd når kilde = `ANNET`                            |

Opphørsvarianten [`BekreftBostedOpphørOppgavetypeDataDto`](../../../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/typer/bosted/BekreftBostedOpph%C3%B8rOppgavetypeDataDto.java)
har de samme feltene, men uten `tom`.

## Implementasjon

| Klasse | Beskrivelse |
|--------|-------------|
| [`BekreftBostedOppgavelInnholdUtleder`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/bosted/BekreftBostedOppgavelInnholdUtleder.java) | Utleder varseltekst og lenke |
| [`BekreftBostedOppgaveDataEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/bosted/BekreftBostedOppgaveDataEntitet.java) | JPA-entitet for oppgavedata |
| [`BekreftBostedOppgaveDataMapperFraDtoTilEntitet`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/bosted/BekreftBostedOppgaveDataMapperFraDtoTilEntitet.java) | Mapper fra DTO til entitet |
| [`BekreftBostedOppgaveDokumentUtleder`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/journalforing/typer/BekreftBostedOppgaveDokumentUtleder.java) | Bygger innholdsdata til det journalførte dokumentet |
| [`BekreftBostedOppgaveDataMapperFraEntitetTilDto`](../../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/typer/varsel/typer/bosted/BekreftBostedOppgaveDataMapperFraEntitetTilDto.java) | Mapper fra entitet til DTO |
