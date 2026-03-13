package no.nav.ung.brukerdialog.web.app.tjenester.oppgavebehandling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
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
import no.nav.ung.brukerdialog.kontrakt.oppgaver.MigrerOppgaveDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.MigreringsRequest;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.MigreringsResultat;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraDtoTilEntitet;
import no.nav.ung.brukerdialog.web.server.abac.AbacAttributtEmptySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * REST-tjeneste for migrering av brukerdialogoppgaver fra annen applikasjon.
 * Tilgjengelig kun for driftstilgang.
 */
@Path("/forvaltning/oppgave/migrer")
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Forvaltning", description = "API for forvaltning av brukerdialog oppgaver")
public class MigrerBrukerdialogOppgaverRestTjeneste {

    private static final Logger log = LoggerFactory.getLogger(MigrerBrukerdialogOppgaverRestTjeneste.class);

    private BrukerdialogOppgaveRepository repository;
    private Instance<OppgaveDataMapperFraDtoTilEntitet> oppgaveDataMapper;


    public MigrerBrukerdialogOppgaverRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public MigrerBrukerdialogOppgaverRestTjeneste(
        BrukerdialogOppgaveRepository repository,
        @Any Instance<OppgaveDataMapperFraDtoTilEntitet> oppgaveDataMapper) {
        this.repository = repository;
        this.oppgaveDataMapper = oppgaveDataMapper;
    }

    /**
     * Migrerer oppgaver fra en annen app.
     * Idempotent - oppgaver som allerede eksisterer hoppes over.
     *
     * @param request Liste med oppgaver som skal migreres
     * @return Resultat med statistikk over migrering
     */
    @POST
    @Operation(
        summary = "Migrer brukerdialogoppgaver fra annen applikasjon",
        description = "Oppretter en prosess-task per oppgave for migrering. " +
            "Idempotent - gjør ingenting hvis oppgave med samme referanse allerede eksisterer."
    )
    @BeskyttetRessurs(action = BeskyttetRessursActionType.CREATE, resource = BeskyttetRessursResourceType.DRIFT)
    public Response migrerOppgaver(@Valid @NotNull @TilpassetAbacAttributt(supplierClass = AbacAttributtEmptySupplier.class) MigreringsRequest request) {
        log.info("Mottatt forespørsel om å migrere {} oppgaver", request.oppgaver().size());

        int antallOpprettet = 0;
        int antallHoppetOver = 0;

        for (MigrerOppgaveDto oppgaveDto : request.oppgaver()) {
            // Sjekk om oppgave allerede eksisterer
            var eksisterende = repository.hentOppgaveForOppgavereferanse(oppgaveDto.oppgaveReferanse());

            if (eksisterende.isPresent()) {
                // Pga feil i migreringslogikk ved kjøring i test
                if (eksisterende.get().getStatus() != oppgaveDto.status()) {
                    eksisterende.get().setStatus(oppgaveDto.status());
                    repository.oppdater(eksisterende.get());
                }

                log.debug("Oppgave med referanse {} eksisterer allerede, hopper over", oppgaveDto.oppgaveReferanse());
                antallHoppetOver++;
            } else {

                // Opprett ny oppgave med alle felter fra migrering
                LocalDateTime frist = oppgaveDto.frist() != null
                    ? oppgaveDto.frist().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                    : null;

                LocalDateTime løstDato = oppgaveDto.løstDato() != null
                    ? oppgaveDto.løstDato().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                    : null;

                LocalDateTime opprettetTidspunkt = oppgaveDto.opprettetDato().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

                var nyOppgave = new BrukerdialogOppgaveEntitet(
                    oppgaveDto.oppgaveReferanse(),
                    oppgaveDto.oppgavetype(),
                    oppgaveDto.aktørId(),
                    oppgaveDto.respons(),
                    oppgaveDto.status(),
                    frist,
                    løstDato,
                    opprettetTidspunkt,
                    "ung-deltakelse-opplyser"
                );
                opprettOppgave(nyOppgave, oppgaveDto.oppgavetypeData());
                antallOpprettet++;
            }
        }

        var resultat = new MigreringsResultat(antallOpprettet, antallHoppetOver);
        log.info("Migrering fullført: {} opprettet, {} hoppet over, {} totalt",
            resultat.antallOpprettet(), resultat.antallHoppetOver(), resultat.antallTotalt());

        return Response.ok(resultat).build();
    }


    private void opprettOppgave(BrukerdialogOppgaveEntitet oppgaveEntitet, OppgavetypeDataDto oppgavetypeData) {
        var oppgaveData = OppgaveDataMapperFraDtoTilEntitet.finnTjeneste(oppgaveDataMapper, oppgaveEntitet.getOppgaveType()).map(oppgavetypeData);
        oppgaveEntitet.setOppgaveData(oppgaveData);
        repository.lagre(oppgaveEntitet);
    }

}

