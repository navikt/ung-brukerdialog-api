package no.nav.ung.brukerdialog.oppgave.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.apptjeneste.AppServiceHandler;
import no.nav.k9.felles.integrasjon.kafka.KafkaConsumerManager;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.kafka.RapportertInntektHendelseHåndterer;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse.kafka.SøknadHendelseHåndterer;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.kafka.SvarPåVarselHendelseHåndterer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class BrukerdialogKafkaServiceHandler implements AppServiceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BrukerdialogKafkaServiceHandler.class);

    private KafkaConsumerManager<String, String> kcm;

    public BrukerdialogKafkaServiceHandler() {
    }

    @Inject
    public BrukerdialogKafkaServiceHandler(
        @KonfigVerdi(value = "OPPGAVER_I_UNGBRUKERDIALOG_ENABLED", defaultVerdi = "true") boolean oppgaverIUngsakEnabled,
        SvarPåVarselHendelseHåndterer svarPåVarselHendelseHåndterer,
        RapportertInntektHendelseHåndterer rapportertInntektHendelseHåndterer,
        SøknadHendelseHåndterer søknadHendelseHåndterer) {
        this.kcm = oppgaverIUngsakEnabled ?
            new KafkaConsumerManager<>(List.of(
                svarPåVarselHendelseHåndterer,
                rapportertInntektHendelseHåndterer,
                søknadHendelseHåndterer)) :
            new KafkaConsumerManager<>(List.of());
    }

    @Override
    public void start() {
        LOG.info("Starter konsumering av topics={}", kcm.topicNames());
        kcm.start((t, e) -> LOG.error("{} :: Caught exception in stream, exiting", t, e));
    }

    @Override
    public void stop() {
        LOG.info("Starter shutdown av topics={} med 10 sekunder timeout", kcm.topicNames());
        kcm.stop();
    }

    public boolean allRunning() {
        return kcm.allRunning();
    }

    public String getTopicNames() {
        return kcm.topicNames();
    }

}
