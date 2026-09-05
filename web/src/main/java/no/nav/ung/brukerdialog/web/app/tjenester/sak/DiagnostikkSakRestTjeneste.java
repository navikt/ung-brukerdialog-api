package no.nav.ung.brukerdialog.web.app.tjenester.sak;

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
import no.nav.ung.brukerdialog.kontrakt.sak.diagnostikk.DiagnostikkSakRequestDto;
import no.nav.ung.brukerdialog.sak.diagnostikk.DiagnostikkSakLogg;
import no.nav.ung.brukerdialog.sak.diagnostikk.DiagnostikkSakTjeneste;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.web.server.abac.AbacAttributtSupplier;

import java.util.Optional;

@Path(DiagnostikkSakRestTjeneste.BASE_PATH)
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Forvaltning", description = "API for diagnostikk av brukerdialog-sak")
public class DiagnostikkSakRestTjeneste {

    static final String BASE_PATH = "/forvaltning/sak/diagnostikk";

    private DiagnostikkSakTjeneste diagnostikkSakTjeneste;
    private EntityManager entityManager;

    public DiagnostikkSakRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public DiagnostikkSakRestTjeneste(DiagnostikkSakTjeneste diagnostikkSakTjeneste, EntityManager entityManager) {
        this.diagnostikkSakTjeneste = diagnostikkSakTjeneste;
        this.entityManager = entityManager;
    }

    @POST
    @Operation(
        summary = "Dumper alle rader i brukerdialog-sak for en deltaker",
        description = "Slår opp på aktørId eller saksnummer. Logger aksess i DIAGNOSTIKK_SAK_LOGG."
    )
    @BeskyttetRessurs(action = BeskyttetRessursActionType.READ, resource = BeskyttetRessursResourceType.DRIFT)
    public Response hentDiagnostikk(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class) DiagnostikkSakRequestDto requestDto) {

        Optional<AktørId> aktørId = requestDto.aktørId() != null
            ? Optional.of(requestDto.aktørId())
            : diagnostikkSakTjeneste.finnAktørForSaksnummer(requestDto.saksnummer());

        entityManager.persist(new DiagnostikkSakLogg(aktørId.orElse(null), requestDto.saksnummer(), BASE_PATH, requestDto.begrunnelse()));
        entityManager.flush();

        return aktørId
            .map(diagnostikkSakTjeneste::dump)
            .map(Response::ok)
            .map(Response.ResponseBuilder::build)
            .orElseGet(() -> Response.noContent().build());
    }
}
