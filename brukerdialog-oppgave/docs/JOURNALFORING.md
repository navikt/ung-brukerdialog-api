# Journalføring av brukerdialogoppgaver

Alle varsler/oppgaver som opprettes av `ung-brukerdialog-api` skal journalføres i Dokarkiv
(Joark) — det er et lovkrav, se «Rettslig grunnlag» under. Denne siden beskriver hvordan det er
løst. For bakgrunn og vurderinger, se det opprinnelige planleggingsdokumentet i teamets interne
arkiv; denne siden dekker den ferdige løsningen.

## Rettslig grunnlag

- **Journalføringsplikten** følger av
  [§ 14 i forskrift om dokumentasjon og arkiv (arkivforskrifta)](https://lovdata.no/dokument/SF/forskrift/2025-12-17-2647):
  alle inngående og utgående dokumenter som er saksdokumenter etter offentleglova § 4, og som er
  eller blir saksbehandlet, skal registreres i journalen. §§ 14-15 angir også hvilke metadata som
  skal registreres (avsender/mottaker, dato, tittel o.l.) — se «Datamodell» under for hvordan det
  er løst her.
- **Dokumentet er et forhåndsvarsel** etter
  [§ 16 i forvaltningsloven](https://lovdata.no/lov/1967-02-10/§16): parten skal varsles før
  vedtak treffes og få høve til å uttale seg innen en frist. Loven krever også at varselet «gjøre
  greie for hva saken gjelder» (§ 16 tredje ledd) — det er derfor ytelsen alltid skal framgå
  tydelig av tittel og brevtekst, se «Dokumentet som journalføres» under. Brevkoden
  (se «Kjente begrensninger») er valgt ut fra denne bestemmelsen.

## Når journalføres en oppgave?

**Ved opprettelse** — `OppgaveLivssyklusTjeneste.opprettOppgave(...)` planlegger journalføringen
som en egen `ProsessTask` (`JournalførOppgaveTask`) i **samme transaksjon** som oppgaven.
Journalføringsraden opprettes **ikke** her, men av tasken selv, først når journalføringen
faktisk lykkes — enten journalfører vi og lagrer en komplett rad, eller det skjer ingenting.
Journalposten oppdateres heller **ikke** ved senere statusendring (f.eks. når oppgaven besvares
eller utløper) — den beskriver alltid oppgaven slik den var ved opprettelse.

Journalføringen skjer i en egen task nettopp for at feil i journalføring (Dokarkiv nede, PDL
nede, o.l.) aldri skal blokkere selve oppgaveopprettelsen eller Min Side-varselet.

## Journalposttype

Alltid `UTGAAENDE` — dokumentet går fra Nav til bruker.

## Hvordan tema og fagsaksystem bestemmes

`tema`, `fagsaksystem` og PDL-`behandlingsnummer` utledes fra oppgavens `OppgaveYtelsetype`
(påkrevd felt på `OpprettOppgaveDto`) via en uttømmende `switch` i `JournalføringParametre`:

```java
public static JournalføringParametre utled(OppgaveYtelsetype ytelsetype) {
    return switch (ytelsetype) {
        case UNGDOMSYTELSE    -> new JournalføringParametre(Fagsaksystem.UNG_SAK, Tema.UNG, Behandlingsnummer.UNGDOMSYTELSEN);
        case AKTIVITETSPENGER -> new JournalføringParametre(Fagsaksystem.UNG_SAK, Tema.UNG, Behandlingsnummer.AKTIVITETSPENGER);
    };
}
```

Kalleren kan altså **ikke** sende inn feil tema — det finnes ikke som et felt på requestbody.
`switch`-en er bevisst uttømmende uten `default`, slik at en ny `OppgaveYtelsetype` gir
kompileringsfeil i stedet for å stille arve `Tema.UNG`.

> K9-ytelser (`Fagsaksystem.K9`, `Tema.OMS`) er foreløpig ikke støttet — se
> [`OppgaveYtelsetype`](../../kontrakt/src/main/java/no/nav/ung/brukerdialog/kontrakt/oppgaver/OppgaveYtelsetype.java).

Kode: [`JournalføringParametre`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/journalforing/JournalføringParametre.java)

## Sakstype — FAGSAK eller GENERELL_SAK

| `journalføring.saksnummer` på requestbody | Dokarkiv `sak` |
|---|---|
| satt | `{ sakstype: FAGSAK, fagsakId, fagsaksystem }` |
| mangler | `{ sakstype: GENERELL_SAK }` |

`SØK_YTELSE` er den eneste oppgavetypen som har lov til å mangle `saksnummer` (den har ingen
fagsak ved opprettelse). For alle andre typer er `saksnummer` egentlig ment å være påkrevd, med
`@GyldigJournalføring` på `OpprettOppgaveDto` som håndhevende mekanisme — men denne annotasjonen
er **midlertidig fjernet fra DTO-en** (se "Kjente begrensninger" under), så håndhevelsen er i
praksis avslått inntil videre. `GyldigJournalføringValidator` finnes fortsatt i kode og er
testdekket direkte, klar til å kobles på igjen.

## Datamodell

Journalføring har sitt eget livsløp og ligger i en egen tabell, `BD_OPPGAVE_JOURNALFORING`, med
FK til `BD_OPPGAVE`. `BD_OPPGAVE` er urørt av journalføring.

Raden finnes **hvis og bare hvis** oppgaven faktisk er journalført — det finnes ingen
mellomtilstand. `journalpost_id` og `journalfort_tid` er derfor påkrevde felter, satt idet raden
opprettes; de kan aldri stå tomme på en eksisterende rad.

- `saksnummer satt ⟺ sakstype = 'FAGSAK'` er en check-constraint (defence in depth).

Kode: [`OppgaveJournalføringEntitet`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/journalforing/OppgaveJournalføringEntitet.java),
[`OppgaveJournalføringRepository`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/journalforing/OppgaveJournalføringRepository.java)

## Migrerte oppgaver journalføres ikke

`MigrerBrukerdialogOppgaverRestTjeneste` skriver direkte til repositoryet og går utenom
`OppgaveLivssyklusTjeneste`. Migrerte oppgaver (fra den gamle applikasjonen) får derfor ingen
journalføringsrad — det er en villet konsekvens, ikke en glipp.

## Dokumentet som journalføres

Hver oppgavetype har en egen `OppgaveDokumentUtleder`-implementasjon (SPI, koblet via
`@OppgaveTypeRef`) som gir tittel og innholdsdata til en felles PDF-mal
(Handlebars + openhtmltopdf). Se
[`LEGG_TIL_NY_OPPGAVETYPE.md`](../tjeneste/LEGG_TIL_NY_OPPGAVETYPE.md) for hvordan dette kobles
for en ny oppgavetype.

To dokumentvarianter følger med journalposten:

| Variant | Format | Innhold |
|---|---|---|
| `ARKIV` | PDF | Brukerrettet dokument |
| `ORIGINAL` | JSON | Maskinlesbar oppgavedata |

`OppgaveDokumentUtleder`-implementasjonene skal **aldri** returnere navn eller fødselsnummer —
personopplysningene legges til av `JournalførOppgaveTask` selv, hentet fra PDL, slik at
implementasjonene forblir testbare uten ekte fødselsnumre.

Kode: [`OppgaveDokumentUtleder`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/journalforing/OppgaveDokumentUtleder.java),
[`PdfGenerator`](../pdf/src/main/java/no/nav/ung/brukerdialog/pdf/PdfGenerator.java)

## Bruker og mottaker

`bruker` og `avsenderMottaker` på journalposten er brukerens fødselsnummer, slått opp fra
`aktørId` via PDL i tasken (`hentPersonIdentForAktørId`). Navn til PDF-en hentes med et eget
PDL-kall (`hentPerson`) med `behandlingsnummer` fra `JournalføringParametre` — se
`JournalførOppgaveTask.hentPersonInfo(...)`.

Fødselsnummeret lagres **ikke** i journalføringstabellen — det slås opp i tasken og lever kun i
minne under kallet.

## Feilhåndtering

| Situasjon | Oppførsel |
|---|---|
| Dokarkiv svarer 409 (journalposten finnes fra før) | Behandles som en gyldig, idempotent respons med eksisterende `journalpostId` (via `DokarkivKlient`), og raden lagres som normalt. |
| Dokarkiv svarer 5xx | Propagerer, prosesstask retryer (`maxFailedRuns=5, firstDelay=60, thenDelay=300`) - ingen rad lagres før forsøket lykkes |
| PDL finner ingen folkeregisterident | Tasken feiler med en melding som navngir oppgavereferanse og oppgavetype — **aldri** aktørId eller fødselsnummer |
| PDL-nedetid | Propagerer, prosesstask retryer |
| Journalpost opprettet, men ikke ferdigstilt | Logges som `WARN` — raden lagres likevel siden journalposten finnes |

## Parametrisering

| Miljøvariabel | Type | Default | Formål |
|---|---|---|---|
| `JOURNALFORING_ENABLED` | Boolean | `false` | Global av/på-bryter. `true` i dev-gcp, `false` i prod-gcp inntil flyten er verifisert. |
| `JOURNALFORING_DEAKTIVERTE_OPPGAVETYPER` | String (kommaseparert) | `""` | Slå av enkelt-oppgavetyper uten ny deploy. Ugyldig oppgavetype feiler ved oppstart. |

Flagget styrer **kun** om `JournalførOppgaveTask` faktisk journalfører — sjekket av tasken selv
idet den kjører, ikke ved oppretting. Er flagget av, journalføres ikke oppgaven, og ingen rad
opprettes.

Kode: [`JournalføringKonfig`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/journalforing/JournalføringKonfig.java)

## Kjente begrensninger / åpne punkter

- **Brevkode** er per ytelse (`Brevkode`-enum): `FVL – forhåndsvarsel – ungdomsytelsen` og
  `FVL – forhåndsvarsel – aktivitetspenger` — foreløpige verdier, ikke bekreftet med Team
  Dokumentløsninger.
- **K9-ytelser** (`Fagsaksystem.K9`, `Tema.OMS`) er ikke støttet ennå.
- **`saksnummer` er foreløpig valgfri** selv for oppgavetyper som normalt har fagsak —
  `@GyldigJournalføring` (som ville håndhevet dette som en 400-feil) er bevisst fjernet fra
  `OpprettOppgaveDto` inntil `ung-sak`/nedstrøms konsumenter er bekreftet klare til å alltid
  sende feltet i prod. Manglende verdi gir i mellomtiden kun en `WARN`-logg (se
  `OppgaveLivssyklusTjeneste`). Annotasjonen gjeninnføres på DTO-en når feltet er i prod.
- **Prod er ikke aktivert ennå** (`JOURNALFORING_ENABLED=false` i `prod-gcp.yml`) — aktiveres når
  flyten er verifisert i dev.

## Sentrale klasser

| Klasse | Modul | Ansvar |
|---|---|---|
| `JournalføringParametre` | `tjeneste` | Utleder tema/fagsaksystem/behandlingsnummer fra ytelsetype |
| `GyldigJournalføringValidator` | `kontrakt` | Validerer `saksnummer` mot oppgavetype — **ikke koblet på** (`@GyldigJournalføring` fjernet fra DTO, se «Kjente begrensninger») |
| `OppgaveJournalføringEntitet` / `Repository` | `tjeneste` | Datamodell og persistens |
| `JournalførOppgaveTask` | `tjeneste` | Selve journalføringen: PDL-oppslag, PDF, kall mot Dokarkiv |
| `JournalføringKonfig` | `tjeneste` | Parametrisering (på/av, deny-liste) |
| `OppgaveDokumentUtleder` (+ typer) | `tjeneste` | Tittel og PDF-innhold per oppgavetype |
| `DokarkivKlient` (k9-felles) | ekstern avhengighet | HTTP-klient mot Dokarkiv - delt bibliotek fra `k9-dokarkiv-klient`, ikke egenbygd |
| `PdfGenerator` | `pdf` | Handlebars + openhtmltopdf |
