package no.nav.ung.brukerdialog.sak.mikrofrontend;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.integrasjon.pdl.PdlKlient;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskHandler;
import no.nav.tms.microfrontend.MicrofrontendMessageBuilder;
import no.nav.tms.microfrontend.Sensitivitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Sender gjeldende status for mikrofrontenden til Min side.
 */
@ApplicationScoped
@ProsessTask(PubliserMinSideMikrofrontendTask.TASKTYPE)
public class PubliserMinSideMikrofrontendTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "minside.publisermikrofrontend";
    public static final String MIKROFRONTEND_ID = "mikrofrontendId";

    private static final Logger log = LoggerFactory.getLogger(PubliserMinSideMikrofrontendTask.class);

    private MinSideMikrofrontendRepository repository;
    private MinSideMikrofrontendKafkaProducer producer;
    private PdlKlient pdl;
    private String appNamespace;

    PubliserMinSideMikrofrontendTask() {
        // for CDI proxy
    }

    @Inject
    public PubliserMinSideMikrofrontendTask(
        MinSideMikrofrontendRepository repository,
        MinSideMikrofrontendKafkaProducer producer,
        PdlKlient pdl,
        @KonfigVerdi(value = "NAIS_NAMESPACE", defaultVerdi = "k9saksbehandling") String appNamespace
    ) {
        this.repository = repository;
        this.producer = producer;
        this.pdl = pdl;
        this.appNamespace = appNamespace;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        MikrofrontendId mikrofrontendId = MikrofrontendId.valueOf(prosessTaskData.getPropertyValue(MIKROFRONTEND_ID));
        AktørId aktørId = new AktørId(prosessTaskData.getAktørId());
        MikrofrontendStatus status = repository.hent(aktørId, mikrofrontendId)
            .map(MinSideMikrofrontendEntitet::getStatus)
            .orElseThrow(() -> new IllegalStateException("Fant ikke status for mikrofrontend " + mikrofrontendId));

        String personIdent = pdl.hentPersonIdentForAktørId(aktørId.getId())
            .orElseThrow(() -> new IllegalStateException("Finner ikke personident for aktørId"));
        String melding = switch (status) {
            case AKTIVERT -> MicrofrontendMessageBuilder.INSTANCE
                .enable(personIdent, mikrofrontendId.getId(), appNamespace, Sensitivitet.HIGH)
                .text();
            case DEAKTIVERT -> MicrofrontendMessageBuilder.INSTANCE
                .disable(personIdent, mikrofrontendId.getId(), appNamespace)
                .text();
        };

        RecordMetadata recordMetadata = producer.send(mikrofrontendId.getId(), melding);
        log.info("Sendte status {} for mikrofrontend {} til {} partition {} offset {}",
            status, mikrofrontendId, recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
    }

    @Override
    public Set<String> requiredProperties() {
        return Set.of(MIKROFRONTEND_ID, ProsessTaskData.AKTØR_ID);
    }
}
