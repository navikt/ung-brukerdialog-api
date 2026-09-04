package no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst;

import com.fasterxml.jackson.core.type.TypeReference;
import no.nav.ung.brukerdialog.JsonObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifiserer JSON-kontrakten til {@link OppgaveTekst}-hierarkiet - dette er formen frontend på
 * sikt skal konsumere direkte fra {@code tekster}-feltet på {@code BrukerdialogOppgaveDto}, så et
 * brudd her er et kontraktsbrudd, ikke bare en intern datamodell-endring.
 */
class OppgaveTekstTest {

    @Test
    void avsnitt_rundtrip_bevarer_alle_felter() throws Exception {
        OppgaveTekst original = new OppgaveAvsnitt("En tittel", "Noe innhold", true);

        String json = JsonObjectMapper.getJson(original);
        OppgaveTekst deserialisert = JsonObjectMapper.fromJson(json, OppgaveTekst.class);

        assertThat(json).contains("\"type\" : \"AVSNITT\"");
        assertThat(deserialisert).isEqualTo(original);
    }

    @Test
    void avsnitt_uten_tittel_serialiseres_med_tittel_lik_null() throws Exception {
        OppgaveTekst original = new OppgaveAvsnitt("Bare innhold");

        String json = JsonObjectMapper.getJson(original);
        OppgaveTekst deserialisert = JsonObjectMapper.fromJson(json, OppgaveTekst.class);

        assertThat(deserialisert).isEqualTo(original);
        assertThat(((OppgaveAvsnitt) deserialisert).tittel()).isNull();
        assertThat(deserialisert.fet()).isFalse();
    }

    @Test
    void liste_rundtrip_bevarer_punkter() throws Exception {
        OppgaveTekst original = new OppgaveListe("Overskrift", List.of("Punkt 1", "Punkt 2"), false);

        String json = JsonObjectMapper.getJson(original);
        OppgaveTekst deserialisert = JsonObjectMapper.fromJson(json, OppgaveTekst.class);

        assertThat(json).contains("\"type\" : \"LISTE\"");
        assertThat(deserialisert).isEqualTo(original);
        assertThat(((OppgaveListe) deserialisert).punkter()).containsExactly("Punkt 1", "Punkt 2");
    }

    @Test
    void tabell_rundtrip_bevarer_kolonner_og_rader() throws Exception {
        OppgaveTekst original = new OppgaveTabell(
            List.of("Kilde", "Inntekt før skatt"),
            List.of(
                List.of("Arbeidsgiver AS", "25 000 kr"),
                List.of("Totalt", "25 000 kr")
            ));

        String json = JsonObjectMapper.getJson(original);
        OppgaveTekst deserialisert = JsonObjectMapper.fromJson(json, OppgaveTekst.class);

        assertThat(json).contains("\"type\" : \"TABELL\"");
        assertThat(deserialisert).isEqualTo(original);
    }

    /**
     * Realistisk brukstilfelle: en heterogen liste, slik den faktisk vil forekomme i
     * {@code tekster}-feltet på {@code BrukerdialogOppgaveDto}. Serialiserer via en eksplisitt
     * {@link TypeReference} (i stedet for {@link JsonObjectMapper#getJson}, som tar imot
     * {@code Object} og dermed sletter generisk typeinfo) - speiler hvordan Jackson faktisk
     * serialiserer et {@code List<OppgaveTekst>}-record-felt, der den statiske typen er kjent.
     */
    @Test
    void heterogen_liste_av_tekstblokker_rundtripper_i_riktig_rekkefølge() throws Exception {
        List<OppgaveTekst> original = List.of(
            new OppgaveAvsnitt("Første avsnitt - dette er varselteksten"),
            new OppgaveListe(List.of("Første punkt", "Andre punkt")),
            new OppgaveTabell(List.of("Kolonne"), List.of(List.of("Verdi"))),
            new OppgaveAvsnitt("Fristen for å svare er senest 1. januar 2025.", true)
        );

        String json = JsonObjectMapper.getMapper()
            .writerFor(new TypeReference<List<OppgaveTekst>>() {})
            .writeValueAsString(original);
        List<OppgaveTekst> deserialisert = JsonObjectMapper.getMapper()
            .readerFor(new TypeReference<List<OppgaveTekst>>() {})
            .readValue(json);

        assertThat(json).contains("\"type\":\"AVSNITT\"", "\"type\":\"LISTE\"", "\"type\":\"TABELL\"");
        assertThat(deserialisert).containsExactlyElementsOf(original);
    }
}
