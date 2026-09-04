package no.nav.ung.brukerdialog.sak.soknad;

import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.fagsak.FagSakEntitet;
import no.nav.ung.brukerdialog.sak.fagsak.VedtakPeriodeEntitet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

class TilgjengeligSøknadUtleder {

    private static final Logger log = LoggerFactory.getLogger(TilgjengeligSøknadUtleder.class);

    static final Period VINDU_ÅPNER_FØR_TOM = Period.ofWeeks(4);

    static final Period VINDU_LUKKER_ETTER_TOM = Period.ofWeeks(52);

    static TilgjengeligSøknadResponse utled(LocalDate iDag,
                                            List<SøknadHendelseEntitet> søknader,
                                            FagSakEntitet fagsak) {

        if (fagsak == null) {
            var harUbehandletSøknad = false;
            var harInnsyn = false;
            return new TilgjengeligSøknadResponse(
                søknader.isEmpty() ? TilgjengeligSøknadType.FØRSTEGANGSSØKNAD : TilgjengeligSøknadType.INGEN,
                harUbehandletSøknad,
                harInnsyn
            );
        }

        var sisteInnvilgedePeriode = fagsak.getPerioder().stream()
            .filter(it -> it.getResultat() == VedtakResultatType.INNVILGET)
            .map(VedtakPeriodeEntitet::getPeriode)
            .findFirst();
        boolean harInnsyn = sisteInnvilgedePeriode.isPresent();

        boolean harUbehandletSøknad = søknader.stream().anyMatch(h -> h.getMottattIFagsak() == null);
        if (harUbehandletSøknad) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.INGEN, true, harInnsyn);
        }

        // Sak uten innvilgelse, typisk fullt avslag. Bruker kan søke på nytt som førstegangssøker.
        if (sisteInnvilgedePeriode.isEmpty()) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD, false, false);
        }

        // Fra og med her gjelder regelen uavhengig av om det finnes søknadshendelser. En bruker med
        // løpende innvilgelse skal ikke få førstegangssøknad selv om hendelsen mangler, slik den gjør
        // for saker fra før søknadshendelser ble registrert.
        LocalDate tom = sisteInnvilgedePeriode.get().getTomDato();
        LocalDate vinduÅpner = tom.minus(VINDU_ÅPNER_FØR_TOM);
        LocalDate vinduLukker = tom.plus(VINDU_LUKKER_ETTER_TOM);

        if (iDag.isBefore(vinduÅpner)) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.INGEN, false, true);
        }
        if (!iDag.isAfter(vinduLukker)) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.NY_PERIODE_SØKNAD, false, true);
        }
        return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD, false, true);
    }

}
