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

class OppgaveTekstfragmentDataFlaggTest {

    private static final Handlebars HANDLEBARS = PdfGenerator.HANDLEBARS;

    @Test
    void avsnitt_med_data_true_havner_i_begge_lister() throws Exception {
        Resultat resultat = rendre("""
            {{#avsnitt data=true}}I varselInnhold{{/avsnitt}}
            {{#avsnitt}}Kun i alle{{/avsnitt}}
            """, Map.of());

        assertThat(resultat.alle).containsExactly(
            new OppgaveAvsnitt("I varselInnhold"),
            new OppgaveAvsnitt("Kun i alle"));
        assertThat(resultat.varselInnhold).containsExactly(new OppgaveAvsnitt("I varselInnhold"));
    }

    @Test
    void tabell_med_data_true_havner_i_begge_lister() throws Exception {
        Resultat resultat = rendre("""
            {{#tabell overskrift1="A" overskrift2="B" data=true}}{{rad "x" "y"}}{{/tabell}}
            """, Map.of());

        assertThat(resultat.alle).hasSize(1);
        assertThat(resultat.varselInnhold).isEqualTo(resultat.alle);
    }

    @Test
    void liste_uten_data_havner_kun_i_alle() throws Exception {
        Resultat resultat = rendre("""
            {{#liste}}{{punkt "Punkt A"}}{{/liste}}
            """, Map.of());

        assertThat(resultat.alle).hasSize(1);
        assertThat(resultat.varselInnhold).isEmpty();
    }

    @Test
    void fet_inni_data_avsnitt_påvirkes_ikke_av_flagget_men_følger_med_som_del_av_innhold() throws Exception {
        Resultat resultat = rendre("""
            {{#avsnitt data=true}}Fristen er {{#fet}}1. januar 2025{{/fet}}.{{/avsnitt}}
            """, Map.of());

        OppgaveTekst forventet = new OppgaveAvsnitt("Fristen er <b>1. januar 2025</b>.");
        assertThat(resultat.alle).containsExactly(forventet);
        assertThat(resultat.varselInnhold).containsExactly(forventet);
    }

    private static Resultat rendre(String malTekst, Map<String, Object> modell) throws Exception {
        Template template = HANDLEBARS.compileInline(malTekst);
        List<OppgaveTekst> alle = new ArrayList<>();
        List<OppgaveTekst> varselInnhold = new ArrayList<>();
        Context context = Context.newBuilder(modell)
            .resolver(MapValueResolver.INSTANCE, MethodValueResolver.INSTANCE)
            .build();
        context.data(OppgaveTekstfragmentRenderer.AKKUMULATOR_NØKKEL, alle);
        context.data(OppgaveTekstfragmentRenderer.VARSEL_INNHOLD_NØKKEL, varselInnhold);
        template.apply(context);
        return new Resultat(alle, varselInnhold);
    }

    private record Resultat(List<OppgaveTekst> alle, List<OppgaveTekst> varselInnhold) {
    }
}
