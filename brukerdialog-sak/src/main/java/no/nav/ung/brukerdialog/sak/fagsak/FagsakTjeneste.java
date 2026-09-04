package no.nav.ung.brukerdialog.sak.fagsak;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.vedtak.FagSakRequest;
import no.nav.ung.brukerdialog.kontrakt.vedtak.MottattSøknadDto;
import no.nav.ung.brukerdialog.sak.soknad.FagsakYtelseType;
import no.nav.ung.brukerdialog.sak.soknad.SøknadHendelseEntitet;
import no.nav.ung.brukerdialog.sak.soknad.SøknadHendelseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        FagSakEntitet fagsak = new FagSakEntitet(request.aktørId(), ytelseType, request.saksnummer());
        fagsakRepository.lagre(fagsak);

        List<SøknadHendelseEntitet> søknader = søknadHendelseRepository.hentAktiveSøknadForAktørOgYtelse(request.aktørId(), ytelseType)
            .stream().filter(it -> it.getMottattIFagsak() == null)
            .toList();

        var mottatteSøknadId = request.mottatteSøknader().stream().map(MottattSøknadDto::søknadId).toList();
        søknader.stream().filter(it -> mottatteSøknadId.contains(it.getSøknadId()))
            .forEach(søknad -> {
                søknad.markerMottattIFagsak(fagsak);
                søknadHendelseRepository.lagre(søknad);
            });

    }

}
