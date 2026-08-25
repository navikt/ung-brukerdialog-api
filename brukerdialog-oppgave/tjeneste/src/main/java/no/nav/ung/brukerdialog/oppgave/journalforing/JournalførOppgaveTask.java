package no.nav.ung.brukerdialog.oppgave.journalforing;

import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import no.nav.k9.felles.exception.HttpStatuskodeException;
import no.nav.k9.felles.integrasjon.dokarkiv.DokarkivKlient;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.Bruker;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.OpprettJournalpostRequest;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.OpprettJournalpostRequestBuilder;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.OpprettJournalpostResponse;
import no.nav.k9.felles.integrasjon.pdl.Behandlingsnummer;
import no.nav.k9.felles.integrasjon.pdl.HentPersonQueryRequest;
import no.nav.k9.felles.integrasjon.pdl.Navn;
import no.nav.k9.felles.integrasjon.pdl.NavnResponseProjection;
import no.nav.k9.felles.integrasjon.pdl.Pdl;
import no.nav.k9.felles.integrasjon.pdl.Person;
import no.nav.k9.felles.integrasjon.pdl.PersonResponseProjection;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskHandler;
import no.nav.ung.brukerdialog.JsonObjectMapper;
import no.nav.ung.brukerdialog.journalforing.pdf.PdfDokument;
import no.nav.ung.brukerdialog.journalforing.pdf.PdfGenerator;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.typer.JournalpostId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Journalfører en brukerdialogoppgave mot Dokarkiv. Opprettes av
 * {@code OppgaveLivssyklusTjeneste} ved oppgaveopprettelse, men kjøres uavhengig av den, slik at
 * feil i journalføring aldri blokkerer selve oppgaveopprettelsen.
 * <p>
 * {@code maxFailedRuns/firstDelay/thenDelay} gir ~21 minutter med retry (5 forsøk) før tasken
 * ender i {@code FEILET} - robust mot forbigående 5xx, men rask nok til å varsle om en reell
 * feil.
 */
@ApplicationScoped
@ProsessTask(value = JournalførOppgaveTask.TASKTYPE, maxFailedRuns = 5, firstDelay = 60, thenDelay = 300)
public class JournalførOppgaveTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "oppgave.journalfor";
    public static final String OPPGAVE_REFERANSE = "oppgaveReferanse";

    /** Helautomatisk journalføring - ingen saksbehandler er involvert. */
    private static final String JOURNALFOERENDE_ENHET = "9999";

    /**
     * TODO: foreløpig brevkode - skal bekreftes med Team Dokumentløsninger.
     * <p>
     * Dokumentet som journalføres her er nettopp et forhåndsvarsel: parten skal varsles før
     * vedtak treffes og få høve til å uttale seg innen en frist, jf. fvl. § 16 første ledd.
     * Varselet skal gjøre greie for hva saken gjelder, jf. § 16 tredje ledd - derfor skal
     * ytelsen alltid framgå tydelig av tittel og brevtekst (se {@link OppgaveDokumentTekster}).
     *
     * @see <a href="https://lovdata.no/lov/1967-02-10/§16">forvaltningsloven § 16 (forhåndsvarsling)</a>
     */
    private static final String BREVKODE = "FVL 04-16.0";

    private static final String TILLEGGSOPPLYSNING_NOKKEL = "ung.oppgave.eRef";

    private static final Logger log = LoggerFactory.getLogger(JournalførOppgaveTask.class);

    private OppgaveJournalføringRepository journalføringRepository;
    private BrukerdialogOppgaveRepository oppgaveRepository;
    private JournalføringKonfig journalføringKonfig;
    private Instance<OppgaveDokumentUtleder> dokumentUtledere;
    private Pdl pdl;
    private PdfGenerator pdfGenerator;
    private DokarkivKlient dokarkivKlient;

    JournalførOppgaveTask() {
        // for CDI proxy
    }

    @Inject
    public JournalførOppgaveTask(OppgaveJournalføringRepository journalføringRepository,
                                  BrukerdialogOppgaveRepository oppgaveRepository,
                                  JournalføringKonfig journalføringKonfig,
                                  @Any Instance<OppgaveDokumentUtleder> dokumentUtledere,
                                  Pdl pdl,
                                  PdfGenerator pdfGenerator,
                                  DokarkivKlient dokarkivKlient) {
        this.journalføringRepository = journalføringRepository;
        this.oppgaveRepository = oppgaveRepository;
        this.journalføringKonfig = journalføringKonfig;
        this.dokumentUtledere = dokumentUtledere;
        this.pdl = pdl;
        this.pdfGenerator = pdfGenerator;
        this.dokarkivKlient = dokarkivKlient;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        UUID oppgavereferanse = UUID.fromString(prosessTaskData.getPropertyValue(OPPGAVE_REFERANSE));

        OppgaveJournalføringEntitet journalføring = journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)
            .orElseThrow(() -> new IllegalStateException(
                "Finner ingen journalføringsrad for oppgavereferanse " + oppgavereferanse));

        // Idempotens: journalpost_id settes kun ved reell suksess, så denne sjekken er trygg
        // selv om tasken skulle bli kjørt på nytt etter et vellykket forsøk.
        if (journalføring.erJournalført()) {
            log.info("Oppgave {} er allerede journalført, hopper over", oppgavereferanse);
            JournalføringMetrikker.registrer(journalføring.getOppgave().getOppgaveType(), JournalføringMetrikker.Resultat.HOPPET_OVER);
            return;
        }

        BrukerdialogOppgaveEntitet oppgave = oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)
            .orElseThrow(() -> new IllegalStateException(
                "Finner ingen oppgave for oppgavereferanse " + oppgavereferanse));

        // Flagget kan ha blitt slått av (globalt eller per oppgavetype) etter at raden ble
        // opprettet. Raden blir da stående PLANLAGT - det er poenget med at den lagres
        // uavhengig av flagget.
        if (!journalføringKonfig.erAktivertFor(oppgave.getOppgaveType())) {
            log.info("Journalføring er deaktivert for oppgavetype {} (oppgaveReferanse={}) - raden forblir PLANLAGT",
                oppgave.getOppgaveType(), oppgavereferanse);
            JournalføringMetrikker.registrer(oppgave.getOppgaveType(), JournalføringMetrikker.Resultat.HOPPET_OVER);
            return;
        }

        var tidtaking = JournalføringMetrikker.startTidtaking();
        try {
            PersonInfo person = hentPersonInfo(oppgave);

            OppgaveDokumentUtleder dokumentUtleder = OppgaveDokumentUtleder.finnUtleder(dokumentUtledere, oppgave.getOppgaveType());
            String tittel = dokumentUtleder.utledTittel(oppgave);
            Map<String, Object> oppgaveData = byggOppgaveData(dokumentUtleder, oppgave);

            String opprettetDato = oppgave.getOpprettetTidspunkt().toLocalDate().toString();
            byte[] pdf = pdfGenerator.genererPdf(new PdfDokument(dokumentUtleder.malnavn(), byggPdfData(tittel, opprettetDato, oppgaveData, person)));
            byte[] json = tilOriginalJson(oppgaveData);

            OpprettJournalpostRequest request = byggJournalpostRequest(oppgave, journalføring, person, tittel, pdf, json);

            OpprettJournalpostResponse response = dokarkivKlient.opprettJournalpost(request);
            journalføring.markerJournalført(new JournalpostId(response.journalpostId()));
            journalføringRepository.oppdater(journalføring);
            JournalføringMetrikker.registrer(oppgave.getOppgaveType(), JournalføringMetrikker.Resultat.OK);
            if (response.journalpostferdigstilt()) {
                log.info("Journalførte oppgave {} med journalpostId {}", oppgavereferanse, response.journalpostId());
            } else {
                JournalføringMetrikker.registrerIkkeFerdigstilt(oppgave.getOppgaveType());
                log.warn("Journalpost {} for oppgave {} ble opprettet, men ikke ferdigstilt: {}",
                    response.journalpostId(), oppgavereferanse, response.melding());
            }
        } catch (HttpStatuskodeException e) {
            if (e.getHttpStatuskode() == 409) {
                // Journalposten finnes allerede for denne eksternReferanseId, men journalpostId
                // kan ikke leses fra responsbody med dagens OidcRestClient (k9-felles 11.2.13).
                // Vi lagrer aldri en journalført rad uten journalpostId - tasken feiler i stedet
                // og følges opp manuelt.
                // TODO: fjern denne grenen når k9-felles eksponerer responsbody ved feilstatus.
                JournalføringMetrikker.registrer(oppgave.getOppgaveType(), JournalføringMetrikker.Resultat.DUPLIKAT_UTEN_ID);
                throw new JournalføringException(
                    "Journalpost finnes allerede for oppgavereferanse %s, men journalpostId kunne ikke leses (HTTP 409)"
                        .formatted(oppgavereferanse), e);
            }
            JournalføringMetrikker.registrer(oppgave.getOppgaveType(), JournalføringMetrikker.Resultat.FEILET);
            throw e;
        } catch (RuntimeException e) {
            JournalføringMetrikker.registrer(oppgave.getOppgaveType(), JournalføringMetrikker.Resultat.FEILET);
            throw e;
        } finally {
            JournalføringMetrikker.stoppTidtaking(tidtaking, oppgave.getOppgaveType());
        }
    }

    @Override
    public Set<String> requiredProperties() {
        return Set.of(OPPGAVE_REFERANSE);
    }

    /**
     * Henter fødselsnummer og navn for PDF-en og journalpost-metadataen.
     * <p>
     * To PDL-kall, hver med den API-en som er mest robust for sitt formål:
     * <ol>
     *   <li>{@code hentPersonIdentForAktørId} - kun gjeldende {@code FOLKEREGISTERIDENT}, uten
     *       historikk. Tom {@link Optional} betyr at PDL ikke har et folkeregisterident for
     *       personen (f.eks. kun NPID) - kaster {@link JournalføringException} med
     *       kun oppgavereferanse og oppgavetype, ALDRI aktørId eller fnr.</li>
     *   <li>{@code hentPerson} med {@code Behandlingsnummer} utledet fra oppgavens ytelsetype
     *       (se {@link JournalføringParametre}), for navnet. Navnet formateres som
     *       {@code fornavn mellomnavn etternavn} via {@link #formaterNavn} - IKKE
     *       {@code ung-sak}s {@code PersonBasisTjeneste.mapNavn}-sorteringsformat
     *       ({@code etternavn fornavn mellomnavn}).</li>
     * </ol>
     * Fødselsnummeret og navnet lever kun i denne metodens kallstack og i {@link PersonInfo} -
     * de lagres aldri i databasen, og {@link PersonInfo#toString()} er PII-fri.
     * <p>
     * Et PDL-404 eller annen {@code PdlException} fanges bevisst ikke her - den skal propagere
     * til {@link #doTask} og videre til prosesstask-rammeverkets retry.
     */
    private PersonInfo hentPersonInfo(BrukerdialogOppgaveEntitet oppgave) {
        String aktørId = oppgave.getAktørId().getId();

        // Optional.empty() betyr at PDL ikke har et FOLKEREGISTERIDENT for personen (f.eks. kun
        // NPID) - ikke at PDL svarte 404. Et PDL-404 kastes av hentPersonIdentForAktørId selv
        // (PdlException) og skal propagere til retry, ikke fanges her.
        String fødselsnummer = pdl.hentPersonIdentForAktørId(aktørId)
            .orElseThrow(() -> new JournalføringException(
                "Fant ikke folkeregisterident for oppgave %s (oppgavetype %s)"
                    .formatted(oppgave.getOppgavereferanse(), oppgave.getOppgaveType())));

        Behandlingsnummer behandlingsnummer = JournalføringParametre.utled(oppgave.getYtelsetype()).behandlingsnummer();
        var query = new HentPersonQueryRequest();
        query.setIdent(aktørId); // PDL godtar aktørId.
        var projection = new PersonResponseProjection()
            .navn(new NavnResponseProjection().fornavn().mellomnavn().etternavn());

        Person person = pdl.hentPerson(query, projection, List.of(behandlingsnummer));
        String navn = person.getNavn().stream()
            .findFirst()
            .map(JournalførOppgaveTask::formaterNavn)
            .orElseThrow(() -> new JournalføringException(
                "Fant ikke navn for oppgave %s (oppgavetype %s)"
                    .formatted(oppgave.getOppgavereferanse(), oppgave.getOppgaveType())));

        return new PersonInfo(fødselsnummer, navn);
    }

    /**
     * Naturlig navnerekkefølge til brevet - {@code fornavn mellomnavn etternavn}. IKKE kopier
     * {@code ung-sak}s {@code PersonBasisTjeneste.mapNavn}, som gir sorteringsformatet
     * {@code etternavn fornavn mellomnavn}.
     */
    private static String formaterNavn(Navn navn) {
        return Stream.of(navn.getFornavn(), navn.getMellomnavn(), navn.getEtternavn())
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" "));
    }

    /**
     * Oppgavetype-spesifikt innhold (se {@link OppgaveDokumentUtleder}), utvidet med
     * {@code oppgaveReferanse} - satt sentralt her slik at ingen av de åtte implementasjonene
     * trenger å duplisere det.
     */
    private Map<String, Object> byggOppgaveData(OppgaveDokumentUtleder utleder, BrukerdialogOppgaveEntitet oppgave) {
        Map<String, Object> data = new LinkedHashMap<>(utleder.utledInnholdsdata(oppgave));
        data.putIfAbsent("oppgaveReferanse", oppgave.getOppgavereferanse().toString());
        return data;
    }

    /**
     * Fletter tittel, opprettelsesdato, navn/fødselsnummer (PII) og oppgavetype-spesifikt
     * innhold til datamodellen malen ({@link OppgaveDokumentUtleder#malnavn()}) rendres mot.
     * Flettingen skjer bevisst sentralt her og ikke i hver enkelt {@link OppgaveDokumentUtleder}
     * - se klasse-javadoc der.
     */
    private Map<String, Object> byggPdfData(String tittel, String opprettetDato, Map<String, Object> oppgaveData, PersonInfo person) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tittel", tittel);
        data.put("opprettetDato", opprettetDato);
        data.put("navn", person.navn());
        data.put("fødselsnummer", person.fødselsnummer());
        data.put("oppgave", oppgaveData);
        return data;
    }

    /**
     * {@code ORIGINAL}-varianten skal inneholde oppgavedata, ikke navn/fnr.
     */
    private byte[] tilOriginalJson(Map<String, Object> oppgaveData) {
        try {
            return JsonObjectMapper.getMapper().writeValueAsBytes(oppgaveData);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(
                "Klarte ikke å serialisere oppgavedata til JSON (ORIGINAL-variant)", e);
        }
    }

    /**
     * Bygger dokarkiv-requesten. {@code tema}/{@code fagsaksystem}/{@code sakstype}/
     * {@code fagsakId} leses fra den lagrede journalføringsraden, ikke re-utledes - utledningen
     * skjer én gang, ved opprettelse, og lagres i raden, slik at raden forblir det etterrettelige
     * sporet av hva som faktisk ble sendt til arkivet.
     */
    private OpprettJournalpostRequest byggJournalpostRequest(BrukerdialogOppgaveEntitet oppgave,
                                                               OppgaveJournalføringEntitet journalføring,
                                                               PersonInfo person,
                                                               String tittel,
                                                               byte[] pdf,
                                                               byte[] json) {
        var bruker = new Bruker(person.fødselsnummer(), Bruker.BrukerIdType.FNR);
        // navn utelates bevisst - Dokarkiv slår selv opp navn i PDL.
        var avsenderMottaker = new OpprettJournalpostRequest.AvsenderMottaker(
            person.fødselsnummer(), null, null, OpprettJournalpostRequest.AvsenderMottaker.IdType.FNR);

        var sak = journalføring.getSakstype() == Sakstype.FAGSAK
            ? OpprettJournalpostRequest.Sak.forSaksnummer(journalføring.getFagsakId().getVerdi(), journalføring.getFagsaksystem().name())
            : OpprettJournalpostRequest.Sak.GENERELL_FAGSAK;

        var dokument = new OpprettJournalpostRequest.Dokument(
            tittel,
            BREVKODE,
            null,
            List.of(
                new OpprettJournalpostRequest.DokumentVariantArkivertPDFA(pdf),
                // "ArkivertPDFA" er k9-felles sitt navn på denne recorden, men feltene
                // (filtype/variantformat/fysiskDokument) er generiske - brukes her til
                // ORIGINAL/JSON-varianten også, ikke bare PDF/A-arkivvarianten.
                new OpprettJournalpostRequest.DokumentVariantArkivertPDFA(
                    OpprettJournalpostRequest.Filtype.JSON, OpprettJournalpostRequest.Variantformat.ORIGINAL, json)
            )
        );

        String oppgavereferanse = oppgave.getOppgavereferanse().toString();
        return new OpprettJournalpostRequestBuilder()
            .medJournalpostType("UTGAAENDE")
            .medAvsenderMottaker(avsenderMottaker)
            .medBruker(bruker)
            .medTema(journalføring.getTema().name())
            .medTittel(tittel)
            .medJournalfoerendeEnhet(JOURNALFOERENDE_ENHET)
            .medEksternReferanseId(oppgavereferanse)
            .medTilleggsopplysninger(List.of(new OpprettJournalpostRequest.Tilleggsopplysning(TILLEGGSOPPLYSNING_NOKKEL, oppgavereferanse)))
            .medSak(sak)
            .medDokumenter(List.of(dokument))
            .build();
    }

    /**
     * Fødselsnummer og navn hentet fra PDL for journalføring. Bevisst PII-fri
     * {@code toString()} - fnr og navn skal aldri havne i logg.
     */
    private record PersonInfo(String fødselsnummer, String navn) {
        @Override
        public String toString() {
            return "PersonInfo{<PII utelatt>}";
        }
    }
}
