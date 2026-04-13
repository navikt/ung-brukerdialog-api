package no.nav.ung.brukerdialog.web.app.tjenester.oppgavebehandling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import no.nav.ung.brukerdialog.kontrakt.AktørIdDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.*;
import no.nav.ung.brukerdialog.oppgave.OppgaveForSaksbehandlingTjeneste;
import no.nav.ung.brukerdialog.web.server.abac.AbacAttributtSupplier;

@Path(OppgavebehandlingRestTjeneste.BASE_PATH)
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "oppgavebehandling", description = "API for operasjoner på brukerdialogoppgaver")
public class OppgavebehandlingRestTjeneste {

    static final String BASE_PATH = "/oppgavebehandling";

    private OppgaveForSaksbehandlingTjeneste oppgaveForSaksbehandlingTjeneste;

    public OppgavebehandlingRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public OppgavebehandlingRestTjeneste(OppgaveForSaksbehandlingTjeneste oppgaveForSaksbehandlingTjeneste) {
        this.oppgaveForSaksbehandlingTjeneste = oppgaveForSaksbehandlingTjeneste;
    }

    @POST
    @Path("/opprett")
    @Operation(summary = "Oppretter en brukerdialogoppgave", tags = "oppgavebehandling")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.CREATE, resource = BeskyttetRessursResourceType.OPPGAVE, auditlogg = false)
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
    @Operation(summary = "Avbryter en oppgave basert på ekstern referanse", tags = "oppgavebehandling")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.OPPGAVE, auditlogg = false)
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
    @Operation(summary = "Markerer en oppgave som utløpt", tags = "oppgavebehandling")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.OPPGAVE, auditlogg = false)
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
    @Operation(summary = "Setter oppgaver av en gitt type og periode til utløpt", tags = "oppgavebehandling")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.OPPGAVE, auditlogg = false)
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
    @Operation(summary = "Setter oppgaver av en gitt type og periode til avbrutt", tags = "oppgavebehandling")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.OPPGAVE, auditlogg = false)
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
    @Operation(summary = "Løser en søk-ytelse-oppgave for en deltaker", tags = "oppgavebehandling")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.OPPGAVE, auditlogg = false)
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
    @Operation(summary = "Endrer frist for en oppgave", tags = "oppgavebehandling")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.UPDATE, resource = BeskyttetRessursResourceType.OPPGAVE, auditlogg = false)
    public Response endreFrist(
        @Valid
        @NotNull
        @TilpassetAbacAttributt(supplierClass = AbacAttributtSupplier.class)
        EndreFristDto dto) {
        oppgaveForSaksbehandlingTjeneste.endreFrist(dto.aktørId(), dto.eksternReferanse(), dto.frist());
        return Response.ok().build();
    }

}

