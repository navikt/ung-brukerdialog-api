package no.nav.ung.brukerdialog.oppgave.journalforing;

/**
 * Kastes når journalføring av en brukerdialogoppgave mot Dokarkiv feiler på en måte som ikke
 * skal føre til automatisk retry via {@code ProsessTask}, eller når en forutsetning for
 * journalføring mangler. Brukes blant annet ved:
 * <ul>
 *   <li>HTTP 409 fra Dokarkiv - journalposten finnes allerede, men {@code journalpostId} kan
 *       ikke leses fra responsen med dagens klient. Tasken skal ende i
 *       {@code FEILET} og følges opp manuelt, ikke retrye i det uendelige.</li>
 *   <li>Personen mangler folkeregisterident i PDL.</li>
 * </ul>
 * <p>
 * Meldingen skal ALDRI inneholde fødselsnummer, aktørId eller annet PII - kun
 * oppgavereferanse og oppgavetype.
 */
public class JournalføringException extends RuntimeException {

    public JournalføringException(String message) {
        super(message);
    }

    public JournalføringException(String message, Throwable cause) {
        super(message, cause);
    }
}
