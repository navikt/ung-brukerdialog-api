package no.nav.ung.brukerdialog.oppgave.saksbehandling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.EndreOppgaveStatusDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OpprettOppgaveDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.OppgaveForSaksbehandlingTjeneste;
import no.nav.ung.brukerdialog.oppgave.OppgaveLivssyklusTjeneste;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.InntektsrapporteringOppgaveDataEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OppgaveForSaksbehandlingTjenesteImpl implements OppgaveForSaksbehandlingTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(OppgaveForSaksbehandlingTjenesteImpl.class);

    private BrukerdialogOppgaveRepository repository;
    private OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste;

    public OppgaveForSaksbehandlingTjenesteImpl() {
        // CDI proxy
    }

    @Inject
    public OppgaveForSaksbehandlingTjenesteImpl(BrukerdialogOppgaveRepository repository,
                                                OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste) {
        this.repository = repository;
        this.oppgaveLivssyklusTjeneste = oppgaveLivssyklusTjeneste;
    }

    @Override
    public void opprettOppgave(OpprettOppgaveDto oppgave) {
        BrukerdialogOppgaveEntitet oppgaveEntitet = new BrukerdialogOppgaveEntitet(
            oppgave.oppgaveReferanse(),
            oppgave.oppgavetypeData().oppgavetype(),
            oppgave.aktørId(),
            oppgave.ytelsetype(),
            oppgave.frist());
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
            .filter(o -> matcherPeriodeEllerHarIkkePeriode(o, dto))
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
            .filter(o -> matcherPeriodeEllerHarIkkePeriode(o, dto))
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
        søkYtelseOppgaver.forEach(o -> oppgaveLivssyklusTjeneste.løsOppgave(o, Optional.empty()));
    }


    @Override
    public void endreFrist(AktørId aktørId, UUID eksternReferanse, LocalDateTime frist) {
        repository.endreFrist(eksternReferanse, aktørId, frist);
    }


    /**
     * Sjekker om en oppgave matcher periodekravet i DTO-en, eller om oppgavetype ikke krever periode.
     *
     * <p>Oppgavetyper er klassifisert som enten periodebaserte (f.eks. RAPPORTER_INNTEKT som krever
     * fomDato/tomDato) eller ikke-periodebaserte (f.eks. SØK_YTELSE som ikke har periode).
     *
     * <p>Logikken er som følger:
     * <ul>
     *   <li>Hvis oppgavetype ikke krever periode ({@link OppgaveType#kreverPeriode()} == false),
     *       returneres alltid {@code true} uavhengig av oppgavens innhold.</li>
     *   <li>Hvis oppgavetype krever periode, må oppgavens lagrede periode (fomDato/tomDato) eksakt
     *       matche DTO-ens fomDato og tomDato.</li>
     *   <li>Hvis oppgavetype krever periode, men oppgavedata er av uventet type, returneres {@code false}.</li>
     * </ul>
     *
     * @param oppgave oppgaveentiteten som skal sjekkes
     * @param dto DTO-en som inneholder oppgavetype og eventuelle periodedata
     * @return {@code true} hvis oppgaven matcher periodekravet; {@code false} ellers
     */
    private boolean matcherPeriodeEllerHarIkkePeriode(BrukerdialogOppgaveEntitet oppgave,
                                        EndreOppgaveStatusDto dto) {
        if (!dto.oppgavetype().kreverPeriode()) {
            return true;
        }
        if (oppgave.getOppgaveData() instanceof InntektsrapporteringOppgaveDataEntitet data) {
            return data.getFraOgMed().equals(dto.fomDato()) && data.getTilOgMed().equals(dto.tomDato());
        }
        return false;
    }
}
