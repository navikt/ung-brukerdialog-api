package no.nav.ung.brukerdialog.sak.soknad;

import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadDto;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;

import java.util.List;

class TilgjengeligSøknadUtleder {

    static TilgjengeligSøknadDto utled(List<SøknadHendelseEntitet> tidligereSøknader) {
        //Tillater nye søknader til del 2 er på plass for å forenkle testing.
        if (true) {
            return new TilgjengeligSøknadDto(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD, false, false);
        }

        if (!tidligereSøknader.isEmpty()) {
            return new TilgjengeligSøknadDto(TilgjengeligSøknadType.INGEN, true, false);
        }
        return new TilgjengeligSøknadDto(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD, false, false);
    }
}
