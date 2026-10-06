package no.nav.ung.brukerdialog.sak.mikrofrontend;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.integrasjon.kafka.GenerellKafkaProducer;
import no.nav.k9.felles.integrasjon.kafka.KafkaPropertiesBuilder;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;

@ApplicationScoped
public class MinSideMikrofrontendKafkaProducer {

    private GenerellKafkaProducer producer;

    MinSideMikrofrontendKafkaProducer() {
        // for CDI proxy
    }

    @Inject
    public MinSideMikrofrontendKafkaProducer(
        @KonfigVerdi(value = "kafka.minside.mikrofrontend.topic", defaultVerdi = "min-side.aapen-microfrontend-v1") String topic,
        @KonfigVerdi(value = "KAFKA_BROKERS") String kafkaBrokers,
        @KonfigVerdi(value = "KAFKA_TRUSTSTORE_PATH", required = false) String trustStorePath,
        @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String trustStorePassword,
        @KonfigVerdi(value = "KAFKA_KEYSTORE_PATH", required = false) String keyStoreLocation,
        @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String keyStorePassword
    ) {
        Properties aivenProps = new KafkaPropertiesBuilder()
            .clientId("KP-" + topic)
            .bootstrapServers(kafkaBrokers)
            .truststorePath(trustStorePath)
            .truststorePassword(trustStorePassword)
            .keystorePath(keyStoreLocation)
            .keystorePassword(keyStorePassword)
            .buildForProducerAiven();

        producer = new GenerellKafkaProducer(topic, aivenProps);
    }

    public RecordMetadata send(String nøkkel, String json) {
        return producer.sendJsonMedNøkkel(nøkkel, json);
    }
}
