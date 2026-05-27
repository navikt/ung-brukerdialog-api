package no.nav.ung.brukerdialog.kontrakt.oppgaver;

public enum OppgaveType {

    BEKREFT_ENDRET_STARTDATO,
    BEKREFT_ENDRET_SLUTTDATO,
    BEKREFT_ENDRET_PERIODE,
    BEKREFT_AVVIK_REGISTERINNTEKT,
    RAPPORTER_INNTEKT(true),
    SØK_YTELSE,
    BEKREFT_BOSTED,
    BEKREFT_OPPHOR_VED_MAKSDATO;

    private final boolean kreverPeriode;

    OppgaveType() {
        this.kreverPeriode = false;
    }

    OppgaveType(boolean kreverPeriode) {
        this.kreverPeriode = kreverPeriode;
    }

    /**
     * Returnerer true dersom denne oppgavetypen krever at fomDato og tomDato er satt
     * ved endring av status via {@code EndreOppgaveStatusDto}.
     */
    public boolean kreverPeriode() {
        return kreverPeriode;
    }
}
