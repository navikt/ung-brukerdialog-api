package no.nav.ung.brukerdialog.sak.fagsak;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.vedtak.MottaFagsakRequest;
import no.nav.ung.brukerdialog.kontrakt.vedtak.MottattSøknadDto;
import no.nav.ung.brukerdialog.sak.FagsakYtelseType;
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

    public void motta(FagsakYtelseType ytelseType, MottaFagsakRequest request) {
        FagsakEntitet fagsak = fagsakRepository.lagreFagsak(request.aktørId(), ytelseType, request.saksnummer(), request.vedtakPerioder());

        Set<UUID> mottatteSøknadIder = request.mottatteSøknader().stream()
            .map(MottattSøknadDto::søknadId)
            .collect(Collectors.toSet());
        søknadHendelseRepository.markerMottattIFagsak(request.aktørId(), ytelseType, fagsak, mottatteSøknadIder);

        log.info("Mottok fagsakinfo for saksnummer={} med {} perioder.",
            request.saksnummer().getVerdi(), request.vedtakPerioder().size());
    }

}
