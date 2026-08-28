package no.nav.ung.brukerdialog.pdf;

import java.util.Map;
import java.util.Objects;

/**
 * Handlebars-malnavn (uten filendelse) og datamodellen for {@link PdfGenerator}. {@code data}
 * kan inneholde navn/fødselsnummer - skal aldri logges eller inngå i en exception-melding.
 */
public record PdfDokument(String malnavn, Map<String, Object> data) {
    public PdfDokument {
        Objects.requireNonNull(malnavn, "malnavn");
        Objects.requireNonNull(data, "data");
    }
}
