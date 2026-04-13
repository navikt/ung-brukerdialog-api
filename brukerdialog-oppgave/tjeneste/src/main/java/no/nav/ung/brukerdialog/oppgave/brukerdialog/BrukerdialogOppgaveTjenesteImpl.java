package no.nav.ung.brukerdialog.oppgave.brukerdialog;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveResponsDto;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveMapper;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.OppgaveLivssyklusTjeneste;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.oppgave.saksbehandling.OppgaveForSaksbehandlingTjenesteImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tjeneste for å hente og vise brukerdialog-oppgaver.
 * Brukes primært av REST-tjenester for å hente oppgaver til visning.
 *
 * For å opprette og administrere oppgaver, bruk {@link OppgaveForSaksbehandlingTjenesteImpl}.
 */
@ApplicationScoped
public class BrukerdialogOppgaveTjenesteImpl implements BrukerdialogOppgaveTjeneste {

    private OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste;
    private BrukerdialogOppgaveRepository repository;
    private BrukerdialogOppgaveMapper mapper;

    public BrukerdialogOppgaveTjenesteImpl() {
        // CDI proxy
    }

    @Inject
    public BrukerdialogOppgaveTjenesteImpl(OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste, BrukerdialogOppgaveRepository repository, BrukerdialogOppgaveMapper mapper) {
        this.oppgaveLivssyklusTjeneste = oppgaveLivssyklusTjeneste;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<BrukerdialogOppgaveDto> hentAlleOppgaverForAktør(AktørId aktørId, OppgaveYtelsetype ytelsetype) {
        return repository.hentAlleOppgaverForAktør(aktørId, ytelsetype).stream()
            .map(mapper::tilDto)
            .collect(Collectors.toList());
    }

    @Override
    public BrukerdialogOppgaveDto hentOppgaveForOppgavereferanse(UUID oppgavereferanse, AktørId aktørId) {
        var oppgave = repository.hentOppgaveForOppgavereferanse(oppgavereferanse, aktørId)
            .orElseThrow(() -> new IllegalArgumentException("Fant ikke oppgave med oppgaveReferanse: " + oppgavereferanse));
        return mapper.tilDto(oppgave);
    }

    @Override
    public BrukerdialogOppgaveDto løsOppgave(UUID oppgavereferanse, AktørId aktørId, Optional<OppgaveResponsDto> oppgaveResponsDto) {
        var oppgave = repository.hentOppgaveForOppgavereferanse(oppgavereferanse, aktørId)
            .orElseThrow(() -> new IllegalArgumentException("Fant ikke oppgave med oppgaveReferanse: " + oppgavereferanse));
        var oppdatertOppgave = oppgaveLivssyklusTjeneste.løsOppgave(oppgave, oppgaveResponsDto);
        return mapper.tilDto(oppdatertOppgave);
    }
}

