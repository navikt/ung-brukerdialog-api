package no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskHandler;
import no.nav.ung.brukerdialog.JsonObjectMapper;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.OppgaveLivssyklusTjeneste;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse.kafka.model.SøknadTopicEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * ProsessTask for å håndtere respons mottatt fra Kafka.
 */
@ApplicationScoped
@ProsessTask(value = HåndterSøknadProsessTask.TASK_NAVN)
public class HåndterSøknadProsessTask implements ProsessTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(HåndterSøknadProsessTask.class);
    private static final ObjectMapper MAPPER = JsonObjectMapper.getMapper();
    public static final String TASK_NAVN = "handle.ung.soknad";

    private BrukerdialogOppgaveRepository oppgaveRepository;
    private OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste;

    HåndterSøknadProsessTask() {
        // CDI
    }

    @Inject
    public HåndterSøknadProsessTask(BrukerdialogOppgaveRepository oppgaveRepository, OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste) {
        this.oppgaveRepository = oppgaveRepository;
        this.oppgaveLivssyklusTjeneste = oppgaveLivssyklusTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var payload = prosessTaskData.getPayloadAsString();

        SøknadTopicEntry topicEntry = null;
        try {
            topicEntry = MAPPER.readValue(payload, SøknadTopicEntry.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Ugyldig payload", e);
        }
        var oppgavereferanse = topicEntry.data().oppgaveReferanse();

        log.info("Behandler søknad for oppgaveReferanse='{}'", oppgavereferanse);

        // Finn oppgaven basert på oppgaveReferanse
        var oppgave = oppgaveRepository.hentOppgaveForOppgavereferanse(UUID.fromString(oppgavereferanse))
            .orElseThrow(() -> new IllegalStateException(
                "Fant ingen oppgave for oppgaveReferanse=" + oppgavereferanse));

        oppgaveLivssyklusTjeneste.løsOppgave(oppgave);

        log.info("Mottatt søknad behandlet for oppgave med referanse='{}'",
            oppgave.getOppgavereferanse());

    }
}

