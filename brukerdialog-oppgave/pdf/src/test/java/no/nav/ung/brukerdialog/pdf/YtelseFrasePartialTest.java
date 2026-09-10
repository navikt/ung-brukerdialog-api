package no.nav.ung.brukerdialog.pdf;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class YtelseFrasePartialTest {

    private static final Handlebars HANDLEBARS = PdfGenerator.HANDLEBARS;

    @Test
    void ytelse_preposisjonsfrase_settes_inn_midt_i_setningen_uten_uønsket_whitespace_ungdomsytelse() throws Exception {
        List<OppgaveTekst> resultat = rendre("""
            {{#avsnitt}}Veilederen din har endret startdatoen din {{> partial/ytelse_preposisjonsfrase}} til 1. januar 2025.{{/avsnitt}}
            """, Map.of("ytelsetype", "UNGDOMSYTELSE"));

        assertThat(resultat).containsExactly(
            new OppgaveAvsnitt("Veilederen din har endret startdatoen din i ungdomsprogrammet til 1. januar 2025."));
    }

    @Test
    void ytelse_preposisjonsfrase_settes_inn_midt_i_setningen_uten_uønsket_whitespace_aktivitetspenger() throws Exception {
        List<OppgaveTekst> resultat = rendre("""
            {{#avsnitt}}Veilederen din har endret startdatoen din {{> partial/ytelse_preposisjonsfrase}} til 1. januar 2025.{{/avsnitt}}
            """, Map.of("ytelsetype", "AKTIVITETSPENGER"));

        assertThat(resultat).containsExactly(
            new OppgaveAvsnitt("Veilederen din har endret startdatoen din for aktivitetspenger til 1. januar 2025."));
    }

    @Test
    void ytelse_navn_settes_inn_midt_i_setningen_uten_uønsket_whitespace_ungdomsytelse() throws Exception {
        List<OppgaveTekst> resultat = rendre("""
            {{#avsnitt}}Du vil nå få {{> partial/ytelse_navn}} i perioden 1. januar 2025 til 1. februar 2025.{{/avsnitt}}
            """, Map.of("ytelsetype", "UNGDOMSYTELSE"));

        assertThat(resultat).containsExactly(
            new OppgaveAvsnitt("Du vil nå få ungdomsprogramytelsen i perioden 1. januar 2025 til 1. februar 2025."));
    }

    @Test
    void ytelse_navn_settes_inn_midt_i_setningen_uten_uønsket_whitespace_aktivitetspenger() throws Exception {
        List<OppgaveTekst> resultat = rendre("""
            {{#avsnitt}}Du vil nå få {{> partial/ytelse_navn}} i perioden 1. januar 2025 til 1. februar 2025.{{/avsnitt}}
            """, Map.of("ytelsetype", "AKTIVITETSPENGER"));

        assertThat(resultat).containsExactly(
            new OppgaveAvsnitt("Du vil nå få aktivitetspenger i perioden 1. januar 2025 til 1. februar 2025."));
    }

    @Test
    void ytelse_navn_kan_brukes_to_ganger_i_samme_setning_uten_uønsket_whitespace() throws Exception {
        List<OppgaveTekst> resultat = rendre("""
            {{#avsnitt}}Din siste dag med {{> partial/ytelse_navn}} er 1. januar 2025. Det er fordi du har brukt opp dagene du kan motta {{> partial/ytelse_navn}}.{{/avsnitt}}
            """, Map.of("ytelsetype", "AKTIVITETSPENGER"));

        assertThat(resultat).containsExactly(new OppgaveAvsnitt(
            "Din siste dag med aktivitetspenger er 1. januar 2025. Det er fordi du har brukt opp dagene du kan motta aktivitetspenger."));
    }

    private static List<OppgaveTekst> rendre(String malTekst, Map<String, Object> modell) throws Exception {
        Template template = HANDLEBARS.compileInline(malTekst);
        List<OppgaveTekst> alle = new ArrayList<>();
        List<OppgaveTekst> varselInnhold = new ArrayList<>();
        Context context = Context.newBuilder(modell)
            .resolver(MapValueResolver.INSTANCE, MethodValueResolver.INSTANCE)
            .build();
        context.data(OppgaveTekstfragmentRenderer.AKKUMULATOR_NØKKEL, alle);
        context.data(OppgaveTekstfragmentRenderer.VARSEL_INNHOLD_NØKKEL, varselInnhold);
        template.apply(context);
        return alle;
    }
}
