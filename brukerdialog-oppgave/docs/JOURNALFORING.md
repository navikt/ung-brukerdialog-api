# Journalføring av brukerdialogoppgaver

Alle varsler/oppgaver som opprettes av `ung-brukerdialog-api` skal journalføres i Dokarkiv
(Joark) — det er et lovkrav. Denne siden beskriver hvordan det er løst. For bakgrunn og
vurderinger, se det opprinnelige planleggingsdokumentet i teamets interne arkiv; denne siden
dekker den ferdige løsningen.

## Når journalføres en oppgave?

**Ved opprettelse** — `OppgaveLivssyklusTjeneste.opprettOppgave(...)` oppretter alltid en
journalføringsrad, og planlegger journalføringen som en egen `ProsessTask`
(`JournalførOppgaveTask`) i **samme transaksjon** som oppgaven. Journalposten oppdateres
**ikke** ved senere statusendring (f.eks. når oppgaven besvares eller utløper) — den beskriver
alltid oppgaven slik den var ved opprettelse.

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

| `journalføring.fagsakId` på requestbody | Dokarkiv `sak` |
|---|---|
| satt | `{ sakstype: FAGSAK, fagsakId, fagsaksystem }` |
| mangler | `{ sakstype: GENERELL_SAK }` |

`SØK_YTELSE` er den eneste oppgavetypen som har lov til å mangle `fagsakId` (den har ingen
fagsak ved opprettelse). For alle andre typer er `fagsakId` påkrevd — håndheves av
`@GyldigJournalføring` på `OpprettOppgaveDto` med en feilmelding som navngir både oppgavetypen
og hvilke typer som er unntatt.

## Datamodell

Journalføring har sitt eget livsløp og ligger i en egen tabell, `BD_OPPGAVE_JOURNALFORING`, med
FK til `BD_OPPGAVE`. `BD_OPPGAVE` er urørt av journalføring.

```
PLANLAGT  →  JOURNALFORT
```

- Raden opprettes **alltid**, uavhengig av om `JournalførOppgaveTask` faktisk kjøres. Det gjør
  at «hva mangler journalføring?» blir ett spørsmål (`status = 'PLANLAGT'`), uansett om årsaken
  er at flagget er av, oppgavetypen er deaktivert, eller tasken ikke har kjørt ennå.
- `journalpost_id` settes kun ved reell suksess fra Dokarkiv, og databasen håndhever
  `journalpost_id satt ⟺ status = 'JOURNALFORT'` som en check-constraint (defence in depth).
- `fagsak_id satt ⟺ sakstype = 'FAGSAK'` er også en check-constraint.

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
| `ARKIV` | PDF | Brukerrettet dokument, inkluderer brukerens navn og fødselsnummer |
| `ORIGINAL` | JSON | Maskinlesbar oppgavedata — **ikke** navn/fødselsnummer |

`OppgaveDokumentUtleder`-implementasjonene skal **aldri** returnere navn eller fødselsnummer —
personopplysningene legges til av `JournalførOppgaveTask` selv, hentet fra PDL, slik at
implementasjonene forblir testbare uten ekte fødselsnumre.

Kode: [`OppgaveDokumentUtleder`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/journalforing/OppgaveDokumentUtleder.java),
[`PdfGenerator`](../journalforing/src/main/java/no/nav/ung/brukerdialog/journalforing/pdf/PdfGenerator.java)

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
| Dokarkiv svarer 409 (journalposten finnes fra før) | Tasken **feiler** (`JournalføringException`) i stedet for å lagre uten `journalpostId` — journalpostId kan ikke leses fra 409-responsen med dagens `OidcRestClient`. Følges opp manuelt. |
| Dokarkiv svarer 5xx | Propagerer, prosesstask retryer (`maxFailedRuns=5, firstDelay=60, thenDelay=300`) |
| PDL finner ingen folkeregisterident | Tasken feiler med en melding som navngir oppgavereferanse og oppgavetype — **aldri** aktørId eller fødselsnummer |
| PDL-nedetid | Propagerer, prosesstask retryer |
| Journalpost opprettet, men ikke ferdigstilt | Logges som `WARN` og telles i egen metrikk — raden markeres likevel `JOURNALFORT` siden journalposten finnes |

## Parametrisering

| Miljøvariabel | Type | Default | Formål |
|---|---|---|---|
| `JOURNALFORING_ENABLED` | Boolean | `false` | Global av/på-bryter. `true` i dev-gcp, `false` i prod-gcp inntil flyten er verifisert. |
| `JOURNALFORING_DEAKTIVERTE_OPPGAVETYPER` | String (kommaseparert) | `""` | Slå av enkelt-oppgavetyper uten ny deploy. Ugyldig oppgavetype feiler ved oppstart. |

Flagget styrer **kun** om `JournalførOppgaveTask` opprettes — journalføringsraden lagres alltid,
slik at etterslepet forblir komplett og spørrbart uansett konfigurasjon.

Kode: [`JournalføringKonfig`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/journalforing/JournalføringKonfig.java)

## Observabilitet

| Metrikk | Tagger | Formål |
|---|---|---|
| `ung_brukerdialog_journalforing_total` | `oppgavetype`, `resultat` (`OK`/`DUPLIKAT_UTEN_ID`/`FEILET`/`HOPPET_OVER`) | Volum og feilrate |
| `ung_brukerdialog_journalforing_ikke_ferdigstilt_total` | `oppgavetype` | Journalpost opprettet, men ikke ferdigstilt |
| `ung_brukerdialog_journalforing_varighet` | `oppgavetype` | Latens mot Dokarkiv |
| `ung_brukerdialog_journalforing_etterslep` | – | Gauge: antall rader med `status = 'PLANLAGT'` eldre enn 1 time. Fanger opp både «flagget er av» og «tasken kom aldri i mål». |

Alle metrikker eksponeres på `/internal/metrics/prometheus`. Loggmeldinger inneholder kun
`oppgavereferanse`, `oppgavetype`, `journalpostId` og `fagsaksystem` — aldri fødselsnummer,
aktørId eller oppgavedata.

Kode: [`JournalføringMetrikker`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/journalforing/JournalføringMetrikker.java)

## Kjente begrensninger / åpne punkter

- **Brevkode** `FVL 04-16.0` er foreløpig og skal bekreftes med Team Dokumentløsninger — se
  `TODO` i [`JournalførOppgaveTask`](../tjeneste/src/main/java/no/nav/ung/brukerdialog/oppgave/journalforing/JournalførOppgaveTask.java).
- **Behandlingsnummer** (`UNGDOMSYTELSEN`/B950) bør bekreftes med personvernansvarlig for bruk
  til journalføring av utgående varsel, ikke bare søknadsbehandling.
- **K9-ytelser** (`Fagsaksystem.K9`, `Tema.OMS`) er ikke støttet ennå.
- **`fagsakId` er foreløpig valgfri** selv for oppgavetyper som normalt har fagsak — manglende
  verdi gir kun `WARN`-logg. Dette strammes inn til en 400-feil når `ung-sak` er i prod med
  feltet.
- **Prod er ikke aktivert ennå** (`JOURNALFORING_ENABLED=false` i `prod-gcp.yml`) — aktiveres når
  flyten er verifisert i dev.

## Sentrale klasser

| Klasse | Modul | Ansvar |
|---|---|---|
| `JournalføringParametre` | `tjeneste` | Utleder tema/fagsaksystem/behandlingsnummer fra ytelsetype |
| `GyldigJournalføringValidator` | `kontrakt` | Validerer `fagsakId` mot oppgavetype |
| `OppgaveJournalføringEntitet` / `Repository` | `tjeneste` | Datamodell og persistens |
| `JournalførOppgaveTask` | `tjeneste` | Selve journalføringen: PDL-oppslag, PDF, kall mot Dokarkiv |
| `JournalføringKonfig` | `tjeneste` | Parametrisering (på/av, deny-liste) |
| `JournalføringMetrikker` | `tjeneste` | Metrikker |
| `OppgaveDokumentUtleder` (+ typer) | `tjeneste` | Tittel og PDF-innhold per oppgavetype |
| `DokArkivKlient` / `DokArkivKlientImpl` | `journalforing` | HTTP-klient mot Dokarkiv |
| `PdfGenerator` | `journalforing` | Handlebars + openhtmltopdf |
