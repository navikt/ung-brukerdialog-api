package no.nav.ung.brukerdialog.oppgave.journalforing;

public enum Journalposttittel {

    UNGDOMSYTELSE("ungdomsprogramytelsen"),
    AKTIVITETSPENGER("aktivitetspenger");

    /** Felles prefiks - journalposten er alltid et forhåndsvarsel, uansett ytelse. */
    private static final String PREFIX = "Forhåndsvarsel om ";

    private final String tittel;

    Journalposttittel(String ytelsenavn) {
        this.tittel = PREFIX + ytelsenavn;
    }

    public String getTittel() {
        return tittel;
    }
}
