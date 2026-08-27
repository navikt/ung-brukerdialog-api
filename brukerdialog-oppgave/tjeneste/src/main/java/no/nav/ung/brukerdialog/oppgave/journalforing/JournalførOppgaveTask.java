package no.nav.ung.brukerdialog.oppgave.journalforing;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import no.nav.k9.felles.integrasjon.dokarkiv.DokarkivKlient;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.Bruker;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.OpprettJournalpostRequest;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.OpprettJournalpostRequestBuilder;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.OpprettJournalpostResponse;
import no.nav.k9.felles.integrasjon.pdl.Behandlingsnummer;
import no.nav.k9.felles.integrasjon.pdl.HentPersonQueryRequest;
import no.nav.k9.felles.integrasjon.pdl.Navn;
import no.nav.k9.felles.integrasjon.pdl.NavnResponseProjection;
import no.nav.k9.felles.integrasjon.pdl.PdlKlient;
import no.nav.k9.felles.integrasjon.pdl.Person;
import no.nav.k9.felles.integrasjon.pdl.PersonResponseProjection;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskHandler;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.pdf.PdfDokument;
import no.nav.ung.brukerdialog.pdf.PdfGenerator;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.typer.JournalpostId;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Journalfører en brukerdialogoppgave mot Dokarkiv, uavhengig av
 * {@code OppgaveLivssyklusTjeneste} som oppretter tasken - feil her blokkerer aldri
 * oppgaveopprettelsen.
 * <p>
 * Journalføringsraden ({@link OppgaveJournalføringEntitet}) finnes hvis og bare hvis
 * journalføringen faktisk har lykkes.
 */
@ApplicationScoped
@ProsessTask(value = JournalførOppgaveTask.TASKTYPE, maxFailedRuns = 5, firstDelay = 60, thenDelay = 300)
public class JournalførOppgaveTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "oppgave.journalfor";
    public static final String OPPGAVE_REFERANSE = "oppgaveReferanse";
    /** Valgfri - satt kun når oppgavetypen krever fagsak og saksbehandlingssystemet har oppgitt den. */
    public static final String SAKSNUMMER = "saksnummer";

    /** Helautomatisk journalføring - ingen saksbehandler er involvert. */
    private static final String JOURNALFOERENDE_ENHET = "9999";

    private static final String TILLEGGSOPPLYSNING_NOKKEL = "ung.oppgave.eRef";

    private static final Set<OppgaveType> DEAKTIVERTE_OPPGAVETYPER = EnumSet.noneOf(OppgaveType.class);

    private static final Logger log = LoggerFactory.getLogger(JournalførOppgaveTask.class);

    private OppgaveJournalføringRepository journalføringRepository;
    private BrukerdialogOppgaveRepository oppgaveRepository;
    private boolean journalføringEnabled;
    private Instance<OppgaveDokumentUtleder> dokumentUtledere;
    private PdlKlient pdl;
    private PdfGenerator pdfGenerator;
    private DokarkivKlient dokarkivKlient;

    JournalførOppgaveTask() {
        // for CDI proxy
    }

    @Inject
    public JournalførOppgaveTask(OppgaveJournalføringRepository journalføringRepository,
                                  BrukerdialogOppgaveRepository oppgaveRepository,
                                  @KonfigVerdi(value = "JOURNALFORING_ENABLED", defaultVerdi = "false")
                                  boolean journalføringEnabled,
                                  @Any Instance<OppgaveDokumentUtleder> dokumentUtledere,
                                  PdlKlient pdl,
                                  PdfGenerator pdfGenerator,
                                  DokarkivKlient dokarkivKlient) {
        this.journalføringRepository = journalføringRepository;
        this.oppgaveRepository = oppgaveRepository;
        this.journalføringEnabled = journalføringEnabled;
        this.dokumentUtledere = dokumentUtledere;
        this.pdl = pdl;
        this.pdfGenerator = pdfGenerator;
        this.dokarkivKlient = dokarkivKlient;
    }

    private boolean erAktivertFor(OppgaveType oppgaveType) {
        return journalføringEnabled && !DEAKTIVERTE_OPPGAVETYPER.contains(oppgaveType);
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        UUID oppgavereferanse = UUID.fromString(prosessTaskData.getPropertyValue(OPPGAVE_REFERANSE));

        // Idempotens: raden opprettes kun ved reell suksess, så eksistens alene er nok.
        if (journalføringRepository.hentForOppgaveReferanse(oppgavereferanse).isPresent()) {
            log.info("Oppgave {} er allerede journalført, hopper over", oppgavereferanse);
            return;
        }

        BrukerdialogOppgaveEntitet oppgave = oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)
            .orElseThrow(() -> new IllegalStateException(
                "Finner ingen oppgave for oppgavereferanse " + oppgavereferanse));

        if (!erAktivertFor(oppgave.getOppgaveType())) {
            log.info("Journalføring er deaktivert for oppgavetype {} (oppgaveReferanse={}) - journalfører ikke",
                oppgave.getOppgaveType(), oppgavereferanse);
            return;
        }

        PersonInfo person = hentPersonInfo(oppgave);

        OppgaveDokumentUtleder dokumentUtleder = OppgaveDokumentUtleder.finnUtleder(dokumentUtledere, oppgave.getOppgaveType());
        String dokumentTittel = dokumentUtleder.utledTittel(oppgave);
        Map<String, Object> oppgaveData = byggOppgaveData(dokumentUtleder, oppgave);

        String opprettetDato = oppgave.getOpprettetTidspunkt().toLocalDate().toString();
        byte[] pdf = pdfGenerator.genererPdf(new PdfDokument(dokumentUtleder.malnavn(), byggPdfData(dokumentTittel, opprettetDato, oppgaveData, person)));

        JournalføringParametre parametre = JournalføringParametre.utled(oppgave.getYtelsetype());
        Saksnummer saksnummer = hentSaksnummer(prosessTaskData);
        Sakstype sakstype = saksnummer != null ? Sakstype.FAGSAK : Sakstype.GENERELL_SAK;

        OpprettJournalpostRequest request = byggJournalpostRequest(oppgave, parametre, sakstype, saksnummer, person, dokumentTittel, pdf);

        OpprettJournalpostResponse response = dokarkivKlient.opprettJournalpost(request);

        // Journalposten er alltid opprettet i Dokarkiv på dette tidspunktet, men kan ha blitt
        // stående som MIDLERTIDIG dersom automatisk ferdigstilling feilet (f.eks. inaktivt tema).
        // Vi lagrer derfor kun raden - og anser oppgaven som journalført - når ferdigstillingen
        // faktisk lyktes. Ved journalpostferdigstilt=false kaster vi i stedet, slik at tasken kan
        // kjøres på nytt: en retry gjenbruker samme eksternReferanseId (ingen duplikat opprettes)
        // og Dokarkiv forsøker ferdigstilling på nytt for den eksisterende journalposten.
        if (!response.journalpostferdigstilt()) {
            throw new IllegalStateException(
                "Journalpost %s for oppgave %s ble opprettet, men ikke ferdigstilt: %s"
                    .formatted(response.journalpostId(), oppgavereferanse, response.melding()));
        }

        OppgaveJournalføringEntitet journalføring = new OppgaveJournalføringEntitet(
            oppgave, parametre.fagsaksystem(), saksnummer, new JournalpostId(response.journalpostId()));
        journalføringRepository.lagre(journalføring);

        log.info("Journalførte oppgave {} med journalpostId {}", oppgavereferanse, response.journalpostId());
    }

    @Override
    public Set<String> requiredProperties() {
        return Set.of(OPPGAVE_REFERANSE);
    }

    private static Saksnummer hentSaksnummer(ProsessTaskData prosessTaskData) {
        String saksnummer = prosessTaskData.getPropertyValue(SAKSNUMMER);
        return saksnummer != null ? new Saksnummer(saksnummer) : null;
    }


    private PersonInfo hentPersonInfo(BrukerdialogOppgaveEntitet oppgave) {
        return new PersonInfo(hentFødselsnummer(oppgave), hentNavn(oppgave));
    }

    /**
     * Kun gjeldende {@code FOLKEREGISTERIDENT}, uten historikk - unngår å sende et opphørt
     * fødselsnummer til arkivet.
     */
    private String hentFødselsnummer(BrukerdialogOppgaveEntitet oppgave) {
        return pdl.hentPersonIdentForAktørId(oppgave.getAktørId().getId())
            .orElseThrow(() -> new JournalføringException(
                "Fant ikke folkeregisterident for oppgave %s (oppgavetype %s)"
                    .formatted(oppgave.getOppgavereferanse(), oppgave.getOppgaveType())));
    }

    private String hentNavn(BrukerdialogOppgaveEntitet oppgave) {
        Behandlingsnummer behandlingsnummer = JournalføringParametre.utled(oppgave.getYtelsetype()).behandlingsnummer();
        var query = new HentPersonQueryRequest();
        query.setIdent(oppgave.getAktørId().getId()); // PDL godtar aktørId.
        var projection = new PersonResponseProjection()
            .navn(new NavnResponseProjection().fornavn().mellomnavn().etternavn());

        Person person = pdl.hentPerson(query, projection, List.of(behandlingsnummer));
        return person.getNavn().stream()
            .findFirst()
            .map(JournalførOppgaveTask::formaterNavn)
            .orElseThrow(() -> new JournalføringException(
                "Fant ikke navn for oppgave %s (oppgavetype %s)"
                    .formatted(oppgave.getOppgavereferanse(), oppgave.getOppgaveType())));
    }

    private static String formaterNavn(Navn navn) {
        return Stream.of(navn.getFornavn(), navn.getMellomnavn(), navn.getEtternavn())
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" "));
    }

    /**
     * Satt sentralt her for å unngå duplisering i alle {@link OppgaveDokumentUtleder}-implementasjonene.
     */
    private Map<String, Object> byggOppgaveData(OppgaveDokumentUtleder utleder, BrukerdialogOppgaveEntitet oppgave) {
        Map<String, Object> data = new LinkedHashMap<>(utleder.utledInnholdsdata(oppgave));
        data.putIfAbsent("oppgaveReferanse", oppgave.getOppgavereferanse().toString());
        return data;
    }

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
     * Journalpostens tittel ({@link JournalføringParametre#journalposttittel}) er bevisst
     * forskjellig fra {@code dokumentTittel} - journalposten får en generisk per-ytelse-tittel,
     * mens dokumentet beholder sin oppgavetype-spesifikke tittel (samme skille som
     * {@code k9-brukerdialog-prosessering} gjør).
     */
    private OpprettJournalpostRequest byggJournalpostRequest(BrukerdialogOppgaveEntitet oppgave,
                                                               JournalføringParametre parametre,
                                                               Sakstype sakstype,
                                                               Saksnummer saksnummer,
                                                               PersonInfo person,
                                                               String dokumentTittel,
                                                               byte[] pdf) {
        var bruker = new Bruker(person.fødselsnummer(), Bruker.BrukerIdType.FNR);
        // navn utelates bevisst - Dokarkiv slår selv opp navn i PDL.
        var avsenderMottaker = new OpprettJournalpostRequest.AvsenderMottaker(
            person.fødselsnummer(), null, null, OpprettJournalpostRequest.AvsenderMottaker.IdType.FNR);

        var sak = sakstype == Sakstype.FAGSAK
            ? OpprettJournalpostRequest.Sak.forSaksnummer(saksnummer.getVerdi(), parametre.fagsaksystem().name())
            : OpprettJournalpostRequest.Sak.GENERELL_FAGSAK;

        var dokument = new OpprettJournalpostRequest.Dokument(
            dokumentTittel,
            parametre.brevkode().getKode(),
            null,
            List.of(new OpprettJournalpostRequest.DokumentVariantArkivertPDFA(pdf))
        );

        String oppgavereferanse = oppgave.getOppgavereferanse().toString();
        return new OpprettJournalpostRequestBuilder()
            .medJournalpostType("UTGAAENDE")
            .medAvsenderMottaker(avsenderMottaker)
            .medBruker(bruker)
            .medTema(parametre.tema().name())
            .medTittel(parametre.journalposttittel().getTittel())
            .medJournalfoerendeEnhet(JOURNALFOERENDE_ENHET)
            .medEksternReferanseId(oppgavereferanse)
            .medTilleggsopplysninger(List.of(new OpprettJournalpostRequest.Tilleggsopplysning(TILLEGGSOPPLYSNING_NOKKEL, oppgavereferanse)))
            .medSak(sak)
            .medDokumenter(List.of(dokument))
            .build();
    }

    /**
     * Bevisst PII-fri {@code toString()} - fnr og navn skal aldri havne i logg.
     */
    private record PersonInfo(String fødselsnummer, String navn) {
        @Override
        public String toString() {
            return "PersonInfo{<PII utelatt>}";
        }
    }
}
