package no.nav.ung.brukerdialog.oppgave.journalforing;

/**
 * Dokarkiv-tema. Utledes fra oppgavens
 * {@link no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype} - kan ikke sendes inn av
 * kalleren (se {@link JournalføringParametre#utled}).
 */
public enum Tema {

    UNG("Ungdomsytelsen");
    // TODO: OMS ("Omsorgs-, pleie- og opplæringspenger") når K9-ytelser skal støttes.
    // Se JournalføringParametre.utled(...).

    private final String visningsnavn;

    Tema(String visningsnavn) {
        this.visningsnavn = visningsnavn;
    }

    public String getVisningsnavn() {
        return visningsnavn;
    }
}
