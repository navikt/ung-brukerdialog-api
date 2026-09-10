package no.nav.ung.brukerdialog.pdf;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Rendrer en Handlebars-mal fra {@code handlebars/tekstfragmenter/} og henter ut resultatet som
 * en {@code List<OppgaveTekst>}. Til forskjell fra {@link PdfGenerator} (som rendrer et helt
 * HTML-dokument) rendrer denne kun enkeltstående tekstfragmenter - malene inneholder selve
 * forgreiningslogikken for hvilken ferdigskrevet norsk tekst som skal brukes for en gitt
 * oppgavetype.
 * Gjenbruker {@link PdfGenerator}s allerede konfigurerte {@code Handlebars}-instans (samme
 * helpers og samme klassepath-rot).
 */
@ApplicationScoped
public class OppgaveTekstfragmentRenderer {

    static final String AKKUMULATOR_NØKKEL = "__oppgaveTekster__";
    static final String VARSEL_INNHOLD_NØKKEL = "__varselInnhold__";

    public record Resultat(List<OppgaveTekst> alle, List<OppgaveTekst> varselInnhold) {
    }

    public Resultat rendre(String malnavn, Map<String, Object> data) {
        Objects.requireNonNull(malnavn, "malnavn");
        Objects.requireNonNull(data, "data");
        Template template = kompilerMal(malnavn);
        List<OppgaveTekst> akkumulator = new ArrayList<>();
        List<OppgaveTekst> varselInnhold = new ArrayList<>();
        Context context = Context.newBuilder(data)
            .resolver(MapValueResolver.INSTANCE, MethodValueResolver.INSTANCE)
            .build();
        context.data(AKKUMULATOR_NØKKEL, akkumulator);
        context.data(VARSEL_INNHOLD_NØKKEL, varselInnhold);
        try {
            template.apply(context);
        } catch (IOException e) {
            throw new UncheckedIOException(
                "Klarte ikke å rendre tekstfragment-mal '%s'".formatted(malnavn), e);
        }
        return new Resultat(akkumulator, varselInnhold);
    }

    private static Template kompilerMal(String malnavn) {
        try {
            return PdfGenerator.HANDLEBARS.compile(malnavn);
        } catch (IOException e) {
            throw new UncheckedIOException("Fant ikke Handlebars-mal '%s'".formatted(malnavn), e);
        }
    }
}

