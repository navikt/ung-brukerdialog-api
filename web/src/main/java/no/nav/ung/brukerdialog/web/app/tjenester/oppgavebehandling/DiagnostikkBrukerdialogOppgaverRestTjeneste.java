package no.nav.ung.brukerdialog.web.app.tjenester.oppgavebehandling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionType;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursResourceType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveEksternReferanseDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.diagnostikk.DiagnostikkBrukerdialogOppgaveDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.diagnostikk.DiagnostikkBrukerdialogOppgaveMapper;
import no.nav.ung.brukerdialog.web.server.abac.AbacAttributtSupplier;

import java.util.Optional;

/**
 * REST-tjeneste for diagnostikk av brukerdialogoppgaver.
 * Tilgjengelig kun for driftstilgang.
 */
@Path("/forvaltning/oppgave/diagnostikk")
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Forvaltning", description = "API for diagnostikk av brukerdialog oppgaver")
public class DiagnostikkBrukerdialogOppgaverRestTjeneste {

    private BrukerdialogOppgaveRepository repository;
    private DiagnostikkBrukerdialogOppgaveMapper oppgaveMapper;

    public DiagnostikkBrukerdialogOppgaverRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public DiagnostikkBrukerdialogOppgaverRestTjeneste(
        BrukerdialogOppgaveRepository repository,
        DiagnostikkBrukerdialogOppgaveMapper oppgaveMapper) {
        this.repository = repository;
        this.oppgaveMapper = oppgaveMapper;
    }

    @POST
    @Operation(
        summary = "Henter oppgave for bruk til diagnostikk",
        description = "Henter oppgave for diagnostikkformål."
    )
    @BeskyttetRessurs(action = BeskyttetRessursActionType.READ, resource = BeskyttetRessursResourceType.DRIFT)
    public Response hentDiagnostikk(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class) OppgaveEksternReferanseDto eksternReferanseDto) {
        Optional<BrukerdialogOppgaveEntitet> oppgave = repository.hentOppgaveForOppgavereferanse(eksternReferanseDto.oppgaveReferanse());
        Optional<DiagnostikkBrukerdialogOppgaveDto> mappetOppgave = oppgave.map(oppgaveMapper::tilDto);
        return mappetOppgave.map(Response::ok)
            .map(Response.ResponseBuilder::build)
            .orElse(Response.noContent().build());
    }
}

