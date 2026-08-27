package no.nav.ung.brukerdialog.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.slf4j.Slf4jLogger;
import com.openhtmltopdf.util.XRLog;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Genererer PDF-er fra Handlebars-maler, i samme stil som {@code k9-brukerdialog-prosessering}s
 * {@code PDFGenerator}. Generisk rendrings-infrastruktur - kjenner ikke innholdet i malene eller
 * datamodellen (se {@link PdfDokument}).
 */
@ApplicationScoped
public class PdfGenerator {

    private static final String KLASSEPATH_ROT = "handlebars";
    private static final String FONTNAVN = "Source Sans Pro";
    private static final DateTimeFormatter DATO_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIDSPUNKT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final byte[] REGULAR_FONT = lesFontFil("Regular");
    private static final byte[] BOLD_FONT = lesFontFil("Bold");
    private static final byte[] ITALIC_FONT = lesFontFil("Italic");

    private static final Handlebars HANDLEBARS = konfigurerHandlebars();

    static {
        // Ruter openhtmltopdfs interne logging til slf4j, som resten av applikasjonen bruker.
        XRLog.setLoggerImpl(new Slf4jLogger());
    }

    public byte[] genererPdf(PdfDokument dokument) {
        Objects.requireNonNull(dokument, "dokument");
        String html = tilHtml(dokument);
        return tilPdf(html);
    }

    /**
     * Public for testing.
     */
    public String tilHtml(PdfDokument dokument) {
        Objects.requireNonNull(dokument, "dokument");
        Template template = kompilerMal(dokument.malnavn());
        Context context = Context.newBuilder(dokument.data())
            .resolver(MapValueResolver.INSTANCE)
            .build();
        try {
            return template.apply(context);
        } catch (IOException e) {
            throw new UncheckedIOException(
                "Klarte ikke å rendre Handlebars-mal '%s'".formatted(dokument.malnavn()), e);
        }
    }

    private static Template kompilerMal(String malnavn) {
        try {
            return HANDLEBARS.compile(malnavn);
        } catch (IOException e) {
            throw new UncheckedIOException("Fant ikke Handlebars-mal '%s'".formatted(malnavn), e);
        }
    }

    private byte[] tilPdf(String html) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            new PdfRendererBuilder()
                .useFastMode()
                .usePdfUaAccessibility(true)
                .withHtmlContent(html, "")
                .useFont(() -> new ByteArrayInputStream(REGULAR_FONT), FONTNAVN, 400, BaseRendererBuilder.FontStyle.NORMAL, false)
                .useFont(() -> new ByteArrayInputStream(BOLD_FONT), FONTNAVN, 700, BaseRendererBuilder.FontStyle.NORMAL, false)
                .useFont(() -> new ByteArrayInputStream(ITALIC_FONT), FONTNAVN, 400, BaseRendererBuilder.FontStyle.ITALIC, false)
                .toStream(output)
                .buildPdfRenderer()
                .createPDF();
            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Klarte ikke å generere PDF", e);
        }
    }

    private static Handlebars konfigurerHandlebars() {
        Handlebars handlebars = new Handlebars(new ClassPathTemplateLoader("/" + KLASSEPATH_ROT));
        registrerFritekstHelper(handlebars);
        registrerDatoHelper(handlebars);
        registrerTidspunktHelper(handlebars);
        registrerDatoLangHelper(handlebars);
        registrerMånedHelper(handlebars);
        registrerMånedÅrHelper(handlebars);
        registrerEqHelper(handlebars);
        registrerIsNotNullHelper(handlebars);
        registrerKronerHelper(handlebars);
        handlebars.infiniteLoops(true);
        return handlebars;
    }

    private static void registrerFritekstHelper(Handlebars handlebars) {
        handlebars.registerHelper("fritekst", (Helper<String>) (context, options) -> {
            if (context == null) {
                return "";
            }
            String escaped = Handlebars.Utils.escapeExpression(context).toString()
                .replaceAll("\r\n|[\n\r]", "<br/>");
            return new Handlebars.SafeString(escaped);
        });
    }

    private static void registrerDatoHelper(Handlebars handlebars) {
        handlebars.registerHelper("dato", (Helper<String>) (context, options) ->
            context == null ? "" : DATO_FORMAT.format(LocalDate.parse(context)));
    }

    private static void registrerTidspunktHelper(Handlebars handlebars) {
        handlebars.registerHelper("tidspunkt", (Helper<String>) (context, options) ->
            context == null ? "" : TIDSPUNKT_FORMAT.format(ZonedDateTime.parse(context)));
    }

    private static void registrerDatoLangHelper(Handlebars handlebars) {
        handlebars.registerHelper("datoLang", (Helper<String>) (context, options) ->
            context == null ? "" : NorskDatoFormat.datoLang(LocalDate.parse(context)));
    }

    private static void registrerMånedHelper(Handlebars handlebars) {
        handlebars.registerHelper("måned", (Helper<String>) (context, options) ->
            context == null ? "" : NorskDatoFormat.måned(LocalDate.parse(context)));
    }

    private static void registrerMånedÅrHelper(Handlebars handlebars) {
        handlebars.registerHelper("månedÅr", (Helper<String>) (context, options) ->
            context == null ? "" : NorskDatoFormat.månedÅr(LocalDate.parse(context)));
    }

    private static void registrerEqHelper(Handlebars handlebars) {
        handlebars.registerHelper("eq", (Helper<Object>) (context, options) ->
            Objects.equals(context, options.param(0)) ? options.fn() : options.inverse());
    }

    private static void registrerIsNotNullHelper(Handlebars handlebars) {
        handlebars.registerHelper("isNotNull", (Helper<Object>) (context, options) ->
            context != null ? options.fn() : options.inverse());
    }

    /** F.eks. «12 345 kr» - space som tusenskilletegn, ingen desimaler (beløp er alltid hele kroner). */
    private static void registrerKronerHelper(Handlebars handlebars) {
        handlebars.registerHelper("kroner", (Helper<Object>) (context, options) ->
            context == null ? "" : formaterKroner(((Number) context).longValue()));
    }

    private static String formaterKroner(long beløp) {
        String siffer = Long.toString(Math.abs(beløp));
        StringBuilder gruppert = new StringBuilder();
        int count = 0;
        for (int i = siffer.length() - 1; i >= 0; i--) {
            gruppert.append(siffer.charAt(i));
            count++;
            if (count % 3 == 0 && i != 0) {
                gruppert.append(' ');
            }
        }
        return (beløp < 0 ? "-" : "") + gruppert.reverse() + " kr";
    }

    private static byte[] lesFontFil(String variant) {
        String path = "/%s/fonts/SourceSansPro-%s.ttf".formatted(KLASSEPATH_ROT, variant);
        try (InputStream input = PdfGenerator.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("Fant ikke fontfil på klassepath: " + path);
            }
            return input.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Klarte ikke å lese fontfil " + path, e);
        }
    }
}
