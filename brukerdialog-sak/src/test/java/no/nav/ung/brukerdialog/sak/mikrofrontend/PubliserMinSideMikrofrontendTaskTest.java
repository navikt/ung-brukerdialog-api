package no.nav.ung.brukerdialog.sak.mikrofrontend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.k9.felles.integrasjon.pdl.PdlKlient;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PubliserMinSideMikrofrontendTaskTest {

    private static final AktørId AKTØR_ID = new AktørId("1234567890123");
    private static final String FNR = "12345678901";
    private static final String NAMESPACE = "k9saksbehandling";

    @Mock
    private MinSideMikrofrontendRepository repository;
    @Mock
    private MinSideMikrofrontendKafkaProducer producer;
    @Mock
    private PdlKlient pdl;

    private PubliserMinSideMikrofrontendTask task;

    @BeforeEach
    void setUp() {
        task = new PubliserMinSideMikrofrontendTask(repository, producer, pdl, NAMESPACE);
    }

    @Test
    void skal_sende_enable_med_høy_sensitivitet_når_status_er_aktivert() throws Exception {
        gittStatus(MikrofrontendStatus.AKTIVERT);

        task.doTask(prosessTaskData());

        JsonNode melding = sendtMelding("aktivitetspenger-innsyn");
        assertThat(melding.get("@action").asText()).isEqualTo("enable");
        assertThat(melding.get("ident").asText()).isEqualTo(FNR);
        assertThat(melding.get("microfrontend_id").asText()).isEqualTo("aktivitetspenger-innsyn");
        assertThat(melding.get("sensitivitet").asText()).isEqualTo("high");
        assertThat(melding.get("@initiated_by").asText()).isEqualTo(NAMESPACE);
    }

    @Test
    void skal_sende_disable_når_status_er_deaktivert() throws Exception {
        gittStatus(MikrofrontendStatus.DEAKTIVERT);

        task.doTask(prosessTaskData());

        JsonNode melding = sendtMelding("aktivitetspenger-innsyn");
        assertThat(melding.get("@action").asText()).isEqualTo("disable");
        assertThat(melding.get("ident").asText()).isEqualTo(FNR);
        assertThat(melding.get("microfrontend_id").asText()).isEqualTo("aktivitetspenger-innsyn");
        assertThat(melding.get("@initiated_by").asText()).isEqualTo(NAMESPACE);
        assertThat(melding.has("sensitivitet")).isFalse();
    }

    @Test
    void skal_feile_når_personident_ikke_finnes() {
        when(repository.hent(AKTØR_ID, MikrofrontendId.AKTIVITETSPENGER_INNSYN))
            .thenReturn(Optional.of(new MinSideMikrofrontendEntitet(AKTØR_ID, MikrofrontendId.AKTIVITETSPENGER_INNSYN, MikrofrontendStatus.AKTIVERT)));
        when(pdl.hentPersonIdentForAktørId(AKTØR_ID.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> task.doTask(prosessTaskData())).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(producer);
    }

    @Test
    void skal_feile_når_status_ikke_finnes() {
        when(repository.hent(AKTØR_ID, MikrofrontendId.AKTIVITETSPENGER_INNSYN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> task.doTask(prosessTaskData())).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(producer);
    }

    private void gittStatus(MikrofrontendStatus status) {
        when(repository.hent(AKTØR_ID, MikrofrontendId.AKTIVITETSPENGER_INNSYN))
            .thenReturn(Optional.of(new MinSideMikrofrontendEntitet(AKTØR_ID, MikrofrontendId.AKTIVITETSPENGER_INNSYN, status)));
        when(pdl.hentPersonIdentForAktørId(AKTØR_ID.getId())).thenReturn(Optional.of(FNR));
        when(producer.send(anyString(), anyString()))
            .thenReturn(new RecordMetadata(new TopicPartition("min-side.aapen-microfrontend-v1", 0), 0, 0, 0, 0, 0));
    }

    private ProsessTaskData prosessTaskData() {
        ProsessTaskData data = ProsessTaskData.forProsessTask(PubliserMinSideMikrofrontendTask.class);
        data.setAktørId(AKTØR_ID.getId());
        data.setProperty(PubliserMinSideMikrofrontendTask.MIKROFRONTEND_ID, MikrofrontendId.AKTIVITETSPENGER_INNSYN.name());
        return data;
    }

    private JsonNode sendtMelding(String forventetNøkkel) throws Exception {
        var nøkkel = ArgumentCaptor.forClass(String.class);
        var json = ArgumentCaptor.forClass(String.class);
        verify(producer).send(nøkkel.capture(), json.capture());
        assertThat(nøkkel.getValue()).isEqualTo(forventetNøkkel);
        return new ObjectMapper().readTree(json.getValue());
    }
}
