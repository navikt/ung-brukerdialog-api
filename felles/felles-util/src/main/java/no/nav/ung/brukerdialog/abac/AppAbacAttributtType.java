package no.nav.ung.brukerdialog.abac;


import no.nav.k9.felles.sikkerhet.abac.AbacAttributtType;

/**
 * AbacAttributtTyper brukes i applikasjonen for å utlede hva som er relevant å sende til PDP for tilgangskontroll
 */
public enum AppAbacAttributtType implements AbacAttributtType {

    DOKUMENT_ID,
    /**
     * referanse generert av konsument
     */
    OPPGAVE_EKSTERN_REFERANSE,
    SAKER_MED_FNR(true),
    SAKER_MED_AKTØR_ID(true),

    YTELSETYPE;

    private final boolean maskerOutput;

    AppAbacAttributtType() {
        this.maskerOutput = false;
    }

    AppAbacAttributtType(boolean maskerOutput) {
        this.maskerOutput = maskerOutput;
    }

    @Override
    public boolean getMaskerOutput() {
        return maskerOutput;
    }

}
