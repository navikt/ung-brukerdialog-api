package no.nav.ung.brukerdialog.journalforing.pdf;

import java.util.Map;
import java.util.Objects;

/**
 * Alt {@link PdfGenerator} trenger for å rendre ett dokument: navnet på Handlebars-malen
 * (uten filendelse, se {@code src/main/resources/handlebars}) og datamodellen malen bindes mot.
 *
 * <p>{@code data} kan inneholde navn og fødselsnummer - denne typen skal derfor
 * aldri logges eller inngå i en exception-melding.
 */
public record PdfDokument(String malnavn, Map<String, Object> data) {
    public PdfDokument {
        Objects.requireNonNull(malnavn, "malnavn");
        Objects.requireNonNull(data, "data");
    }
}
