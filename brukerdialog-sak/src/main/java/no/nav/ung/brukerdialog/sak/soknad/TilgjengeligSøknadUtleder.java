package no.nav.ung.brukerdialog.sak.soknad;

import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;

import java.util.List;

class TilgjengeligSøknadUtleder {

    static TilgjengeligSøknadResponse utled(List<SøknadHendelseEntitet> tidligereSøknader) {
        //Tillater nye søknader til del 2 er på plass for å forenkle testing.
        if (true) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD, false, false);
        }

        if (!tidligereSøknader.isEmpty()) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.INGEN, true, false);
        }
        return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD, false, false);
    }
}
