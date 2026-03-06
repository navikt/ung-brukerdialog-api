package no.nav.ung.brukerdialog.web.app.tjenester;

import no.nav.k9.prosesstask.rest.ProsessTaskRestTjeneste;
import no.nav.ung.brukerdialog.web.app.tjenester.oppgavebehandling.MigrerBrukerdialogOppgaverRestTjeneste;
import no.nav.ung.brukerdialog.web.app.tjenester.oppgavebehandling.OppgavebehandlingRestTjeneste;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InternRestClasses implements RestClasses {
    @Override
    public Set<Class<?>> getRestClasses() {
        Set<Class<?>> classes = new HashSet<>(List.of(ProsessTaskRestTjeneste.class,
            MigrerBrukerdialogOppgaverRestTjeneste.class,
            OppgavebehandlingRestTjeneste.class));
        return Set.copyOf(classes);
    }
}
