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
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveListe;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTabell;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfBoxRenderer;
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

    /** URL og lenketekst for «Min side»-lenken - se {@link #registrerLenkifyHelper}. */
    private static final String MIN_SIDE_FRASE = "Min side på nav.no";
    private static final String MIN_SIDE_URL = "https://www.nav.no/minside";

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
            // MethodValueResolver trengs for å lese OppgaveTekst-recordenes accessor-metoder
            // (tittel/innhold/fet/punkter/kolonneOverskrifter/rader) - MapValueResolver alene
            // løser kun Map-nøkler, ikke metodekall på POJO-er/records nøstet i dataen.
            .resolver(MapValueResolver.INSTANCE, MethodValueResolver.INSTANCE)
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
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             PdfBoxRenderer renderer = new PdfRendererBuilder()
                 .usePdfUaAccessibility(true)
                 .withHtmlContent(html, "")
                 .useFont(() -> new ByteArrayInputStream(REGULAR_FONT), FONTNAVN, 400, BaseRendererBuilder.FontStyle.NORMAL, false)
                 .useFont(() -> new ByteArrayInputStream(BOLD_FONT), FONTNAVN, 700, BaseRendererBuilder.FontStyle.NORMAL, false)
                 .useFont(() -> new ByteArrayInputStream(ITALIC_FONT), FONTNAVN, 400, BaseRendererBuilder.FontStyle.ITALIC, false)
                 .toStream(output)
                 .buildPdfRenderer()) {
            renderer.createPDF();
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
        registrerBlokktypeHelpere(handlebars);
        registrerLenkifyHelper(handlebars);
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

    private static void registrerKronerHelper(Handlebars handlebars) {
        handlebars.registerHelper("kroner", (Helper<Object>) (context, options) ->
            context == null ? "" : NorskBeløpFormat.kroner(((Number) context).longValue()));
    }

    /**
     * Blokktype-forgrening for {@code oppgave.hbs} - jknack Handlebars har ingen innebygd
     * {@code instanceof}, så disse tre erstatter det for {@link OppgaveTekst}s forseglede
     * undertyper. Alternativet (en egen strengbasert diskriminator-metode på grensesnittet) ville
     * duplisert det {@code @JsonTypeInfo} allerede uttrykker for JSON - unngås bevisst.
     */
    private static void registrerBlokktypeHelpere(Handlebars handlebars) {
        handlebars.registerHelper("isAvsnitt", (Helper<OppgaveTekst>) (tekst, options) ->
            tekst instanceof OppgaveAvsnitt ? options.fn() : options.inverse());
        handlebars.registerHelper("isListe", (Helper<OppgaveTekst>) (tekst, options) ->
            tekst instanceof OppgaveListe ? options.fn() : options.inverse());
        handlebars.registerHelper("isTabell", (Helper<OppgaveTekst>) (tekst, options) ->
            tekst instanceof OppgaveTabell ? options.fn() : options.inverse());
    }

    /**
     * Erstatter den faste frasen «Min side på nav.no» i en (allerede fritt formulert)
     * tekstblokk med en faktisk lenke til Min side. Selve frasen er alltid en hardkodet konstant
     * i {@code OppgaveTekster}/{@code *OppgaveInnholdUtleder} - aldri brukerinnhold - så det er
     * trygt å sette inn rå HTML for akkurat denne frasen etter at resten av strengen er escapet.
     */
    private static void registrerLenkifyHelper(Handlebars handlebars) {
        handlebars.registerHelper("lenkify", (Helper<String>) (context, options) -> {
            if (context == null) {
                return "";
            }
            String escaped = Handlebars.Utils.escapeExpression(context).toString();
            String lenket = escaped.replace(MIN_SIDE_FRASE,
                "<a href=\"%s\" title=\"%s\">%s</a>".formatted(MIN_SIDE_URL, MIN_SIDE_FRASE, MIN_SIDE_FRASE));
            return new Handlebars.SafeString(lenket);
        });
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
