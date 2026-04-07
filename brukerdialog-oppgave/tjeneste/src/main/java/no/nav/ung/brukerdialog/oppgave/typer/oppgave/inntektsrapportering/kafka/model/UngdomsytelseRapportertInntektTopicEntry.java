package no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.kafka.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UngdomsytelseRapportertInntektTopicEntry(
    MetaInfo metadata,
    UngdomsytelseInntektsrapporteringMottatt data
) {
}

