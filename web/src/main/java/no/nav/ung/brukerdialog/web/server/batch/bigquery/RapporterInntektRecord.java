package no.nav.ung.brukerdialog.web.server.batch.bigquery;

import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RapporterInntektRecord(
    UUID oppgaveReferanse,
    String oppgaveStatus,
    LocalDate fraOgMed,
    LocalDate tilOgMed,
    boolean gjelderDelerAvMåned,
    LocalDateTime opprettetTidspunkt
) implements BigQueryRecord {
}
