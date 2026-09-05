package no.nav.ung.brukerdialog.sak.fagsak;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.vedtak.FagSakRequest;
import no.nav.ung.brukerdialog.kontrakt.vedtak.MottattSøknadDto;
import no.nav.ung.brukerdialog.sak.soknad.FagsakYtelseType;
import no.nav.ung.brukerdialog.sak.soknad.SøknadHendelseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Dependent
public class FagsakTjeneste {

    private static final Logger log = LoggerFactory.getLogger(FagsakTjeneste.class);

    private final FagsakRepository fagsakRepository;
    private final SøknadHendelseRepository søknadHendelseRepository;

    @Inject
    public FagsakTjeneste(FagsakRepository fagsakRepository,
                          SøknadHendelseRepository søknadHendelseRepository) {
        this.fagsakRepository = fagsakRepository;
        this.søknadHendelseRepository = søknadHendelseRepository;
    }

    public void motta(FagsakYtelseType ytelseType, FagSakRequest request) {
        FagSakEntitet fagsak = fagsakRepository.hentForSaksnummer(request.saksnummer())
            .orElseGet(() -> new FagSakEntitet(request.aktørId(), ytelseType, request.saksnummer()));
        validerUendretEier(request, ytelseType, fagsak);

        fagsak.erstattPerioder(request.vedtakPerioder());
        fagsakRepository.lagre(fagsak);

        koblSøknaderTilFagsak(request, ytelseType, fagsak);

        log.info("Mottok fagsakinfo for saksnummer={} med {} perioder.",
            request.saksnummer().getVerdi(), request.vedtakPerioder().size());
    }

    private void koblSøknaderTilFagsak(FagSakRequest request, FagsakYtelseType ytelseType, FagSakEntitet fagsak) {
        Set<UUID> mottatteSøknadIder = request.mottatteSøknader().stream()
            .map(MottattSøknadDto::søknadId)
            .collect(Collectors.toSet());
        if (mottatteSøknadIder.isEmpty()) {
            return;
        }

        søknadHendelseRepository.hentAktiveSøknadForAktørOgYtelse(request.aktørId(), ytelseType).stream()
            .filter(søknad -> søknad.getMottattIFagsak() == null)
            .filter(søknad -> mottatteSøknadIder.contains(søknad.getSøknadId()))
            .forEach(søknad -> {
                søknad.markerMottattIFagsak(fagsak);
                søknadHendelseRepository.lagre(søknad);
            });
    }

    private static void validerUendretEier(FagSakRequest request, FagsakYtelseType ytelseType, FagSakEntitet fagsak) {
        if (!fagsak.getAktørId().equals(request.aktørId())) {
            throw new IllegalArgumentException(
                "Saksnummer " + request.saksnummer().getVerdi() + " er registrert på en annen aktør enn den i meldingen.");
        }
        if (fagsak.getYtelseType() != ytelseType) {
            throw new IllegalArgumentException(
                "Saksnummer " + request.saksnummer().getVerdi() + " er registrert på ytelse " + fagsak.getYtelseType()
                    + ", men meldingen gjelder " + ytelseType + ".");
        }
    }
}
