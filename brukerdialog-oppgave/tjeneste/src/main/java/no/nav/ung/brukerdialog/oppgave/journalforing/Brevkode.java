package no.nav.ung.brukerdialog.oppgave.journalforing;


public enum Brevkode {

    UNGDOMSYTELSE("ungdomsytelsen"),
    AKTIVITETSPENGER("aktivitetspenger");

    /** Felles prefiks - selve forhåndsvarsel-klassifiseringen er lik på tvers av ytelser. */
    private static final String PREFIX = "FVL – forhåndsvarsel – ";

    private final String kode;

    Brevkode(String ytelsenavn) {
        this.kode = PREFIX + ytelsenavn;
    }

    public String getKode() {
        return kode;
    }
}
