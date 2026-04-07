package no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse.kafka.model;

public record SøknadTopicEntry(
    MetaInfo metadata,
    SøknadMottatt data
) {
}

