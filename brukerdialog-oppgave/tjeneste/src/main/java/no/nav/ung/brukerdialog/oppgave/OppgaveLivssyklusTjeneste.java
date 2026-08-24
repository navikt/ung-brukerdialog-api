package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;
import no.nav.ung.brukerdialog.DeaktiverMinSideVarselTask;
import no.nav.ung.brukerdialog.PubliserMinSideVarselTask;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveResponsDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.journalforing.JournalføringDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.EndretPeriodeDataDto;
import no.nav.ung.brukerdialog.oppgave.journalforing.JournalførOppgaveTask;
import no.nav.ung.brukerdialog.oppgave.journalforing.JournalføringKonfig;
import no.nav.ung.brukerdialog.oppgave.journalforing.JournalføringParametre;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveJournalføringEntitet;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveJournalføringRepository;
import no.nav.ung.brukerdialog.oppgave.journalforing.Sakstype;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class OppgaveLivssyklusTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(OppgaveLivssyklusTjeneste.class);

    /**
     * Oppgavetyper uten fagsak ved opprettelse - kan journalføres på generell sak.
     * Speiler {@code GyldigJournalføringValidator.UTEN_FAGSAK} i kontrakt-modulen. De to kan
     * ikke dele én konstant på tvers av modulgrensen (pakke-privat synlighet, og tjeneste skal
     * ikke ta en kompileringsavhengighet til en validator kun for én konstant) - hold dem i sync
     * hvis regelen endres.
     */
    private static final Set<OppgaveType> UTEN_FAGSAK = EnumSet.of(OppgaveType.SØK_YTELSE);

    private ProsessTaskTjeneste prosessTaskTjeneste;
    private BrukerdialogOppgaveRepository brukerdialogOppgaveRepository;
    private OppgaveJournalføringRepository oppgaveJournalføringRepository;
    private JournalføringKonfig journalføringKonfig;
    private Instance<OppgavelInnholdUtleder> varselInnholdUtledere;
    private Instance<OppgaveDataMapperFraDtoTilEntitet> oppgaveDataMapper;

    public OppgaveLivssyklusTjeneste() {
    }

    @Inject
    public OppgaveLivssyklusTjeneste(ProsessTaskTjeneste prosessTaskTjeneste,
                                     BrukerdialogOppgaveRepository brukerdialogOppgaveRepository,
                                     OppgaveJournalføringRepository oppgaveJournalføringRepository,
                                     JournalføringKonfig journalføringKonfig,
                                     @Any Instance<OppgavelInnholdUtleder> varselInnholdUtledere,
                                     @Any Instance<OppgaveDataMapperFraDtoTilEntitet> oppgaveDataMapper) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
        this.brukerdialogOppgaveRepository = brukerdialogOppgaveRepository;
        this.oppgaveJournalføringRepository = oppgaveJournalføringRepository;
        this.journalføringKonfig = journalføringKonfig;
        this.varselInnholdUtledere = varselInnholdUtledere;
        this.oppgaveDataMapper = oppgaveDataMapper;
    }

    /**
     * Løser en oppgave og oppdaterer status.
     * Deaktiverer varsel på Min Side og setter oppgaven til LØST status med løst-dato.
     *
     * @param oppgaveEntitet Oppgaven som skal løses
     * @return
     */
    public BrukerdialogOppgaveEntitet løsOppgave(BrukerdialogOppgaveEntitet oppgaveEntitet, Optional<OppgaveResponsDto> responsDto) {
        logger.info("Løser oppgave: oppgaveType={}, oppgaveReferanse={}", oppgaveEntitet.getOppgaveType(), oppgaveEntitet.getOppgavereferanse());
        opprettTaskForDeaktiveringAvVarsel(oppgaveEntitet);
        oppgaveEntitet.løs(responsDto.orElse(null));
        brukerdialogOppgaveRepository.oppdater(oppgaveEntitet);
        return oppgaveEntitet;
    }

    /**
     * Markerer en oppgave som utløpt.
     * Deaktiverer varsel på Min Side og setter oppgaven til UTLØPT status.
     * Brukes når fristen for oppgaven har gått ut.
     *
     * @param oppgaveEntitet Oppgaven som skal markeres som utløpt
     */
    public void utløpOppgave(BrukerdialogOppgaveEntitet oppgaveEntitet) {
        logger.info("Utløper oppgave: oppgaveType={}, oppgaveReferanse={}", oppgaveEntitet.getOppgaveType(), oppgaveEntitet.getOppgavereferanse());
        opprettTaskForDeaktiveringAvVarsel(oppgaveEntitet);
        oppgaveEntitet.utløp();
        brukerdialogOppgaveRepository.oppdater(oppgaveEntitet);
    }

    /**
     * Avbryter en oppgave.
     * Deaktiverer varsel på Min Side og setter oppgaven til AVBRUTT status.
     * Brukes når oppgaven ikke lenger er relevant eller skal kanselleres.
     *
     * @param oppgaveEntitet Oppgaven som skal avbryttes
     */
    public void avbrytOppgave(BrukerdialogOppgaveEntitet oppgaveEntitet) {
        logger.info("Avbryter oppgave: oppgaveType={}, oppgaveReferanse={}", oppgaveEntitet.getOppgaveType(), oppgaveEntitet.getOppgavereferanse());
        opprettTaskForDeaktiveringAvVarsel(oppgaveEntitet);
        oppgaveEntitet.avbryt();
        brukerdialogOppgaveRepository.oppdater(oppgaveEntitet);
    }

    /**
     * Persisterer oppgave, oppretter journalføring og publiserer varsel til Min Side.
     * <p>
     * Alt i denne metoden skjer i samme transaksjon (transactional outbox): enten
     * committes oppgaven sammen med journalføringsraden og begge prosesstaskene, eller ingen av
     * dem. Ingen del av dette skal derfor kjøres i en egen transaksjon (f.eks. via
     * {@code REQUIRES_NEW}) - det ville enten kunne gitt en committet oppgave uten
     * journalføringsrad, eller en foreldreløs task som peker på en oppgave som ble rullet
     * tilbake.
     *
     * @param oppgaveEntitet  Oppgave som skal opprettes og publiseres.
     * @param oppgavetypeData
     * @param journalføring   Journalføringsrelaterte felter fra requestbody (kan være
     *                        {@code null} - da behandles det som om {@code fagsakId} mangler).
     */
    public void opprettOppgave(BrukerdialogOppgaveEntitet oppgaveEntitet, OppgavetypeDataDto oppgavetypeData, JournalføringDto journalføring) {
        if (oppgaveEntitet.getId() != null) {
            throw new IllegalArgumentException("Oppgave er allerede persistert med id: " + oppgaveEntitet.getId());
        }
        if (oppgavetypeData instanceof EndretPeriodeDataDto endretPeriodeData) {
            logger.info("Oppretter oppgave: oppgaveType={}, oppgaveReferanse={}, ytelsetype={}, frist={}, endringer={}",
                oppgaveEntitet.getOppgaveType(), oppgaveEntitet.getOppgavereferanse(),
                oppgaveEntitet.getYtelsetype(), oppgaveEntitet.getFristTid(),
                endretPeriodeData.endringer());
        } else {
            logger.info("Oppretter oppgave: oppgaveType={}, oppgaveReferanse={}, ytelsetype={}, frist={}",
                oppgaveEntitet.getOppgaveType(), oppgaveEntitet.getOppgavereferanse(),
                oppgaveEntitet.getYtelsetype(), oppgaveEntitet.getFristTid());
        }
        var oppgaveData = OppgaveDataMapperFraDtoTilEntitet.finnTjeneste(oppgaveDataMapper, oppgaveEntitet.getOppgaveType()).map(oppgavetypeData);
        oppgaveEntitet.setOppgaveData(oppgaveData);
        brukerdialogOppgaveRepository.lagre(oppgaveEntitet);
        opprettTaskForPubliseringAvVarsel(oppgaveEntitet);
        opprettJournalføring(oppgaveEntitet, journalføring);
    }

    private void opprettTaskForPubliseringAvVarsel(BrukerdialogOppgaveEntitet oppgaveEntitet) {
        OppgavelInnholdUtleder oppgavelInnholdUtleder = OppgavelInnholdUtleder.finnUtleder(varselInnholdUtledere, oppgaveEntitet.getOppgaveType());
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(PubliserMinSideVarselTask.class);
        prosessTaskData.setProperty(PubliserMinSideVarselTask.OPPGAVE_REFERANSE, oppgaveEntitet.getOppgavereferanse().toString());
        prosessTaskData.setProperty(ProsessTaskData.AKTØR_ID, oppgaveEntitet.getAktørId().getId());
        prosessTaskData.setProperty(PubliserMinSideVarselTask.VARSEL_TEKST, oppgavelInnholdUtleder.utledVarselTekst(oppgaveEntitet));
        prosessTaskData.setProperty(PubliserMinSideVarselTask.VARSEL_LENKE, oppgavelInnholdUtleder.utledVarselLenke(oppgaveEntitet));
        prosessTaskTjeneste.lagre(prosessTaskData);
    }

    private void opprettTaskForDeaktiveringAvVarsel(BrukerdialogOppgaveEntitet oppgaveEntitet) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(DeaktiverMinSideVarselTask.class);
        prosessTaskData.setProperty(DeaktiverMinSideVarselTask.OPPGAVE_REFERANSE, oppgaveEntitet.getOppgavereferanse().toString());
        prosessTaskTjeneste.lagre(prosessTaskData);
    }

    /**
     * Oppretter journalføringsrad og eventuelt {@code JournalførOppgaveTask} for en nyopprettet
     * oppgave. Raden lagres ALLTID når oppgaven skal journalføres, uavhengig av
     * {@link JournalføringKonfig} - det er kun tasken som er betinget av konfigurasjonen, slik at
     * etterslepet (rader uten task) blir komplett og spørrbart.
     */
    private void opprettJournalføring(BrukerdialogOppgaveEntitet oppgaveEntitet, JournalføringDto journalføring) {
        Saksnummer fagsakId = journalføring != null ? journalføring.fagsakId() : null;
        boolean skalJournalføres = fagsakId != null || UTEN_FAGSAK.contains(oppgaveEntitet.getOppgaveType());
        if (!skalJournalføres) {
            // fagsakId er foreløpig valgfri for alle typer. Mangler den for en type som krever
            // fagsak, opprettes ingen journalføringsrad ennå - dette blir en høylytt 400 når
            // valideringen strammes inn, når ung-sak garantert sender feltet.
            logger.warn("Oppretter ikke journalføring: fagsakId mangler for oppgavetype {} som krever fagsak ved journalføring. oppgaveReferanse={}",
                oppgaveEntitet.getOppgaveType(), oppgaveEntitet.getOppgavereferanse());
            return;
        }

        JournalføringParametre parametre = JournalføringParametre.utled(oppgaveEntitet.getYtelsetype());
        Sakstype sakstype = fagsakId != null ? Sakstype.FAGSAK : Sakstype.GENERELL_SAK;
        OppgaveJournalføringEntitet journalføringEntitet = new OppgaveJournalføringEntitet(
            oppgaveEntitet, parametre.tema(), parametre.fagsaksystem(), sakstype, fagsakId);
        oppgaveJournalføringRepository.lagre(journalføringEntitet);

        if (journalføringKonfig.erAktivertFor(oppgaveEntitet.getOppgaveType())) {
            opprettTaskForJournalføring(oppgaveEntitet);
        } else {
            logger.info("Journalføring er ikke aktivert for oppgavetype {} - oppretter ikke JournalførOppgaveTask. oppgaveReferanse={}",
                oppgaveEntitet.getOppgaveType(), oppgaveEntitet.getOppgavereferanse());
        }
    }

    private void opprettTaskForJournalføring(BrukerdialogOppgaveEntitet oppgaveEntitet) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(JournalførOppgaveTask.class);
        prosessTaskData.setProperty(JournalførOppgaveTask.OPPGAVE_REFERANSE, oppgaveEntitet.getOppgavereferanse().toString());
        prosessTaskTjeneste.lagre(prosessTaskData);
    }

}
