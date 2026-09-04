package no.nav.ung.brukerdialog.sak.soknad;

public class SøknadIkkeTilgjengeligException extends RuntimeException {

    public SøknadIkkeTilgjengeligException(String årsak) {
        super(årsak);
    }
}
