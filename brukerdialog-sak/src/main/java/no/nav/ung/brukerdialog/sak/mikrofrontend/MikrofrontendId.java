package no.nav.ung.brukerdialog.sak.mikrofrontend;

import java.util.Objects;

/**
 * Id-en avtales med team min-side, og skal være lik navnet på repoet til mikrofrontenden.
 */
public enum MikrofrontendId {

    AKTIVITETSPENGER_INNSYN("aktivitetspenger-innsyn");

    private final String id;

    MikrofrontendId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String getId() {
        return id;
    }
}
