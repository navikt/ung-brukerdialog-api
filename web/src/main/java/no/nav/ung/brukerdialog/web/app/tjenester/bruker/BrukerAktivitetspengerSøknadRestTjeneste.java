package no.nav.ung.brukerdialog.web.app.tjenester.bruker;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.k9.felles.integrasjon.pdl.PdlKlient;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionType;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursResourceType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.k9.sikkerhet.context.SubjectHandler;
import no.nav.ung.brukerdialog.kontrakt.soknad.OpprettSøknadHendelseRequest;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse;
import no.nav.ung.brukerdialog.sak.soknad.FagsakYtelseType;
import no.nav.ung.brukerdialog.sak.soknad.SøknadHendelseTjeneste;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.web.server.abac.AbacAttributtEmptySupplier;

@Path(BrukerAktivitetspengerSøknadRestTjeneste.BASE_PATH)
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BrukerAktivitetspengerSøknadRestTjeneste {

    static final String BASE_PATH = "/aktivitetspenger/soknad";

    private SøknadHendelseTjeneste søknadHendelseTjeneste;
    private PdlKlient pdl;

    public BrukerAktivitetspengerSøknadRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public BrukerAktivitetspengerSøknadRestTjeneste(SøknadHendelseTjeneste søknadHendelseTjeneste, PdlKlient pdl) {
        this.søknadHendelseTjeneste = søknadHendelseTjeneste;
        this.pdl = pdl;
    }

    @GET
    @Path("/tilgjengelig")
    @Operation(summary = "Om innlogget deltaker kan sende inn aktivitetspenger-søknad nå, og i så fall hva slags", tags = "brukerdialog-søknad")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.READ, resource = BeskyttetRessursResourceType.TOKENX_RESOURCE)
    public TilgjengeligSøknadResponse tilgjengeligSøknad() {
        return søknadHendelseTjeneste.finnTilgjengeligSøknad(finnAktørId(), FagsakYtelseType.AKTIVITETSPENGER);
    }

    /**
     * Kalles synkront av k9-brukerdialog-prosessering ved innsending. Deltakeren utledes fra tokenet,
     * ikke fra søknaden, slik at ingen kan registrere på vegne av en annen.
     */
    @POST
    @Path("/registrer")
    @Operation(summary = "Registrerer at innlogget deltaker har sendt inn aktivitetspenger-søknad", tags = "brukerdialog-søknad")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.CREATE, resource = BeskyttetRessursResourceType.TOKENX_RESOURCE)
    public Response registrer(
        @Valid
        @NotNull
        @Parameter(description = "Søknaden som er sendt inn")
        @TilpassetAbacAttributt(supplierClass = AbacAttributtEmptySupplier.class)
        OpprettSøknadHendelseRequest request) {
        søknadHendelseTjeneste.registrer(finnAktørId(), FagsakYtelseType.AKTIVITETSPENGER, request);
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * Veksler fra personIdent i token til aktørId ved hjelp av PDL.
     */
    private AktørId finnAktørId() {
        String personIdent = SubjectHandler.getSubjectHandler().getSluttBruker().getUid();
        String aktørIdString = pdl.hentAktørIdForPersonIdent(personIdent, false)
            .orElseThrow(() -> new IllegalStateException("Finner ikke aktørId for personIdent"));
        return new AktørId(aktørIdString);
    }
}
