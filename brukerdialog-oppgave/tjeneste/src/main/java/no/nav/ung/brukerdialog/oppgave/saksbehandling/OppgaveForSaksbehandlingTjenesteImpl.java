package no.nav.ung.brukerdialog.oppgave.saksbehandling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.OppgaveForSaksbehandlingTjeneste;
import no.nav.ung.brukerdialog.oppgave.OppgaveLivssyklusTjeneste;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.EndreOppgaveStatusDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OpprettOppgaveDto;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.InntektsrapporteringOppgaveDataEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OppgaveForSaksbehandlingTjenesteImpl implements OppgaveForSaksbehandlingTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(OppgaveForSaksbehandlingTjenesteImpl.class);

    private BrukerdialogOppgaveRepository repository;
    private OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste;
    private boolean isEnabled;

    public OppgaveForSaksbehandlingTjenesteImpl() {
        // CDI proxy
    }

    @Inject
    public OppgaveForSaksbehandlingTjenesteImpl(BrukerdialogOppgaveRepository repository,
                                                OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste,
                                                @KonfigVerdi(value = "OPPGAVER_I_UNGBRUKERDIALOG_ENABLED", defaultVerdi = "true") boolean oppgaverIUngsakEnabled) {
        this.repository = repository;
        this.oppgaveLivssyklusTjeneste = oppgaveLivssyklusTjeneste;
        this.isEnabled = oppgaverIUngsakEnabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void opprettOppgave(OpprettOppgaveDto oppgave) {
        BrukerdialogOppgaveEntitet oppgaveEntitet = new BrukerdialogOppgaveEntitet(
            oppgave.oppgaveReferanse(), oppgave.oppgavetypeData().oppgavetype(), oppgave.aktørId(), oppgave.frist());
        oppgaveLivssyklusTjeneste.opprettOppgave(oppgaveEntitet, oppgave.oppgavetypeData());
    }

    @Override
    public void avbrytOppgave(UUID eksternRef) {
        var oppgave = repository.hentOppgaveForOppgavereferanse(eksternRef)
            .orElseThrow(() -> new IllegalArgumentException("Fant ikke oppgave med oppgaveReferanse: " + eksternRef));
        oppgaveLivssyklusTjeneste.avbrytOppgave(oppgave);
    }

    @Override
    public void oppgaveUtløpt(UUID eksternRef) {
        var oppgave = repository.hentOppgaveForOppgavereferanse(eksternRef)
            .orElseThrow(() -> new IllegalArgumentException("Fant ikke oppgave med oppgaveReferanse: " + eksternRef));
        oppgaveLivssyklusTjeneste.utløpOppgave(oppgave);
    }

    @Override
    public void settOppgaveTilUtløpt(EndreOppgaveStatusDto dto) {
        logger.info("Utløper oppgave av type: {} med periode [{} - {}]", dto.oppgavetype(), dto.fomDato(), dto.tomDato());
        var aktørId = dto.aktørId();
        repository.hentAlleOppgaverForAktør(aktørId).stream()
            .filter(o -> o.getStatus() == OppgaveStatus.ULØST)
            .filter(o -> o.getOppgaveType() == dto.oppgavetype())
            .filter(o -> gjelderSammePeriodeForInntektsrapportering(o, dto))
            .findFirst()
            .ifPresentOrElse(
                oppgave -> {
                    logger.info("Setter oppgave {} til utløpt", oppgave.getOppgavereferanse());
                    oppgaveLivssyklusTjeneste.utløpOppgave(oppgave);
                },
                () -> logger.info("Fant ingen uløst oppgave av type {} for periode [{} - {}]",
                    dto.oppgavetype(), dto.fomDato(), dto.tomDato())
            );
    }

    @Override
    public void settOppgaveTilAvbrutt(EndreOppgaveStatusDto dto) {
        logger.info("Avbryter oppgave av type: {} med periode [{} - {}]", dto.oppgavetype(), dto.fomDato(), dto.tomDato());
        var aktørId = dto.aktørId();
        repository.hentAlleOppgaverForAktør(aktørId).stream()
            .filter(o -> o.getStatus() == OppgaveStatus.ULØST)
            .filter(o -> o.getOppgaveType() == dto.oppgavetype())
            .filter(o -> gjelderSammePeriodeForInntektsrapportering(o, dto))
            .findFirst()
            .ifPresentOrElse(
                oppgave -> {
                    logger.info("Setter oppgave {} til avbrutt", oppgave.getOppgavereferanse());
                    oppgaveLivssyklusTjeneste.avbrytOppgave(oppgave);
                },
                () -> logger.info("Fant ingen uløst oppgave av type {} for periode [{} - {}]",
                    dto.oppgavetype(), dto.fomDato(), dto.tomDato())
            );
    }

    @Override
    public void løsSøkYtelseOppgave(AktørId aktørId) {
        List<BrukerdialogOppgaveEntitet> søkYtelseOppgaver = repository.hentOppgaveForType(
            OppgaveType.SØK_YTELSE, OppgaveStatus.ULØST, aktørId);
        if (søkYtelseOppgaver.size() > 1) {
            logger.warn("Fant flere enn én uløst søk-ytelse-oppgave. Antall: {}", søkYtelseOppgaver.size());
        }
        søkYtelseOppgaver.forEach(oppgaveLivssyklusTjeneste::løsOppgave);
    }

    @Override
    public void endreFrist(AktørId aktørId, UUID eksternReferanse, LocalDateTime frist) {
        repository.endreFrist(eksternReferanse, aktørId, frist);
    }


    // Vurder om denne logikken skal ligge i denne tjenesten eller håndteres av konsument
    private boolean gjelderSammePeriodeForInntektsrapportering(BrukerdialogOppgaveEntitet oppgave,
                                                               EndreOppgaveStatusDto dto) {
        if (oppgave.getOppgaveData() instanceof InntektsrapporteringOppgaveDataEntitet data) {
            return data.getFraOgMed().equals(dto.fomDato()) && data.getTilOgMed().equals(dto.tomDato());
        }
        return false;
    }
}
