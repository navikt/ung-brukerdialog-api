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
import no.nav.k9.felles.integrasjon.pdl.PdlKlient;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionType;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursResourceType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.ung.brukerdialog.kontrakt.FeilDto;
import no.nav.ung.brukerdialog.kontrakt.FeilType;
import no.nav.ung.brukerdialog.kontrakt.sak.mikrofrontend.MikrofrontendForvaltningRequest;
import no.nav.ung.brukerdialog.kontrakt.sak.mikrofrontend.MikrofrontendForvaltningResultat;
import no.nav.ung.brukerdialog.sak.diagnostikk.DiagnostikkSakLogg;
import no.nav.ung.brukerdialog.sak.mikrofrontend.MikrofrontendId;
import no.nav.ung.brukerdialog.sak.mikrofrontend.MinSideMikrofrontendTjeneste;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.web.server.abac.AbacAttributtSupplier;

import java.util.Optional;
import java.util.function.BiFunction;

@Path(MikrofrontendForvaltningRestTjeneste.BASE_PATH)
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Forvaltning", description = "API for forvaltning av inngangen til aktivitetspenger på Min side")
public class MikrofrontendForvaltningRestTjeneste {

    static final String BASE_PATH = "/forvaltning/sak/mikrofrontend/aktivitetspenger";
    private static final String AKTIVER_PATH = "/aktiver";
    private static final String DEAKTIVER_PATH = "/deaktiver";

    private MinSideMikrofrontendTjeneste mikrofrontendTjeneste;
    private PdlKlient pdl;
    private EntityManager entityManager;

    public MikrofrontendForvaltningRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public MikrofrontendForvaltningRestTjeneste(MinSideMikrofrontendTjeneste mikrofrontendTjeneste, PdlKlient pdl, EntityManager entityManager) {
        this.mikrofrontendTjeneste = mikrofrontendTjeneste;
        this.pdl = pdl;
        this.entityManager = entityManager;
    }

    @POST
    @Path(AKTIVER_PATH)
    @Operation(
        summary = "Aktiverer inngangen til aktivitetspenger på Min side for en bruker",
        description = "Brukes til etterfylling. Gjør ingenting hvis inngangen allerede er aktivert. Svarer 404 hvis fødselsnummeret ikke finnes i PDL. Logger aksess i DIAGNOSTIKK_SAK_LOGG."
    )
    @BeskyttetRessurs(action = BeskyttetRessursActionType.CREATE, resource = BeskyttetRessursResourceType.DRIFT)
    public Response aktiver(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class) MikrofrontendForvaltningRequest request) {

        return utfør(request, AKTIVER_PATH, mikrofrontendTjeneste::aktiver);
    }

    @POST
    @Path(DEAKTIVER_PATH)
    @Operation(
        summary = "Deaktiverer inngangen til aktivitetspenger på Min side for en bruker",
        description = "Eneste måte å deaktivere inngangen på. Gjør ingenting hvis inngangen ikke er aktivert. Svarer 404 hvis fødselsnummeret ikke finnes i PDL. Logger aksess i DIAGNOSTIKK_SAK_LOGG."
    )
    @BeskyttetRessurs(action = BeskyttetRessursActionType.CREATE, resource = BeskyttetRessursResourceType.DRIFT)
    public Response deaktiver(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class) MikrofrontendForvaltningRequest request) {

        return utfør(request, DEAKTIVER_PATH, mikrofrontendTjeneste::deaktiver);
    }

    private Response utfør(MikrofrontendForvaltningRequest request, String path,
                           BiFunction<AktørId, MikrofrontendId, Boolean> handling) {
        Optional<AktørId> aktørId = pdl.hentAktørIdForPersonIdent(request.fnr().getIdent(), false).map(AktørId::new);

        // Fødselsnummeret lagres ikke. Aksessloggen får aktørId når den finnes.
        entityManager.persist(new DiagnostikkSakLogg(aktørId.orElse(null), null, BASE_PATH + path, request.begrunnelse()));
        entityManager.flush();

        return aktørId
            .map(it -> handling.apply(it, MikrofrontendId.AKTIVITETSPENGER_INNSYN))
            .map(endret -> Response.ok(new MikrofrontendForvaltningResultat(endret)).build())
            .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                .entity(new FeilDto(FeilType.TOMT_RESULTAT_FEIL, "Fant ikke aktørId for oppgitt fødselsnummer"))
                .build());
    }
}
