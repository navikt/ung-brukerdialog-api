package no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;
import no.nav.k9.felles.integrasjon.kafka.KafkaMessageHandler;


@ApplicationScoped
@ActivateRequestContext
@Transactional
public class SøknadHendelseHåndterer implements KafkaMessageHandler.KafkaStringMessageHandler {

    private static final String GROUP_ID = "ung-inntekt-rapportering"; // Hold konstant pga offset commit
    private String topicName;
    private ProsessTaskTjeneste taskTjeneste;

    SøknadHendelseHåndterer() {
    }

    @Inject
    public SøknadHendelseHåndterer(
        @KonfigVerdi(value = "KAFKA_UNG_SOKNAD_TOPIC", defaultVerdi = "dusseldorf.ungdomsytelse-soknad-mottatt") String topicName,
        ProsessTaskTjeneste taskTjeneste) {
        this.topicName = topicName;
        this.taskTjeneste = taskTjeneste;
    }


    @Override
    public void handleRecord(String key, String value) {
        try {
            // Opprett prosesstask for å håndtere svaret
            var prosessTaskData = ProsessTaskData.forProsessTask(HåndterSøknadProsessTask.class);
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
