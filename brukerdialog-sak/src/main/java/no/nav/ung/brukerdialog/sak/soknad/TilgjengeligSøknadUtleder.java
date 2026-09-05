package no.nav.ung.brukerdialog.sak.soknad;

import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.fagsak.FagSakEntitet;
import no.nav.ung.brukerdialog.sak.fagsak.VedtakPeriodeEntitet;
import no.nav.ung.brukerdialog.tid.DatoIntervallEntitet;

import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

class TilgjengeligSøknadUtleder {

    static final Period VINDU_ÅPNER_FØR_TOM = Period.ofWeeks(4);

    static final Period VINDU_LUKKER_ETTER_TOM = Period.ofWeeks(52);

    static TilgjengeligSøknadResponse utled(LocalDate iDag,
                                            List<SøknadHendelseEntitet> søknader,
                                            FagSakEntitet fagsak) {

        boolean harUbehandletSøknad = søknader.stream().anyMatch(h -> h.getMottattIFagsak() == null);

        // Ung-sak har aldri meldt inn noe på denne brukeren. En registrert søknad ligger da fortsatt til
        // behandling, og er noe annet enn en sak uten innvilgelse.
        if (fagsak == null) {
            return new TilgjengeligSøknadResponse(
                søknader.isEmpty() ? TilgjengeligSøknadType.FØRSTEGANGSSØKNAD : TilgjengeligSøknadType.INGEN,
                harUbehandletSøknad,
                false
            );
        }

        Optional<LocalDate> sisteInnvilgedeTom = sisteInnvilgedeTom(fagsak);
        boolean harInnsyn = sisteInnvilgedeTom.isPresent();

        if (harUbehandletSøknad) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.INGEN, true, harInnsyn);
        }

        // Sak uten innvilgelse, typisk fullt avslag. Bruker kan søke på nytt som førstegangssøker.
        if (sisteInnvilgedeTom.isEmpty()) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD, false, false);
        }

        // Fra og med her gjelder regelen uavhengig av om det finnes søknadshendelser. En bruker med
        // løpende innvilgelse skal ikke få førstegangssøknad selv om hendelsen mangler, slik den gjør
        // for saker fra før søknadshendelser ble registrert.
        LocalDate tom = sisteInnvilgedeTom.get();
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

    private static Optional<LocalDate> sisteInnvilgedeTom(FagSakEntitet fagsak) {
        return fagsak.getAktivePerioder().stream()
            .filter(it -> it.getResultat() == VedtakResultatType.INNVILGET)
            .map(VedtakPeriodeEntitet::getPeriode)
            .map(DatoIntervallEntitet::getTomDato)
            .max(Comparator.naturalOrder());
    }

}
