package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse.SøkYtelseOppgaveDataEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tester for OppgaveJournalføringRepository, inkludert at DB-constraintene fra
 * V1.0_017__oppgave_journalforing.sql håndheves som defence in depth mot bugs i
 * tjenestelaget.
 */
@ExtendWith(CdiAwareExtension.class)
@ExtendWith(JpaExtension.class)
class OppgaveJournalføringRepositoryTest {

    @Inject
    private EntityManager entityManager;

    @Inject
    private OppgaveJournalføringRepository repository;

    @Inject
    private BrukerdialogOppgaveRepository oppgaveRepository;

    private BrukerdialogOppgaveEntitet oppgave;

    @BeforeEach
    void setUp() {
        oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(),
            OppgaveType.SØK_YTELSE,
            new AktørId("1234567890123"),
            OppgaveYtelsetype.UNGDOMSYTELSE,
            null
        );
        oppgave.setOppgaveData(new SøkYtelseOppgaveDataEntitet(LocalDate.now()));
        oppgaveRepository.lagre(oppgave);
    }

    @Test
    void skal_persistere_og_hente_journalføring_for_oppgave() {
        // Arrange
        var journalføring = new OppgaveJournalføringEntitet(
            oppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.FAGSAK, new Saksnummer("ABC123"));

        // Act
        repository.lagre(journalføring);
        entityManager.flush();
        entityManager.clear();

        // Assert
        var hentet = repository.hentForOppgaveReferanse(oppgave.getOppgavereferanse());
        assertThat(hentet).isPresent();
        assertThat(hentet.get().getTema()).isEqualTo(Tema.UNG);
        assertThat(hentet.get().getFagsaksystem()).isEqualTo(Fagsaksystem.UNG_SAK);
        assertThat(hentet.get().getSakstype()).isEqualTo(Sakstype.FAGSAK);
        assertThat(hentet.get().getFagsakId()).isEqualTo(new Saksnummer("ABC123"));
        assertThat(hentet.get().getStatus()).isEqualTo(JournalføringStatus.PLANLAGT);
    }

    @Test
    void skal_returnere_empty_når_ingen_journalføring_finnes_for_oppgave() {
        assertThat(repository.hentForOppgaveReferanse(UUID.randomUUID())).isEmpty();
    }

    @Test
    void hentEtterslep_returnerer_kun_planlagte_rader_eldst_først() {
        // Arrange: én PLANLAGT og én JOURNALFORT
        var planlagt = new OppgaveJournalføringEntitet(
            oppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.GENERELL_SAK, null);
        repository.lagre(planlagt);

        var annenOppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.SØK_YTELSE, new AktørId("9876543210987"), OppgaveYtelsetype.UNGDOMSYTELSE, null);
        annenOppgave.setOppgaveData(new SøkYtelseOppgaveDataEntitet(LocalDate.now()));
        oppgaveRepository.lagre(annenOppgave);

        var journalført = new OppgaveJournalføringEntitet(
            annenOppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.GENERELL_SAK, null);
        journalført.markerJournalført(new no.nav.ung.brukerdialog.typer.JournalpostId("123456789"));
        repository.lagre(journalført);

        entityManager.flush();
        entityManager.clear();

        // Act
        var etterslep = repository.hentEtterslep();

        // Assert
        assertThat(etterslep).hasSize(1);
        assertThat(etterslep.get(0).getOppgave().getOppgavereferanse()).isEqualTo(oppgave.getOppgavereferanse());
    }

    @Test
    void tellEtterslepEldreEnn_teller_kun_rader_eldre_enn_grensen() {
        // Arrange: to PLANLAGT-rader - én "gammel" (2 timer) og én "fersk" (nyopprettet).
        // Etterslep-gaugen skal kun fange rader som har ventet mer enn 1 time.
        var gammel = new OppgaveJournalføringEntitet(
            oppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.GENERELL_SAK, null);
        repository.lagre(gammel);

        var annenOppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.SØK_YTELSE, new AktørId("9876543210987"), OppgaveYtelsetype.UNGDOMSYTELSE, null);
        annenOppgave.setOppgaveData(new SøkYtelseOppgaveDataEntitet(LocalDate.now()));
        oppgaveRepository.lagre(annenOppgave);
        var fersk = new OppgaveJournalføringEntitet(
            annenOppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.GENERELL_SAK, null);
        repository.lagre(fersk);
        entityManager.flush();

        // Flytter "gammel" sin opprettet_tid bakover, forbi entitetens egen livssyklus - se
        // settInnRadDirekte-mønsteret over.
        entityManager.createNativeQuery(
                "update BD_OPPGAVE_JOURNALFORING set opprettet_tid = :tidspunkt where id = :id")
            .setParameter("tidspunkt", java.time.LocalDateTime.now().minusHours(2))
            .setParameter("id", gammel.getId())
            .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // Act
        long antall = repository.tellEtterslepEldreEnn(java.time.Duration.ofHours(1));

        // Assert: kun "gammel" er eldre enn grensen på 1 time
        assertThat(antall).isEqualTo(1);
    }

    @Test
    void db_constraint_avviser_journalfort_status_uten_journalpost_id() {
        assertThatThrownBy(() -> settInnRadDirekte(
            "'UNG', 'UNG_SAK', 'GENERELL_SAK', 'JOURNALFORT', null, null"))
            .isInstanceOf(PersistenceException.class)
            .hasStackTraceContaining("chk_bd_oppgave_journalforing_journalpost");
    }

    @Test
    void db_constraint_avviser_fagsak_uten_fagsak_id() {
        assertThatThrownBy(() -> settInnRadDirekte(
            "'UNG', 'UNG_SAK', 'FAGSAK', 'PLANLAGT', null, null"))
            .isInstanceOf(PersistenceException.class)
            .hasStackTraceContaining("chk_bd_oppgave_journalforing_sak");
    }

    @Test
    void db_constraint_avviser_ukjent_status() {
        assertThatThrownBy(() -> settInnRadDirekte(
            "'UNG', 'UNG_SAK', 'GENERELL_SAK', 'UKJENT', null, null"))
            .isInstanceOf(PersistenceException.class)
            .hasStackTraceContaining("chk_bd_oppgave_journalforing_status");
    }

    /**
     * Setter inn en rad med rå SQL, forbi entitetens egne invarianter, for å verifisere at
     * DB-constraintene alene (defence in depth) avviser en ugyldig kombinasjon selv om
     * tjenestelaget skulle ha en bug. {@code kolonneverdier} er
     * {@code tema, fagsaksystem, sakstype, status, fagsak_id, journalpost_id} i rekkefølge.
     */
    private void settInnRadDirekte(String kolonneverdier) {
        entityManager.createNativeQuery(
                "insert into BD_OPPGAVE_JOURNALFORING " +
                    "(id, bd_oppgave_id, tema, fagsaksystem, sakstype, status, fagsak_id, journalpost_id) " +
                    "values (nextval('SEQ_BD_OPPGAVE_JOURNALFORING'), " +
                    "(select id from BD_OPPGAVE where oppgavereferanse = :oppgavereferanse), " +
                    kolonneverdier + ")")
            .setParameter("oppgavereferanse", oppgave.getOppgavereferanse())
            .executeUpdate();
    }
}
