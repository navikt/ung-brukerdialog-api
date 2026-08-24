package no.nav.ung.brukerdialog.web.app.tjenester.oppgavebehandling;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.Response;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.MigrerOppgaveDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.MigreringsRequest;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraDtoTilEntitet;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveJournalføringRepository;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifiserer at migrerte oppgaver ikke journalføres: migreringen skriver
 * direkte til {@link BrukerdialogOppgaveRepository} og går utenom
 * {@code OppgaveLivssyklusTjeneste}, som er stedet journalføringsraden normalt opprettes fra.
 * <p>
 * Tjenesten instansieres direkte (ikke via {@code @Inject}) for å hoppe over
 * {@code @BeskyttetRessurs}/{@code @Transactional}-interceptorene, som krever en full
 * OIDC-/ABAC-kontekst som ikke finnes i et rent {@code CdiAwareExtension}-testoppsett. Selve
 * migreringslogikken er uendret - kun auth/transaksjonsaspektene er koblet fra, og
 * {@code JpaExtension} gir allerede en ambient transaksjon for testen.
 */
@ExtendWith(CdiAwareExtension.class)
@ExtendWith(JpaExtension.class)
class MigrerBrukerdialogOppgaverRestTjenesteTest {

    @Inject
    private EntityManager entityManager;

    @Inject
    private BrukerdialogOppgaveRepository oppgaveRepository;

    @Inject
    private OppgaveJournalføringRepository journalføringRepository;

    @Inject
    @Any
    private Instance<OppgaveDataMapperFraDtoTilEntitet> oppgaveDataMapper;

    @Test
    void migrert_oppgave_oppretter_ingen_journalføringsrad() {
        // Arrange
        var migreringsTjeneste = new MigrerBrukerdialogOppgaverRestTjeneste(oppgaveRepository, oppgaveDataMapper);
        UUID oppgaveReferanse = UUID.randomUUID();
        var migrertOppgave = new MigrerOppgaveDto(
            oppgaveReferanse,
            new AktørId("1234567890123"),
            OppgaveType.SØK_YTELSE,
            new SøkYtelseOppgavetypeDataDto(LocalDate.of(2026, 2, 1)),
            null,
            OppgaveStatus.ULØST,
            ZonedDateTime.now(),
            null,
            null
        );

        // Act
        Response respons = migreringsTjeneste.migrerOppgaver(new MigreringsRequest(List.of(migrertOppgave)));
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(respons.getStatus()).isEqualTo(200);
        var lagretOppgave = oppgaveRepository.hentOppgaveForOppgavereferanse(oppgaveReferanse);
        assertThat(lagretOppgave).isPresent();
        assertThat(journalføringRepository.hentForOppgaveReferanse(oppgaveReferanse)).isEmpty();
        assertThat(journalføringRepository.hentEtterslep()).isEmpty();
    }
}
