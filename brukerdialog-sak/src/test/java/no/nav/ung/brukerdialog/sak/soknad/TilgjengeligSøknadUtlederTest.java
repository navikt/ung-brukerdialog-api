package no.nav.ung.brukerdialog.sak.soknad;

import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakPeriodeDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.fagsak.FagSakEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Periode;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TilgjengeligSøknadUtlederTest {

    private static final LocalDate _30juni2025 = LocalDate.of(2025, 6, 30);
    private static final LocalDate VINDU_ÅPNER_2juni2025 = _30juni2025.minusWeeks(4);
    private static final LocalDate VINDU_LUKKER_30juni2026 = _30juni2025.plusWeeks(52);
    private static final FagsakYtelseType YTELSE = FagsakYtelseType.AKTIVITETSPENGER;

    @Test
    void ingen_søknad_og_ingen_sak_gir_førstegangssøknad() {
        LocalDate iDag = LocalDate.of(2025, 1, 1);
        var resultat = utled(iDag, List.of(), null);

        assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);
        assertThat(resultat.harUbehandletSøknad()).isFalse();
        assertThat(resultat.harInnsyn()).isFalse();
    }

    @Test
    void ubehandlet_søknad_sperrer_uten_sak() {
        LocalDate iDag = LocalDate.of(2025, 1, 1);
        var resultat = utled(iDag, List.of(ubehandletSøknad()), null);

        assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
        assertThat(resultat.harUbehandletSøknad()).isTrue();
        assertThat(resultat.harInnsyn()).isFalse();
    }

    @Test
    void fullt_avslag_gir_ny_førstegangssøknad() {
        FagSakEntitet fagsak = fagsak(avslåttEtÅrTom(_30juni2025));
        LocalDate iDag = LocalDate.of(2025, 1, 1);
        var resultat = utled(iDag, List.of(behandletSøknad(fagsak)), fagsak);

        assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);
        assertThat(resultat.harUbehandletSøknad()).isFalse();
        assertThat(resultat.harInnsyn()).isFalse();
    }

    @Test
    void ubehandlet_søknad_sperrer_selv_med_full_avslag_sak() {
        LocalDate iDag = LocalDate.of(2025, 1, 1);
        var resultat = utled(iDag, List.of(ubehandletSøknad()), fagsak(avslåttEtÅrTom(_30juni2025)));

        assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
        assertThat(resultat.harUbehandletSøknad()).isTrue();
        assertThat(resultat.harInnsyn()).isFalse();
    }

    @Test
    void innvilgelse_i_vinduet_gir_forlengelse_med_innsyn() {
        FagSakEntitet fagsak = fagsak(innvilgetEtÅrTom(_30juni2025));
        LocalDate iDag = VINDU_ÅPNER_2juni2025;
        var resultat = utled(iDag, List.of(behandletSøknad(fagsak)), fagsak);

        assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.NY_PERIODE_SØKNAD);
        assertThat(resultat.harUbehandletSøknad()).isFalse();
        assertThat(resultat.harInnsyn()).isTrue();
    }

    @Test
    void innvilgelse_gir_innsyn_selv_når_brukeren_ikke_kan_søke() {
        FagSakEntitet fagsak = fagsak(innvilgetEtÅrTom(_30juni2025));
        var resultat = utled(VINDU_ÅPNER_2juni2025.minusDays(1), List.of(behandletSøknad(fagsak)), fagsak);

        assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
        assertThat(resultat.harUbehandletSøknad()).isFalse();
        assertThat(resultat.harInnsyn()).isTrue();
    }

    @Test
    void førstegangssøknad_etter_tom_beholder_innsyn_i_den_gamle_saken() {
        FagSakEntitet fagsak = fagsak(innvilgetEtÅrTom(_30juni2025));
        var resultat = utled(VINDU_LUKKER_30juni2026.plusDays(1), List.of(behandletSøknad(fagsak)), fagsak);

        assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);
        assertThat(resultat.harUbehandletSøknad()).isFalse();
        assertThat(resultat.harInnsyn()).isTrue();
    }

    @Test
    void ny_søknad_som_ennå_ikke_er_behandlet_sperrer_selv_midt_i_vinduet() {
        FagSakEntitet fagsak = fagsak(innvilgetEtÅrTom(_30juni2025));
        var resultat = utled(_30juni2025, List.of(behandletSøknad(fagsak), ubehandletSøknad()), fagsak);

        assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
        assertThat(resultat.harUbehandletSøknad()).isTrue();
        assertThat(resultat.harInnsyn()).isTrue();
    }

    @Test
    void løpende_innvilgelse_uten_søknadshendelse_gir_ikke_førstegangssøknad() {
        FagSakEntitet fagsak = fagsak(innvilgetEtÅrTom(_30juni2025));
        var resultat = utled(_30juni2025, List.of(), fagsak);

        assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.NY_PERIODE_SØKNAD);
    }

    @Test
    void siste_innvilgede_tom_hentes_på_tvers_av_perioder_og_ignorerer_avslag() {
        var fagsak = fagsak(
            innvilgetEtÅrTom(_30juni2025.minusYears(1)),
            innvilgetEtÅrTom(_30juni2025),
            avslåttEtÅrTom(_30juni2025.plusYears(1)));

        var resultat = utled(_30juni2025, List.of(behandletSøknad(fagsak)), fagsak);

        assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.NY_PERIODE_SØKNAD);
    }

    @ParameterizedTest(name = "{1} dager fra vindusåpning gir {2}")
    @CsvSource({
        "-1, dagen før vinduet åpner,      INGEN",
        "0,  dagen vinduet åpner,          NY_PERIODE_SØKNAD",
        "1,  dagen etter vinduet åpner,    NY_PERIODE_SØKNAD"
    })
    void vindusåpning_er_inklusiv(long dagerFraÅpning, String beskrivelse, TilgjengeligSøknadType forventet) {
        var resultat = utled(VINDU_ÅPNER_2juni2025.plusDays(dagerFraÅpning), List.of(behandletSøknad(fagsak())), fagsak(innvilgetEtÅrTom(_30juni2025)));

        assertThat(resultat.type()).as(beskrivelse).isEqualTo(forventet);
    }

    @ParameterizedTest(name = "{1} gir {2}")
    @CsvSource({
        "-1, dagen før vinduet lukker,   NY_PERIODE_SØKNAD",
        "0,  dagen vinduet lukker,       NY_PERIODE_SØKNAD",
        "1,  dagen etter vinduet lukker, FØRSTEGANGSSØKNAD"
    })
    void vinduslukking_er_inklusiv(long dagerFraLukking, String beskrivelse, TilgjengeligSøknadType forventet) {
        var resultat = utled(VINDU_LUKKER_30juni2026.plusDays(dagerFraLukking), List.of(behandletSøknad(fagsak())), fagsak(innvilgetEtÅrTom(_30juni2025)));

        assertThat(resultat.type()).as(beskrivelse).isEqualTo(forventet);
    }

    private static TilgjengeligSøknadResponse utled(LocalDate iDag, List<SøknadHendelseEntitet> hendelser, FagSakEntitet fagsak) {
        return TilgjengeligSøknadUtleder.utled(iDag, hendelser, fagsak);
    }

    private static SøknadHendelseEntitet ubehandletSøknad() {
        return new SøknadHendelseEntitet(UUID.randomUUID(), AktørId.dummy(), YTELSE, LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    private static SøknadHendelseEntitet behandletSøknad(FagSakEntitet sak123) {
        var hendelse = ubehandletSøknad();
        hendelse.markerMottattIFagsak(sak123);
        return hendelse;
    }

    private static VedtakPeriodeDto innvilgetEtÅrTom(LocalDate tom) {
        return periode(tom, VedtakResultatType.INNVILGET);
    }

    private static VedtakPeriodeDto avslåttEtÅrTom(LocalDate tom) {
        return periode(tom, VedtakResultatType.AVSLÅTT);
    }

    private static VedtakPeriodeDto periode(LocalDate tom, VedtakResultatType resultat) {
        return new VedtakPeriodeDto(new Periode(tom.minusWeeks(52).plusDays(1), tom), resultat);
    }

    private static FagSakEntitet fagsak(VedtakPeriodeDto... perioder) {
        var entitet = new FagSakEntitet(AktørId.dummy(), YTELSE, new Saksnummer("SAK123"));
        entitet.erstattPerioder(List.of(perioder));
        return entitet;
    }
}
