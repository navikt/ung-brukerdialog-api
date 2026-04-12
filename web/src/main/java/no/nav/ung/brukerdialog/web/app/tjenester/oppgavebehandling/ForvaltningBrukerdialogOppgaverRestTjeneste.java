package no.nav.ung.brukerdialog.web.app.tjenester.oppgavebehandling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
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
import no.nav.ung.brukerdialog.kontrakt.oppgaver.diagnostikk.ForvaltningSettLøstDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.OppgaveLivssyklusTjeneste;
import no.nav.ung.brukerdialog.oppgave.diagnostikk.DiagnostikkOppgaveLogg;
import no.nav.ung.brukerdialog.web.server.abac.AbacAttributtSupplier;

import java.util.Optional;

/**
 * REST-tjeneste for forvaltning av brukerdialogoppgaver.
 * Tilgjengelig kun for driftstilgang.
 */
@Path("/forvaltning/oppgave")
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Forvaltning", description = "API for forvaltning av brukerdialog oppgaver")
public class ForvaltningBrukerdialogOppgaverRestTjeneste {

    private OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste;
    private BrukerdialogOppgaveRepository repository;
    private EntityManager entityManager;

    public ForvaltningBrukerdialogOppgaverRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public ForvaltningBrukerdialogOppgaverRestTjeneste(
        OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste, BrukerdialogOppgaveRepository repository,
        EntityManager entityManager) {
        this.oppgaveLivssyklusTjeneste = oppgaveLivssyklusTjeneste;
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @POST
    @Path("/los")

    @Operation(
        summary = "Setter oppgave til løst og setter respons",
        description = "Setter oppgave til løst med oppgitt respons"
    )
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.DRIFT)
    public Response løsOppgave(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class) ForvaltningSettLøstDto requestDto) {
        entityManager.persist(new DiagnostikkOppgaveLogg(requestDto.oppgaveReferanse(), "/forvaltning/oppgave/los", requestDto.begrunnelse()));
        entityManager.flush();
        var oppgave = repository.hentOppgaveForOppgavereferanse(requestDto.oppgaveReferanse())
            .orElseThrow(() -> new IllegalStateException("Forventer å finne oppgave"));
        oppgaveLivssyklusTjeneste.løsOppgave(oppgave, Optional.of(requestDto.respons()));
        return Response.ok().build();
    }
}

