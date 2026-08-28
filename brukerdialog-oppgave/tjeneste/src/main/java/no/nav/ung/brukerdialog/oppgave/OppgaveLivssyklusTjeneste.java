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
     * Speiler den kommenterte {@code @AssertTrue}-valideringen i {@code OpprettOppgaveDto}
     * (kontrakt-modulen) - kan ikke dele konstanten på tvers av modulgrensen. Hold i sync hvis
     * regelen endres.
     */
    private static final Set<OppgaveType> UTEN_FAGSAK = EnumSet.of(OppgaveType.SØK_YTELSE);

    private ProsessTaskTjeneste prosessTaskTjeneste;
    private BrukerdialogOppgaveRepository brukerdialogOppgaveRepository;
    private Instance<OppgavelInnholdUtleder> varselInnholdUtledere;
    private Instance<OppgaveDataMapperFraDtoTilEntitet> oppgaveDataMapper;

    public OppgaveLivssyklusTjeneste() {
    }

    @Inject
    public OppgaveLivssyklusTjeneste(ProsessTaskTjeneste prosessTaskTjeneste,
                                     BrukerdialogOppgaveRepository brukerdialogOppgaveRepository,
                                     @Any Instance<OppgavelInnholdUtleder> varselInnholdUtledere,
                                     @Any Instance<OppgaveDataMapperFraDtoTilEntitet> oppgaveDataMapper) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
        this.brukerdialogOppgaveRepository = brukerdialogOppgaveRepository;
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
     * Alt skjer i én transaksjon (transactional outbox): committes sammen, eller ingen av dem.
     *
     * @param journalføring Kan være {@code null} - behandles da som om {@code saksnummer} mangler.
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
        opprettTaskForJournalføringHvisAktuelt(oppgaveEntitet, journalføring);
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
     * {@code saksnummer} sendes med som en task-property siden den ikke er utledbar fra oppgaven
     * selv.
     */
    private void opprettTaskForJournalføringHvisAktuelt(BrukerdialogOppgaveEntitet oppgaveEntitet, JournalføringDto journalføring) {
        Saksnummer saksnummer = journalføring != null ? journalføring.saksnummer() : null;
        boolean skalJournalføres = saksnummer != null || UTEN_FAGSAK.contains(oppgaveEntitet.getOppgaveType());
        if (!skalJournalføres) {
            logger.warn("Oppretter ikke journalføringstask: saksnummer mangler for oppgavetype {} som krever fagsak ved journalføring. oppgaveReferanse={}",
                oppgaveEntitet.getOppgaveType(), oppgaveEntitet.getOppgavereferanse());
            return;
        }

        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(JournalførOppgaveTask.class);
        prosessTaskData.setProperty(JournalførOppgaveTask.OPPGAVE_REFERANSE, oppgaveEntitet.getOppgavereferanse().toString());
        if (saksnummer != null) {
            prosessTaskData.setProperty(JournalførOppgaveTask.SAKSNUMMER, saksnummer.getVerdi());
        }
        prosessTaskTjeneste.lagre(prosessTaskData);
    }

}
