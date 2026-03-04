package no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse.kafka.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SøknadMottatt(
    String oppgaveReferanse
) {
}
