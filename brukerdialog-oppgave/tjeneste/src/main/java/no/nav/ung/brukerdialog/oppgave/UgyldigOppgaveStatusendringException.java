package no.nav.ung.brukerdialog.oppgave;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;

import java.util.UUID;

public class UgyldigOppgaveStatusendringException extends RuntimeException {

    public UgyldigOppgaveStatusendringException(UUID oppgavereferanse, OppgaveStatus fraStatus, OppgaveStatus tilStatus) {
        super("Oppgaven med referanse " + oppgavereferanse + " kan ikke settes til " + tilStatus
            + " fordi den har status " + fraStatus + ". Kun oppgaver med status " + OppgaveStatus.ULØST + " kan endres.");
    }
}
