package no.nav.ung.brukerdialog.oppgave.journalforing;

/**
 * Kastes når journalføring feiler på en måte som ikke skal retryes automatisk (f.eks. HTTP 409
 * fra Dokarkiv - journalposten finnes, men {@code journalpostId} kan ikke leses), eller når en
 * forutsetning mangler (f.eks. person uten folkeregisterident i PDL).
 * <p>
 * Meldingen skal ALDRI inneholde fødselsnummer, aktørId eller annet PII - kun oppgavereferanse
 * og oppgavetype.
 */
public class JournalføringException extends RuntimeException {

    public JournalføringException(String message) {
        super(message);
    }

    public JournalføringException(String message, Throwable cause) {
        super(message, cause);
    }
}
