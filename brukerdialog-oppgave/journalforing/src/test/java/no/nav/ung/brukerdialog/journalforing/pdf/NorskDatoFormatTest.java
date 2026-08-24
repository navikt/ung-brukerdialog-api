package no.nav.ung.brukerdialog.journalforing.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Verifiserer brevtekst-datoformatene, inkludert at {@link java.util.Locale#of}
 * med språkkode {@code "no"} faktisk gir små forbokstaver på månedsnavn i denne JVM-en - se
 * klassejavadoc på {@link NorskDatoFormat}, som påpeker at dette skal bekreftes med en test og
 * ikke antas.
 */
class NorskDatoFormatTest {

    @Test
    void datoLang_gir_dag_punktum_måned_år() {
        assertThat(NorskDatoFormat.datoLang(LocalDate.of(2021, 1, 1))).isEqualTo("1. januar 2021");
        assertThat(NorskDatoFormat.datoLang(LocalDate.of(2025, 12, 24))).isEqualTo("24. desember 2025");
    }

    @Test
    void måned_gir_kun_månedsnavn_med_liten_forbokstav() {
        assertThat(NorskDatoFormat.måned(LocalDate.of(2025, 1, 15))).isEqualTo("januar");
        assertThat(NorskDatoFormat.måned(LocalDate.of(2025, 5, 1))).isEqualTo("mai");
        assertThat(NorskDatoFormat.måned(LocalDate.of(2025, 12, 31))).isEqualTo("desember");
    }

    @Test
    void månedÅr_gir_måned_og_år_med_liten_forbokstav() {
        assertThat(NorskDatoFormat.månedÅr(LocalDate.of(2025, 1, 1))).isEqualTo("januar 2025");
        assertThat(NorskDatoFormat.månedÅr(LocalDate.of(2021, 9, 30))).isEqualTo("september 2021");
    }
}
