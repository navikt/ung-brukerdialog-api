package no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse.kafka.model;

public record MetaInfo(
    int version,
    String correlationId,
    String soknadDialogCommitSha
) {
}

