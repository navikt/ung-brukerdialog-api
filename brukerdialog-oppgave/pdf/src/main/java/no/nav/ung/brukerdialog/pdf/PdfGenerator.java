package no.nav.ung.brukerdialog.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgavePunktliste;
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

    /** Data-rammenøkler brukt av {@link #registrerTekstfragmentHjelpere} - se javadocen der. */
    private static final String LISTE_PUNKTER_NØKKEL = "__listePunkter__";
    private static final String TABELL_RADER_NØKKEL = "__tabellRader__";

    private static final byte[] REGULAR_FONT = lesFontFil("Regular");
    private static final byte[] BOLD_FONT = lesFontFil("Bold");
    private static final byte[] ITALIC_FONT = lesFontFil("Italic");

    static final Handlebars HANDLEBARS = konfigurerHandlebars();

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
        registrerSwitchCaseHelpere(handlebars);
        registrerIsNotNullHelper(handlebars);
        registrerKronerHelper(handlebars);
        registrerBlokktypeHelpere(handlebars);
        registrerLenkifyHelper(handlebars);
        registrerTekstfragmentHjelpere(handlebars);
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

    /**
     * {@code switch}/{@code case}/{@code default}-helperpar - erstatter nøstede
     * {@code {{#eq x "a"}}...{{else}}{{#eq x "b"}}...{{/eq}}...{{/eq}}}-kjeder med en flat,
     * lesbar switch for 3+ veis forgreining. {@code options.data(...)} leser/skriver på
     * datarammen til gjeldende render-{@code Context} - ikke et delt/statisk felt på selve
     * {@code Handlebars}-instansen - så et {@code switch}-kall i ett render kan aldri lekke
     * tilstand til et annet, selv om de deler samme kompilerte {@code Template}.
     */
    private static void registrerSwitchCaseHelpere(Handlebars handlebars) {
        handlebars.registerHelper("switch", (Helper<Object>) (context, options) -> {
            options.data("switch_verdi", context);
            options.data("switch_traff", false);
            return options.fn();
        });
        handlebars.registerHelper("case", (Helper<Object>) (context, options) -> {
            boolean alleredeTruffet = Boolean.TRUE.equals(options.data("switch_traff"));
            if (!alleredeTruffet && Objects.equals(options.data("switch_verdi"), context)) {
                options.data("switch_traff", true);
                return options.fn();
            }
            return null;
        });
        handlebars.registerHelper("default", (Helper<Object>) (context, options) ->
            Boolean.TRUE.equals(options.data("switch_traff")) ? null : options.fn());
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
            tekst instanceof OppgavePunktliste ? options.fn() : options.inverse());
        handlebars.registerHelper("isTabell", (Helper<OppgaveTekst>) (tekst, options) ->
            tekst instanceof OppgaveTabell ? options.fn() : options.inverse());
    }

    /**
     * Erstatter den faste frasen «Min side på nav.no» i en (allerede fritt formulert)
     * tekstblokk med en faktisk lenke til Min side.
     */
    private static void registrerLenkifyHelper(Handlebars handlebars) {
        handlebars.registerHelper("lenkify", (Helper<String>) (context, options) -> {
            if (context == null) {
                return "";
            }
            String lenket = context.replace(MIN_SIDE_FRASE,
                "<a href=\"%s\" title=\"%s\">%s</a>".formatted(MIN_SIDE_URL, MIN_SIDE_FRASE, MIN_SIDE_FRASE));
            return new Handlebars.SafeString(lenket);
        });
    }

    /**
     * Hjelpere som lar en Handlebars-mal bygge en {@code List<OppgaveTekst>} DIREKTE mens malen
     * rendres, i stedet for å rendre til tekst med en egen markørkonvensjon som så må parses
     * tilbake.
     */
    private static void registrerTekstfragmentHjelpere(Handlebars handlebars) {
        handlebars.registerHelper("avsnitt", (Helper<Object>) (context, options) -> {
            String tittel = options.hash("tittel");
            String innhold = tekstFraBlokk(options.fn());
            leggTilElement(options, new OppgaveAvsnitt(tittel, innhold));
            return null;
        });
        handlebars.registerHelper("fet", (Helper<Object>) (context, options) ->
            new Handlebars.SafeString("<b>" + tekstFraBlokk(options.fn()) + "</b>"));
        handlebars.registerHelper("liste", (Helper<Object>) (context, options) -> {
            String tittel = options.hash("tittel");
            List<String> punkter = new ArrayList<>();
            options.data(LISTE_PUNKTER_NØKKEL, punkter);
            options.fn();
            leggTilElement(options, new OppgavePunktliste(tittel, punkter, false));
            return null;
        });
        handlebars.registerHelper("punkt", (Helper<Object>) (context, options) -> {
            punkter(options).add(tekstFraVerdi(context));
            return null;
        });
        handlebars.registerHelper("tabell", (Helper<Object>) (context, options) -> {
            String tittel = options.hash("tittel");
            String overskrift1 = options.hash("overskrift1");
            String overskrift2 = options.hash("overskrift2");
            List<List<String>> rader = new ArrayList<>();
            options.data(TABELL_RADER_NØKKEL, rader);
            options.fn();
            leggTilElement(options, new OppgaveTabell(tittel, List.of(overskrift1, overskrift2), rader, false));
            return null;
        });
        handlebars.registerHelper("rad", (Helper<Object>) (context, options) -> {
            List<String> celler = new ArrayList<>();
            celler.add(tekstFraVerdi(context));
            for (Object param : options.params) {
                celler.add(tekstFraVerdi(param));
            }
            rader(options).add(celler);
            return null;
        });
    }

    private static void leggTilElement(Options options, OppgaveTekst element) {
        akkumulator(options).add(element);
        if (Boolean.TRUE.equals(options.hash("data", false))) {
            varselInnholdAkkumulator(options).add(element);
        }
    }

    private static String tekstFraBlokk(CharSequence blokkInnhold) {
        String tekst = blokkInnhold == null ? "" : blokkInnhold.toString();
        return tekst.strip().replaceAll("\\s*\\n\\s*", " ");
    }

    private static String tekstFraVerdi(Object verdi) {
        return verdi == null ? "" : verdi.toString();
    }

    @SuppressWarnings("unchecked")
    private static List<OppgaveTekst> akkumulator(Options options) {
        return (List<OppgaveTekst>) options.data(OppgaveTekstfragmentRenderer.AKKUMULATOR_NØKKEL);
    }

    @SuppressWarnings("unchecked")
    private static List<OppgaveTekst> varselInnholdAkkumulator(Options options) {
        return (List<OppgaveTekst>) options.data(OppgaveTekstfragmentRenderer.VARSEL_INNHOLD_NØKKEL);
    }

    @SuppressWarnings("unchecked")
    private static List<String> punkter(Options options) {
        return (List<String>) options.data(LISTE_PUNKTER_NØKKEL);
    }

    @SuppressWarnings("unchecked")
    private static List<List<String>> rader(Options options) {
        return (List<List<String>>) options.data(TABELL_RADER_NØKKEL);
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
