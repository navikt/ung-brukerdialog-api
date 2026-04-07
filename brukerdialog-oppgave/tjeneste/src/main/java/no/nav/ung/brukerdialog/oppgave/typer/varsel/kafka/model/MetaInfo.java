package no.nav.ung.brukerdialog.oppgave.typer.varsel.kafka.model;

public record MetaInfo(
    int version,
    String correlationId,
    String soknadDialogCommitSha
) {
}

