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
import no.nav.ung.brukerdialog.typer.JournalpostId;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tester for OppgaveJournalføringRepository, inkludert at DB-constrainten fra
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
            oppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.FAGSAK, new Saksnummer("ABC123"),
            new JournalpostId("123456789"));

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
        assertThat(hentet.get().getSaksnummer()).isEqualTo(new Saksnummer("ABC123"));
        assertThat(hentet.get().getJournalpostId()).isEqualTo(new JournalpostId("123456789"));
    }

    @Test
    void skal_returnere_empty_når_ingen_journalføring_finnes_for_oppgave() {
        assertThat(repository.hentForOppgaveReferanse(UUID.randomUUID())).isEmpty();
    }

    @Test
    void db_constraint_avviser_fagsak_uten_saksnummer() {
        assertThatThrownBy(() -> settInnRadDirekte(
            "'UNG', 'UNG_SAK', 'FAGSAK', null, '123456789'"))
            .isInstanceOf(PersistenceException.class)
            .hasStackTraceContaining("chk_bd_oppgave_journalforing_sak");
    }

    /**
     * Setter inn en rad med rå SQL, forbi entitetens egne invarianter, for å verifisere at
     * DB-constrainten alene (defence in depth) avviser en ugyldig kombinasjon selv om
     * tjenestelaget skulle ha en bug. {@code kolonneverdier} er
     * {@code tema, fagsaksystem, sakstype, saksnummer, journalpost_id} i rekkefølge.
     */
    private void settInnRadDirekte(String kolonneverdier) {
        entityManager.createNativeQuery(
                "insert into BD_OPPGAVE_JOURNALFORING " +
                    "(id, bd_oppgave_id, tema, fagsaksystem, sakstype, saksnummer, journalpost_id, journalfort_tid) " +
                    "values (nextval('SEQ_BD_OPPGAVE_JOURNALFORING'), " +
                    "(select id from BD_OPPGAVE where oppgavereferanse = :oppgavereferanse), " +
                    kolonneverdier + ", current_timestamp)")
            .setParameter("oppgavereferanse", oppgave.getOppgavereferanse())
            .executeUpdate();
    }
}
