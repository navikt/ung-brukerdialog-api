package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.soknad.OpprettSøknadHendelseRequest;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadDto;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class SøknadHendelseTjeneste {

    private static final Logger log = LoggerFactory.getLogger(SøknadHendelseTjeneste.class);

    private final SøknadHendelseRepository repository;

    @Inject
    public SøknadHendelseTjeneste(SøknadHendelseRepository repository) {
        this.repository = repository;
    }

    public void registrer(AktørId aktørId, FagsakYtelseType ytelseType, OpprettSøknadHendelseRequest request) {
        if (repository.hentForSøknadId(request.søknadId()).isPresent()) {
            log.info("Søknadshendelse for søknadId={} er allerede registrert.", request.søknadId());
            return;
        }

        var tidligereSøknader = repository.hentForAktørOgYtelse(aktørId, ytelseType);
        if (TilgjengeligSøknadUtleder.utled(tidligereSøknader).type() == TilgjengeligSøknadType.INGEN) {
            throw new SøknadIkkeTilgjengeligException(tidligereSøknader.stream()
                .findFirst()
                .map(s -> "Deltakeren kan ikke sende søknad nå. Det finnes allerede en registrert søknad, innsendt " + s.getMottatt() + ".")
                .orElse("Deltakeren kan ikke sende søknad nå."));
        }

        var entitet = new SøknadHendelseEntitet(request.søknadId(), aktørId, ytelseType, request.mottatt());
        repository.lagre(entitet);
        log.info("Registrerte søknadshendelse for søknadId={}.", request.søknadId());
    }

    public TilgjengeligSøknadDto finnTilgjengeligSøknad(AktørId aktørId, FagsakYtelseType ytelseType) {
        return TilgjengeligSøknadUtleder.utled(repository.hentForAktørOgYtelse(aktørId, ytelseType));
    }
}
