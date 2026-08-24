package no.nav.ung.brukerdialog.journalforing.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.UncheckedIOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Verifiserer selve rendrings-infrastrukturen (Handlebars + openhtmltopdf + fonter). Bruker en
 * dedikert test-only mal ({@code test-mal.hbs}, kun i {@code src/test/resources}) i stedet for en
 * av produksjonsmalene under {@code typer/}: infrastrukturtester skal ikke koble
 * seg til det kuraterte brevinnholdet, som kan endre seg uavhengig av rendrings-pipelinen.
 */
class PdfGeneratorTest {

    private final PdfGenerator pdfGenerator = new PdfGenerator();

    @Test
    void skal_generere_gyldig_pdf_fra_testmalen() {
        var dokument = new PdfDokument("test-mal", Map.of(
            "tittel", "Test tittel",
            "oppgave", Map.of("oppgaveReferanse", "11111111-1111-1111-1111-111111111111")
        ));

        byte[] pdf = pdfGenerator.genererPdf(dokument);

        assertThat(pdf).isNotEmpty();
        // PDF-magic number.
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void skal_feile_med_malnavn_i_meldingen_men_uten_datainnhold_ved_ukjent_mal() {
        var dokument = new PdfDokument("finnes-ikke", Map.of("hemmelig", "fnr-eller-annen-pii"));

        assertThatThrownBy(() -> pdfGenerator.genererPdf(dokument))
            .isInstanceOf(UncheckedIOException.class)
            .hasMessageContaining("finnes-ikke")
            .hasMessageNotContaining("hemmelig")
            .hasMessageNotContaining("fnr-eller-annen-pii");
    }

    @Test
    void skal_feile_ved_null_dokument() {
        assertThatThrownBy(() -> pdfGenerator.genererPdf(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("dokument");
    }

    /**
     * Rendrer testmalens {@code datoLang}/{@code måned}/{@code månedÅr}/{@code kroner}-uttrykk
     * og leser HTML-mellomsteget direkte via {@link PdfGenerator#tilHtml},
     * for å verifisere at hjelperne faktisk er koblet til {@code Handlebars}-instansen
     * {@link PdfGenerator} bruker. Ren enhetstesting av {@link NorskDatoFormat} (se
     * {@code NorskDatoFormatTest}) dekker ikke selve registreringen/parsingen av ISO-datostrenger
     * i helper-laget. Går bevisst ikke via den rasteriserte PDF-en - dagens PDF/UA-oppsett har
     * ingen {@code ToUnicode}-CMap, så tekst lar seg ikke pålitelig lese ut igjen fra PDF-byte.
     */
    @Test
    void skal_rendre_dato_og_kroner_hjelperne_riktig() {
        var dokument = new PdfDokument("test-mal", Map.of(
            "tittel", "Test tittel",
            "oppgave", Map.of(
                "oppgaveReferanse", "11111111-1111-1111-1111-111111111111",
                "datoLangInput", "2025-01-01",
                "månedInput", "2025-05-01",
                "månedÅrInput", "2021-09-30",
                "kronerInput", 12345)
        ));

        String html = pdfGenerator.tilHtml(dokument);

        assertThat(html).contains("1. januar 2025");
        assertThat(html).contains("<p id=\"måned\">mai</p>");
        assertThat(html).contains("september 2021");
        assertThat(html).contains("12 345 kr");
    }
}
