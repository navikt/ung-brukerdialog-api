package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.soknad.OpprettSøknadHendelseRequest;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Dependent
public class SøknadHendelseTjeneste {

    private static final Logger log = LoggerFactory.getLogger(SøknadHendelseTjeneste.class);

    private final SøknadHendelseRepository repository;

    @Inject
    public SøknadHendelseTjeneste(SøknadHendelseRepository repository) {
        this.repository = repository;
    }

    public void registrer(AktørId aktørId, FagsakYtelseType ytelseType, OpprettSøknadHendelseRequest request) {
        List<SøknadHendelseEntitet> tidligereSøknader = repository.hentAktivSøknadForAktørOgYtelse(aktørId, ytelseType);

        if (tidligereSøknader.stream().anyMatch(it -> it.getSøknadId().equals(request.søknadId()))) {
            log.info("Søknadshendelse for søknadId={} er allerede registrert.", request.søknadId());
            return;
        }

        if (TilgjengeligSøknadUtleder.utled(tidligereSøknader).type() == TilgjengeligSøknadType.INGEN) {
            throw new SøknadIkkeTilgjengeligException(tidligereSøknader.stream()
                .findFirst()
                .map(s -> "Bruker kan ikke sende søknad nå. Det finnes allerede en registrert søknad, innsendt " + s.getMottatt() + ".")
                .orElse("Bruker kan ikke sende søknad nå."));
        }

        var entitet = new SøknadHendelseEntitet(request.søknadId(), aktørId, ytelseType, request.mottatt());
        repository.lagre(entitet);
        log.info("Registrerte søknadshendelse for søknadId={}.", request.søknadId());
    }

    public TilgjengeligSøknadResponse finnTilgjengeligSøknad(AktørId aktørId, FagsakYtelseType ytelseType) {
        return TilgjengeligSøknadUtleder.utled(repository.hentAktivSøknadForAktørOgYtelse(aktørId, ytelseType));
    }
}
