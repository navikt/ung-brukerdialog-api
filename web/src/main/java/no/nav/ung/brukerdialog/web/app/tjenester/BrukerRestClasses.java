package no.nav.ung.brukerdialog.web.app.tjenester;

import no.nav.ung.brukerdialog.web.app.tjenester.bruker.BrukerOppgaveRestTjeneste;

import java.util.Set;

public class BrukerRestClasses implements RestClasses {
    @Override
    public Set<Class<?>> getRestClasses() {
        return Set.of(BrukerOppgaveRestTjeneste.class);
    }
}
