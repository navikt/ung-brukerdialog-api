package no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskHandler;
import no.nav.ung.brukerdialog.JsonObjectMapper;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.RapportertInntektDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.OppgaveLivssyklusTjeneste;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.kafka.model.UngdomsytelseRapportertInntektTopicEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

/**
 * ProsessTask for å håndtere respons mottatt fra Kafka.
 */
@ApplicationScoped
@ProsessTask(value = HåndterRapportertInntektProsessTask.TASK_NAVN)
public class HåndterRapportertInntektProsessTask implements ProsessTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(HåndterRapportertInntektProsessTask.class);
    private static final ObjectMapper MAPPER = JsonObjectMapper.getMapper();
    public static final String TASK_NAVN = "handle.rapportert.inntekt";

    private BrukerdialogOppgaveRepository oppgaveRepository;
    private OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste;

    HåndterRapportertInntektProsessTask() {
        // CDI
    }

    @Inject
    public HåndterRapportertInntektProsessTask(BrukerdialogOppgaveRepository oppgaveRepository, OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste) {
        this.oppgaveRepository = oppgaveRepository;
        this.oppgaveLivssyklusTjeneste = oppgaveLivssyklusTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var payload = prosessTaskData.getPayloadAsString();

        UngdomsytelseRapportertInntektTopicEntry topicEntry = null;
        try {
            topicEntry = MAPPER.readValue(payload, UngdomsytelseRapportertInntektTopicEntry.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Ugyldig payload", e);
        }
        var oppgavereferanse = topicEntry.data().oppgaveReferanse();
        var rapportertInntekt = topicEntry.data().oppgittInntektForPeriode();

        log.info("Behandler rapportert inntekt for oppgaveReferanse='{}'", oppgavereferanse);

        // Finn oppgaven basert på oppgaveReferanse
        var oppgave = oppgaveRepository.hentOppgaveForOppgavereferanse(UUID.fromString(oppgavereferanse))
            .orElseThrow(() -> new IllegalStateException(
                "Fant ingen oppgave for oppgaveReferanse=" + oppgavereferanse));

        // Løser oppgave
        RapportertInntektDto rapportertInntektDto = new RapportertInntektDto(rapportertInntekt.getPeriode().getFraOgMed(), rapportertInntekt.getPeriode().getTilOgMed(), rapportertInntekt.getArbeidstakerOgFrilansInntekt());
        oppgaveLivssyklusTjeneste.løsOppgave(oppgave, Optional.of(rapportertInntektDto));

        log.info("Rapportert inntekt behandlet for oppgave med referanse='{}'",
            oppgave.getOppgavereferanse());

    }
}

