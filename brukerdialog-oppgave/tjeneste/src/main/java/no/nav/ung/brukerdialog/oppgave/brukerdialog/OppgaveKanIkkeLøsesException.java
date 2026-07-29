package no.nav.ung.brukerdialog.oppgave.brukerdialog;

import java.util.UUID;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;

/**
 * Kastes når en oppgave forsøkes løst, men den ikke lenger er i status {@link OppgaveStatus#ULØST}.
 * Mappes til HTTP 409 slik at klienten ikke kjører oppfølgingshandlinger for et duplikat.
 */
public class OppgaveKanIkkeLøsesException extends RuntimeException {

    public OppgaveKanIkkeLøsesException(UUID oppgavereferanse, OppgaveStatus status) {
        super("Oppgaven med referanse " + oppgavereferanse + " kan ikke løses fordi den har status " + status
            + ". Kun oppgaver med status " + OppgaveStatus.ULØST + " kan løses.");
    }
}
