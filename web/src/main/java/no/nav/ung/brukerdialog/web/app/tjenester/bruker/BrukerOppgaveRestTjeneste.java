package no.nav.ung.brukerdialog.web.app.tjenester.bruker;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import no.nav.k9.felles.integrasjon.pdl.Pdl;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionType;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursResourceType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.k9.sikkerhet.context.SubjectHandler;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto;
import no.nav.ung.brukerdialog.oppgave.brukerdialog.BrukerdialogOppgaveTjeneste;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.web.server.abac.AbacAttributtEmptySupplier;

import java.util.List;
import java.util.UUID;

@Path(BrukerOppgaveRestTjeneste.BASE_PATH)
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class BrukerOppgaveRestTjeneste {
    static final String BASE_PATH = "/oppgave";

    private BrukerdialogOppgaveTjeneste oppgaveTjeneste;
    private Pdl pdl;

    public BrukerOppgaveRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public BrukerOppgaveRestTjeneste(BrukerdialogOppgaveTjeneste oppgaveTjeneste, Pdl pdl) {
        this.oppgaveTjeneste = oppgaveTjeneste;
        this.pdl = pdl;
    }

    @GET
    @Path("/hent/alle")
    @Operation(summary = "Henter alle oppgaver for en bruker", tags = "brukerdialog-oppgave")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.READ, resource = BeskyttetRessursResourceType.TOKENX_RESOURCE)
    public List<BrukerdialogOppgaveDto> hentAlleOppgaver() {
        return oppgaveTjeneste.hentAlleOppgaverForAktør(finnAktørId());
    }


    @GET
    @Path("/{oppgavereferanse}")
    @Operation(summary = "Henter en spesifikk oppgave basert på oppgaveReferanse", tags = "brukerdialog-oppgave")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.READ, resource = BeskyttetRessursResourceType.TOKENX_RESOURCE)
    public BrukerdialogOppgaveDto hentOppgave(
        @Valid
        @NotNull
        @PathParam("oppgavereferanse")
        @Parameter(description = "Unik referanse til oppgaven")
        @TilpassetAbacAttributt(supplierClass = AbacAttributtEmptySupplier.class)
        UUID oppgavereferanse) {
        return oppgaveTjeneste.hentOppgaveForOppgavereferanse(oppgavereferanse, finnAktørId());
    }

    /**
     * Veksler fra personIdent i token til aktørId ved hjelp av PDL.
     *
     * @return AktørId til innlogget bruker
     */
    private AktørId finnAktørId() {
        String personIdent = SubjectHandler.getSubjectHandler().getSluttBruker().getUid();
        String aktørIdString = pdl.hentAktørIdForPersonIdent(personIdent, false)
            .orElseThrow(() -> new IllegalStateException("Finner ikke aktørId for personIdent"));
        return new AktørId(aktørIdString);
    }
}

