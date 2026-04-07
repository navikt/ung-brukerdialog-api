package no.nav.ung.brukerdialog.oppgave.typer.varsel.kafka.model;

public record SvarPåVarselTopicEntry(
    MetaInfo metadata,
    SvarPåVarsel data
) {
}

