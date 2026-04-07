package no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.kafka.model;

public record MetaInfo(
    int version,
    String correlationId,
    String soknadDialogCommitSha
) {
}

