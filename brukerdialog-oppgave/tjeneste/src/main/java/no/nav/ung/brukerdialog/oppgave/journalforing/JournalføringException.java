package no.nav.ung.brukerdialog.oppgave.journalforing;

/**
 * Kastes når journalføring feiler på en måte som ikke skal retryes automatisk, f.eks. når en
 * forutsetning mangler (person uten folkeregisterident i PDL).
 */
public class JournalføringException extends RuntimeException {

    public JournalføringException(String message) {
        super(message);
    }

    public JournalføringException(String message, Throwable cause) {
        super(message, cause);
    }
}
