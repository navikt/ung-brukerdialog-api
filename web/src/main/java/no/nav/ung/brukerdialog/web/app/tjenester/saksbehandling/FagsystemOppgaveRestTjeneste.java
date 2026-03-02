package no.nav.ung.brukerdialog.web.app.tjenester.saksbehandling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionType;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursResourceType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.ung.brukerdialog.kontrakt.AktørIdDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.EndreFristDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.EndreOppgaveStatusDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveRequest;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OpprettOppgaveDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveForSaksbehandlingTjeneste;
import no.nav.ung.brukerdialog.web.server.abac.AbacAttributtSupplier;

@Path(FagsystemOppgaveRestTjeneste.BASE_PATH)
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "fagsystem-oppgavebehandling", description = "API for operasjoner på brukerdialogoppgaver fra ung-sak")
public class FagsystemOppgaveRestTjeneste {

    static final String BASE_PATH = "/oppgavebehandling";

    private OppgaveForSaksbehandlingTjeneste oppgaveForSaksbehandlingTjeneste;

    public FagsystemOppgaveRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public FagsystemOppgaveRestTjeneste(OppgaveForSaksbehandlingTjeneste oppgaveForSaksbehandlingTjeneste) {
        this.oppgaveForSaksbehandlingTjeneste = oppgaveForSaksbehandlingTjeneste;
    }

    @POST
    @Path("/opprett")
    @Operation(summary = "Oppretter en brukerdialogoppgave", tags = "saksbehandling-oppgave")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.CREATE, resource = BeskyttetRessursResourceType.FAGSAK)
    public Response opprettOppgave(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class)
        OpprettOppgaveDto oppgave) {
        oppgaveForSaksbehandlingTjeneste.opprettOppgave(oppgave);
        return Response.ok().build();
    }

    @POST
    @Path("/sett-avbrutt")
    @Operation(summary = "Avbryter en oppgave basert på ekstern referanse", tags = "saksbehandling-oppgave")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.FAGSAK)
    public Response avbrytOppgave(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class)
        OppgaveRequest oppgaveRequest) {
        oppgaveForSaksbehandlingTjeneste.avbrytOppgave(oppgaveRequest.oppgaveReferanse());
        return Response.ok().build();
    }

    @POST
    @Path("/sett-utlopt")
    @Operation(summary = "Markerer en oppgave som utløpt", tags = "saksbehandling-oppgave")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.FAGSAK)
    public Response oppgaveUtløpt(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class)
        OppgaveRequest oppgaveRequest) {
        oppgaveForSaksbehandlingTjeneste.oppgaveUtløpt(oppgaveRequest.oppgaveReferanse());
        return Response.ok().build();
    }

    @POST
    @Path("/sett-utlopt-for-type-og-periode")
    @Operation(summary = "Setter oppgaver av en gitt type og periode til utløpt", tags = "saksbehandling-oppgave")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.FAGSAK)
    public Response settOppgaveTilUtløpt(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class)
        EndreOppgaveStatusDto dto) {
        oppgaveForSaksbehandlingTjeneste.settOppgaveTilUtløpt(dto);
        return Response.ok().build();
    }

    @POST
    @Path("/sett-avbrutt-for-type-og-periode")
    @Operation(summary = "Setter oppgaver av en gitt type og periode til avbrutt", tags = "saksbehandling-oppgave")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.FAGSAK)
    public Response settOppgaveTilAvbrutt(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class)
        EndreOppgaveStatusDto dto) {
        oppgaveForSaksbehandlingTjeneste.settOppgaveTilAvbrutt(dto);
        return Response.ok().build();
    }

    @POST
    @Path("/los-sok-ytelse")
    @Operation(summary = "Løser en søk-ytelse-oppgave for en deltaker", tags = "saksbehandling-oppgave")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.FAGSAK)
    public Response løsSøkYtelseOppgave(
        @Valid
        @NotNull
        @Parameter(description = "AktørId for deltakeren")
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class)
        AktørIdDto aktørId) {
        oppgaveForSaksbehandlingTjeneste.løsSøkYtelseOppgave(aktørId.getAktørId());
        return Response.ok().build();
    }

    @POST
    @Path("/endre-frist")
    @Operation(summary = "Endrer frist for en oppgave", tags = "saksbehandling-oppgave")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.FAGSAK)
    public Response endreFrist(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class)
        EndreFristDto dto) {
        oppgaveForSaksbehandlingTjeneste.endreFrist(dto.aktørId(), dto.eksternReferanse(), dto.frist());
        return Response.ok().build();
    }
}

