package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.fagsak.FagsakEntitet;
import no.nav.ung.brukerdialog.sak.fagsak.VedtakPeriodeEntitet;
import no.nav.ung.brukerdialog.tid.DatoIntervallEntitet;

import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;

@Dependent
public class TilgjengeligSøknadUtleder {

    private final Period vinduÅpnerFørTom;
    private final Period vinduLukkerEtterTom;

    @Inject
    TilgjengeligSøknadUtleder(@KonfigVerdi(value = "SOKNADSVINDU_APNER_FOR_TOM", defaultVerdi = "P4W") Period vinduÅpnerFørTom,
                              @KonfigVerdi(value = "SOKNADSVINDU_LUKKER_ETTER_TOM", defaultVerdi = "P52W") Period vinduLukkerEtterTom) {
        this.vinduÅpnerFørTom = vinduÅpnerFørTom;
        this.vinduLukkerEtterTom = vinduLukkerEtterTom;
    }

    TilgjengeligSøknadResponse utled(LocalDate iDag,
                                     List<SøknadHendelseEntitet> søknader,
                                     FagsakEntitet fagsak) {


        var sisteInnvilgedeTom = sisteInnvilgedeTom(fagsak);
        boolean harInnsyn = sisteInnvilgedeTom != null;
        boolean harUbehandletSøknad = søknader.stream().anyMatch(h -> h.getMottattIFagsak() == null);

        if (harUbehandletSøknad) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.INGEN, true, harInnsyn);
        }

        // Har ingen søknader eller bare behandlede søknader

        // Ingen sak eller aldri fått innvilget ytelse
        if (sisteInnvilgedeTom == null) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD, false, false);
        }

        // Har minst en innvilget periode

        LocalDate vinduÅpner = sisteInnvilgedeTom.minus(vinduÅpnerFørTom);
        LocalDate vinduLukker = sisteInnvilgedeTom.plus(vinduLukkerEtterTom);

        if (iDag.isBefore(vinduÅpner)) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.INGEN, false, true);
        }
        //I dag er før eller dagen vinduet lukker
        if (!iDag.isAfter(vinduLukker)) {
            return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.NY_PERIODE_SØKNAD, false, true);
        }
        //I dag er etter vinduet for ny periode. Får da søke som førstegangsbehandling.
        return new TilgjengeligSøknadResponse(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD, false, true);
    }

    private static LocalDate sisteInnvilgedeTom(FagsakEntitet fagsak) {
        if (fagsak == null) {
            return null;
        }

        return fagsak.getAktivePerioder().stream()
            .filter(it -> it.getResultat() == VedtakResultatType.INNVILGET)
            .map(VedtakPeriodeEntitet::getPeriode)
            .map(DatoIntervallEntitet::getTomDato)
            .max(Comparator.naturalOrder())
            .orElse(null);
    }

}
