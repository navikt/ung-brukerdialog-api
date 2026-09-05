package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.soknad.OpprettSøknadHendelseRequest;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.sak.fagsak.FagSakEntitet;
import no.nav.ung.brukerdialog.sak.fagsak.FagsakRepository;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Dependent
public class SøknadHendelseTjeneste {

    private static final Logger log = LoggerFactory.getLogger(SøknadHendelseTjeneste.class);

    private final SøknadHendelseRepository repository;
    private final FagsakRepository fagsakRepository;


    @Inject
    public SøknadHendelseTjeneste(SøknadHendelseRepository repository, FagsakRepository fagsakRepository) {
        this.repository = repository;
        this.fagsakRepository = fagsakRepository;
    }

    public void registrer(AktørId aktørId, FagsakYtelseType ytelseType, OpprettSøknadHendelseRequest request) {
        List<SøknadHendelseEntitet> tidligereSøknader = repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, ytelseType);

        if (tidligereSøknader.stream().anyMatch(it -> it.getSøknadId().equals(request.søknadId()))) {
            log.info("Søknadshendelse for søknadId={} er allerede registrert.", request.søknadId());
            return;
        }

        var tilgjengeligeSøknad = utled(tidligereSøknader, aktørId, ytelseType);
        if (tilgjengeligeSøknad.type() == TilgjengeligSøknadType.INGEN) {
            throw new SøknadIkkeTilgjengeligException(tidligereSøknader.stream()
                .filter(hendelse -> hendelse.getMottattIFagsak() == null)
                .findFirst()
                .map(hendelse -> "Bruker har allerede en ubehandlet søknad mottatt " + hendelse.getMottatt() + ".")
                .orElse("Bruker kan ikke sende søknad nå."));
        }

        repository.lagre(new SøknadHendelseEntitet(request.søknadId(), aktørId, ytelseType, request.mottatt()));
        log.info("Registrerte søknadshendelse for søknadId={}.", request.søknadId());
    }

    public TilgjengeligSøknadResponse finnTilgjengeligSøknad(AktørId aktørId, FagsakYtelseType ytelseType) {
        List<SøknadHendelseEntitet> søknader = repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, ytelseType);
        return TilgjengeligSøknadUtleder.utled(LocalDate.now(), søknader, finnFagsak(aktørId, ytelseType));
    }

    private TilgjengeligSøknadResponse utled(List<SøknadHendelseEntitet> søknader, AktørId aktørId, FagsakYtelseType ytelseType) {
        return TilgjengeligSøknadUtleder.utled(
            LocalDate.now(),
            søknader,
            finnFagsak(aktørId, ytelseType));
    }

    private FagSakEntitet finnFagsak(AktørId aktørId, FagsakYtelseType ytelseType) {
        List<FagSakEntitet> fagsaker = fagsakRepository.hentForAktørOgYtelse(aktørId, ytelseType);
        if (fagsaker.size() > 1) {
            log.warn("Fant flere fagsaker på aktør med saksnummer {}. Bruker den nyeste.", fagsaker.stream().map(FagSakEntitet::getSaksnummer).toList());
        }
        return fagsaker.stream().findFirst().orElse(null);
    }
}
