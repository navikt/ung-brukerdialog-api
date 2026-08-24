package no.nav.ung.brukerdialog.oppgave.journalforing;

/**
 * Status for journalføring av en brukerdialogoppgave. Se {@link OppgaveJournalføringEntitet}.
 */
public enum JournalføringStatus {
    /** Raden er opprettet, men journalpost er ikke opprettet i Dokarkiv ennå. */
    PLANLAGT,
    /** Journalpost er opprettet og ferdigstilt i Dokarkiv. */
    JOURNALFORT
}
