package no.nav.ung.brukerdialog.web.app.tjenester.vedtak;

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
import no.nav.ung.brukerdialog.kontrakt.sak.mikrofrontend.AktiverMikrofrontendRequest;
import no.nav.ung.brukerdialog.sak.mikrofrontend.MikrofrontendId;
import no.nav.ung.brukerdialog.sak.mikrofrontend.MinSideMikrofrontendTjeneste;
import no.nav.ung.brukerdialog.web.server.abac.AbacAttributtSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(AktivitetspengerMikrofrontendRestTjeneste.BASE_PATH)
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "mikrofrontend", description = "API for inngang til aktivitetspenger på Min side")
public class AktivitetspengerMikrofrontendRestTjeneste {

    static final String BASE_PATH = "/aktivitetspenger/mikrofrontend";

    private static final Logger log = LoggerFactory.getLogger(AktivitetspengerMikrofrontendRestTjeneste.class);

    private MinSideMikrofrontendTjeneste mikrofrontendTjeneste;

    public AktivitetspengerMikrofrontendRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public AktivitetspengerMikrofrontendRestTjeneste(MinSideMikrofrontendTjeneste mikrofrontendTjeneste) {
        this.mikrofrontendTjeneste = mikrofrontendTjeneste;
    }

    @POST
    @Path("/aktiver")
    @Operation(summary = "Aktiverer inngangen til aktivitetspenger på Min side for brukeren. Idempotent.", tags = "mikrofrontend")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.CREATE, resource = BeskyttetRessursResourceType.FAGSAK)
    public Response aktiver(@Valid @NotNull @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class) AktiverMikrofrontendRequest request) {
        boolean aktivert = mikrofrontendTjeneste.aktiver(request.aktørId(), MikrofrontendId.AKTIVITETSPENGER_INNSYN);
        log.info("Aktivering av mikrofrontend for saksnummer={}: {}",
            request.saksnummer().getVerdi(), aktivert ? "aktivert" : "var allerede aktivert");
        return Response.ok().build();
    }
}
