package no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;
import no.nav.k9.felles.integrasjon.kafka.KafkaMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
@ActivateRequestContext
@Transactional
public class RapportertInntektHendelseHåndterer implements KafkaMessageHandler.KafkaStringMessageHandler {

    private static final String GROUP_ID = "ung-inntekt-rapportering"; // Hold konstant pga offset commit
    private String topicName;
    private ProsessTaskTjeneste taskTjeneste;

    RapportertInntektHendelseHåndterer() {
    }

    @Inject
    public RapportertInntektHendelseHåndterer(
        @KonfigVerdi(value = "KAFKA_RAPPORTERT_INNTEKT_TOPIC", defaultVerdi = "dusseldorf.ungdomsytelse-inntektsrapportering-mottatt") String topicName,
        ProsessTaskTjeneste taskTjeneste) {
        this.topicName = topicName;
        this.taskTjeneste = taskTjeneste;
    }


    @Override
    public void handleRecord(String key, String value) {
        try {
            // Opprett prosesstask for å håndtere svaret
            var prosessTaskData = ProsessTaskData.forProsessTask(HåndterRapportertInntektProsessTask.class);
            prosessTaskData.setPayload(value);
            prosessTaskData.setCallIdFraEksisterende();
            taskTjeneste.lagre(prosessTaskData);
        } catch (Exception e) {
            throw new IllegalStateException("Feil ved håndtering av rapportert inntekt", e);
        }
    }

    @Override
    public String topic() {
        return topicName;
    }

    @Override
    public String groupId() {
        return GROUP_ID;
    }

}
