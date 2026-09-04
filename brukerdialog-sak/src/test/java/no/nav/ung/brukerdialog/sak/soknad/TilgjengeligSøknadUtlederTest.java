package no.nav.ung.brukerdialog.sak.soknad;

import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.sak.fagsak.FagSakEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Periode;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TilgjengeligSøknadUtlederTest {

    private static final LocalDate TOM = LocalDate.of(2025, 6, 30);
    private static final LocalDate VINDU_ÅPNER = TOM.minusWeeks(4);
    private static final LocalDate VINDU_LUKKER = TOM.plusWeeks(52);
    private static final FagsakYtelseType YTELSE = FagsakYtelseType.AKTIVITETSPENGER;

    @Nested
    class UtenVedtaksstatus {

        @Test
        void ingen_søknad_og_ingen_vedtaksstatus_gir_førstegangssøknad() {
            var resultat = utled(LocalDate.of(2025, 1, 1), List.of(), null);

            assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);
            assertThat(resultat.harUbehandletSøknad()).isFalse();
            assertThat(resultat.harInnsyn()).isFalse();
        }

        @Test
        void ubehandlet_søknad_sperrer_selv_om_ung_sak_ikke_har_meldt_inn_noe() {
            var resultat = utled(LocalDate.of(2025, 1, 1), List.of(ubehandletSøknad()), null);

            assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
            assertThat(resultat.harUbehandletSøknad()).isTrue();
            assertThat(resultat.harInnsyn()).isFalse();
        }

        @Test
        void behandlet_søknad_uten_vedtaksstatus_er_inkonsistent_og_avvises() {
            var resultat = utled(LocalDate.of(2025, 1, 1), List.of(behandletSøknad(new FagSakEntitet(AktørId.dummy(), YTELSE, new Saksnummer("SAK123")))), null);

            assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
            assertThat(resultat.harUbehandletSøknad()).isFalse();
        }
    }

    @Nested
    class MedVedtaksstatus {

        @Test
        void fullt_avslag_gir_ny_førstegangssøknad() {
            FagSakEntitet fagsak = fagsak();
            var resultat = utled(LocalDate.of(2025, 1, 1), List.of(behandletSøknad(fagsak)), fagsak);

            assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);
            assertThat(resultat.harUbehandletSøknad()).isFalse();
            assertThat(resultat.harInnsyn()).isFalse();
        }

        @Test
        void innvilgelse_gir_innsyn_selv_når_deltakeren_ikke_kan_søke() {
            FagSakEntitet fagsak = fagsak(periode(TOM));
            var resultat = utled(VINDU_ÅPNER.minusDays(1), List.of(behandletSøknad(fagsak)), fagsak);

            assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
            assertThat(resultat.harInnsyn()).isTrue();
        }

        @Test
        void ny_søknad_som_ennå_ikke_er_behandlet_sperrer_selv_midt_i_vinduet() {
            FagSakEntitet fagsak = fagsak(periode(TOM));
            var resultat = utled(TOM, List.of(behandletSøknad(fagsak), ubehandletSøknad()), fagsak);

            assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
            assertThat(resultat.harUbehandletSøknad()).isTrue();
            assertThat(resultat.harInnsyn()).isTrue();
        }

        @Test
        void løpende_innvilgelse_uten_søknadshendelse_gir_ikke_førstegangssøknad() {
            FagSakEntitet fagsak = fagsak(periode(TOM));
            var resultat = utled(TOM, List.of(), fagsak);

            assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.NY_PERIODE_SØKNAD);
        }

        @Test
        void siste_tom_hentes_på_tvers_av_perioder_i_samme_sak() {
            var fagsak = fagsak(periode(TOM.minusMonths(6)), periode(TOM));

            var resultat = utled(TOM, List.of(behandletSøknad(fagsak)), fagsak);

            assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.NY_PERIODE_SØKNAD);
        }
    }

    @Nested
    class Vindusgrenser {

        @ParameterizedTest(name = "{1} dager fra vindusåpning gir {2}")
        @CsvSource({
            "-1, dagen før vinduet åpner,      INGEN",
            "0,  dagen vinduet åpner,          NY_PERIODE_SØKNAD",
            "1,  dagen etter vinduet åpner,    NY_PERIODE_SØKNAD"
        })
        void vindusåpning_er_inklusiv(long dagerFraÅpning, String beskrivelse, TilgjengeligSøknadType forventet) {
            var resultat = utled(VINDU_ÅPNER.plusDays(dagerFraÅpning), List.of(behandletSøknad(new FagSakEntitet(AktørId.dummy(), YTELSE, new Saksnummer("SAK123")))), fagsak(periode(TOM)));

            assertThat(resultat.type()).as(beskrivelse).isEqualTo(forventet);
        }

        @ParameterizedTest(name = "{1} gir {2}")
        @CsvSource({
            "-1, dagen før vinduet lukker,   NY_PERIODE_SØKNAD",
            "0,  dagen vinduet lukker,       NY_PERIODE_SØKNAD",
            "1,  dagen etter vinduet lukker, FØRSTEGANGSSØKNAD"
        })
        void vinduslukking_er_inklusiv(long dagerFraLukking, String beskrivelse, TilgjengeligSøknadType forventet) {
            var resultat = utled(VINDU_LUKKER.plusDays(dagerFraLukking), List.of(behandletSøknad(new FagSakEntitet(AktørId.dummy(), YTELSE, new Saksnummer("SAK123")))), fagsak(periode(TOM)));

            assertThat(resultat.type()).as(beskrivelse).isEqualTo(forventet);
        }

        @Test
        void tom_dato_selv_ligger_i_vinduet() {
            FagSakEntitet fagsak = fagsak(periode(TOM));
            var resultat = utled(TOM, List.of(behandletSøknad(fagsak)), fagsak);

            assertThat(resultat.type()).isEqualTo(TilgjengeligSøknadType.NY_PERIODE_SØKNAD);
        }
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

    private static Periode periode(LocalDate tom) {
        return new Periode(tom.minusWeeks(52).plusDays(1), tom);
    }

    private static FagSakEntitet fagsak(Periode... perioder) {
        var entitet = new FagSakEntitet(AktørId.dummy(), YTELSE, new Saksnummer("SAK123")
        );
        entitet.settPerioder(List.of(perioder));
        return entitet;
    }
}
