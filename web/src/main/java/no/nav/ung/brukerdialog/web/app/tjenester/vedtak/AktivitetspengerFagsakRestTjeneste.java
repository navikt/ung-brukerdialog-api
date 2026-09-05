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
import no.nav.ung.brukerdialog.kontrakt.vedtak.FagSakRequest;
import no.nav.ung.brukerdialog.sak.fagsak.FagsakTjeneste;
import no.nav.ung.brukerdialog.sak.soknad.FagsakYtelseType;
import no.nav.ung.brukerdialog.web.server.abac.AbacAttributtSupplier;

@Path(AktivitetspengerFagsakRestTjeneste.BASE_PATH)
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "fagsak", description = "API for fagsakinfo meldt inn av ung-sak")
public class AktivitetspengerFagsakRestTjeneste {

    static final String BASE_PATH = "/aktivitetspenger/fagsak";

    private FagsakTjeneste fagsakTjeneste;

    public AktivitetspengerFagsakRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public AktivitetspengerFagsakRestTjeneste(FagsakTjeneste fagsakTjeneste) {
        this.fagsakTjeneste = fagsakTjeneste;
    }

    @POST
    @Operation(summary = "Melder inn vedtaksperioder og behandlede søknader for en bruker", tags = "fagsak")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.FAGSAK)
    public Response mottaFagsak(@Valid @NotNull @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class) FagSakRequest request) {
        fagsakTjeneste.motta(FagsakYtelseType.AKTIVITETSPENGER, request);
        return Response.ok().build();
    }
}
