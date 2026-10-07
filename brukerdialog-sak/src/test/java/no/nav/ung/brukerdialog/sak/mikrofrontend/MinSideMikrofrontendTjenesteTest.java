package no.nav.ung.brukerdialog.sak.mikrofrontend;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(CdiAwareExtension.class)
@ExtendWith(JpaExtension.class)
class MinSideMikrofrontendTjenesteTest {

    private static final MikrofrontendId MIKROFRONTEND = MikrofrontendId.AKTIVITETSPENGER_INNSYN;

    @Inject
    private EntityManager entityManager;

    @Inject
    private MinSideMikrofrontendRepository repository;

    private ProsessTaskTjeneste prosessTaskTjeneste;
    private MinSideMikrofrontendTjeneste tjeneste;

    @BeforeEach
    void setUp() {
        prosessTaskTjeneste = mock(ProsessTaskTjeneste.class);
        tjeneste = new MinSideMikrofrontendTjeneste(repository, prosessTaskTjeneste);
    }

    @Test
    void skal_lagre_status_og_opprette_task_ved_første_aktivering() {
        var aktørId = AktørId.dummy();

        assertThat(tjeneste.aktiver(aktørId, MIKROFRONTEND)).isTrue();
        flushOgTøm();

        assertThat(status(aktørId)).isEqualTo(MikrofrontendStatus.AKTIVERT);
        var task = enesteTask();
        assertThat(task.getTaskType()).isEqualTo(PubliserMinSideMikrofrontendTask.TASKTYPE);
        assertThat(task.getAktørId()).isEqualTo(aktørId.getId());
        assertThat(task.getPropertyValue(PubliserMinSideMikrofrontendTask.MIKROFRONTEND_ID)).isEqualTo(MIKROFRONTEND.name());
    }

    @Test
    void skal_ikke_opprette_ny_task_når_mikrofrontend_allerede_er_aktivert() {
        var aktørId = AktørId.dummy();
        tjeneste.aktiver(aktørId, MIKROFRONTEND);
        flushOgTøm();
        clearInvocations(prosessTaskTjeneste);

        assertThat(tjeneste.aktiver(aktørId, MIKROFRONTEND)).isFalse();

        verifyNoInteractions(prosessTaskTjeneste);
    }

    @Test
    void skal_deaktivere_aktivert_mikrofrontend() {
        var aktørId = AktørId.dummy();
        tjeneste.aktiver(aktørId, MIKROFRONTEND);
        flushOgTøm();
        clearInvocations(prosessTaskTjeneste);

        assertThat(tjeneste.deaktiver(aktørId, MIKROFRONTEND)).isTrue();
        flushOgTøm();

        assertThat(status(aktørId)).isEqualTo(MikrofrontendStatus.DEAKTIVERT);
        assertThat(enesteTask().getTaskType()).isEqualTo(PubliserMinSideMikrofrontendTask.TASKTYPE);
    }

    @Test
    void skal_ikke_deaktivere_når_mikrofrontend_aldri_er_aktivert() {
        var aktørId = AktørId.dummy();

        assertThat(tjeneste.deaktiver(aktørId, MIKROFRONTEND)).isFalse();

        assertThat(repository.hent(aktørId, MIKROFRONTEND)).isEmpty();
        verifyNoInteractions(prosessTaskTjeneste);
    }

    @Test
    void skal_ikke_deaktivere_på_nytt_når_mikrofrontend_allerede_er_deaktivert() {
        var aktørId = AktørId.dummy();
        tjeneste.aktiver(aktørId, MIKROFRONTEND);
        flushOgTøm();
        tjeneste.deaktiver(aktørId, MIKROFRONTEND);
        flushOgTøm();
        clearInvocations(prosessTaskTjeneste);

        assertThat(tjeneste.deaktiver(aktørId, MIKROFRONTEND)).isFalse();

        verifyNoInteractions(prosessTaskTjeneste);
    }

    @Test
    void skal_kunne_aktivere_igjen_etter_deaktivering() {
        var aktørId = AktørId.dummy();
        tjeneste.aktiver(aktørId, MIKROFRONTEND);
        flushOgTøm();
        tjeneste.deaktiver(aktørId, MIKROFRONTEND);
        flushOgTøm();
        clearInvocations(prosessTaskTjeneste);

        assertThat(tjeneste.aktiver(aktørId, MIKROFRONTEND)).isTrue();
        flushOgTøm();

        assertThat(status(aktørId)).isEqualTo(MikrofrontendStatus.AKTIVERT);
        assertThat(enesteTask().getTaskType()).isEqualTo(PubliserMinSideMikrofrontendTask.TASKTYPE);
    }

    private MikrofrontendStatus status(AktørId aktørId) {
        return repository.hent(aktørId, MIKROFRONTEND).orElseThrow().getStatus();
    }

    private ProsessTaskData enesteTask() {
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(prosessTaskTjeneste, times(1)).lagre(captor.capture());
        return captor.getValue();
    }

    private void flushOgTøm() {
        entityManager.flush();
        entityManager.clear();
    }
}
