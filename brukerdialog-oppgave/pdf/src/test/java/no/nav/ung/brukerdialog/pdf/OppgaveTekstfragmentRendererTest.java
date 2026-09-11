package no.nav.ung.brukerdialog.pdf;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class OppgaveTekstfragmentRendererTest {

    private static final String MALNAVN = "tekstfragmenter/bosted/bekreft_bosted";
    private static final LocalDate FOM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2025, 1, 31);

    private static final OppgaveAvsnitt OM_VARSEL_1 = new OppgaveAvsnitt(
        "Om «Varsel om nye opplysninger»",
        "Dette varselet sendes ut slik at du får mulighet til å komme med en tilbakemelding på opplysningene før Nav fatter vedtak. Tilbakemeldingen sendes inn via Min side på nav.no.");
    private static final OppgaveAvsnitt OM_VARSEL_2 = new OppgaveAvsnitt(
        "Hvis vi ikke hører noe fra deg, bruker Nav opplysningene over når vedtaket fattes.");

    private final OppgaveTekstfragmentRenderer renderer = new OppgaveTekstfragmentRenderer();

    static Stream<Arguments> årsakOgTidsromKombinasjoner() {
        return Stream.of(
            Arguments.of("IKKE_BOSATTADRESSE_I_TRONDHEIM", false,
                "Vi har fått opplysninger om at du fra 1. januar 2025 ikke lenger bor i Trondheim kommune. Du må ha bostedsadresse i Trondheim kommune for å få aktivitetspenger."),
            Arguments.of("IKKE_BOSATTADRESSE_I_TRONDHEIM", true,
                "Vi har fått opplysninger om at du i perioden 1. januar 2025 til 31. januar 2025 ikke bor i Trondheim kommune. Du må ha bostedsadresse i Trondheim kommune for å få aktivitetspenger."),
            Arguments.of("IKKE_BOSTEDSADRESSE_OG_IKKE_FOLKEREGISTRERT_I_TRONDHEIM", false,
                "Vi har fått opplysninger om at du fra 1. januar 2025 ikke lenger bor i Trondheim kommune, og at du heller ikke er folkeregistrert der. Du må ha bostedsadresse i Trondheim kommune for å få aktivitetspenger."),
            Arguments.of("IKKE_BOSTEDSADRESSE_OG_IKKE_FOLKEREGISTRERT_I_TRONDHEIM", true,
                "Vi har fått opplysninger om at du i perioden 1. januar 2025 til 31. januar 2025 ikke bor i Trondheim kommune, og at du heller ikke er folkeregistrert der. Du må ha bostedsadresse i Trondheim kommune for å få aktivitetspenger."),
            Arguments.of("STUDIE_ELLER_ARBEIDSSTED_UTENFOR_TRONDHEIM", false,
                "Vi har fått opplysninger om at du fra 1. januar 2025 har studie- eller arbeidssted utenfor Trondheim kommune. Du må bo i Trondheim kommune for å få aktivitetspenger."),
            Arguments.of("STUDIE_ELLER_ARBEIDSSTED_UTENFOR_TRONDHEIM", true,
                "Vi har fått opplysninger om at du i perioden 1. januar 2025 til 31. januar 2025 har studie- eller arbeidssted utenfor Trondheim kommune. Du må bo i Trondheim kommune for å få aktivitetspenger."),
            Arguments.of("ANNET", false,
                "Vi har fått opplysninger om at du fra 1. januar 2025 ikke lenger bor i Trondheim kommune. Du må bo i Trondheim kommune for å få aktivitetspenger."),
            Arguments.of("ANNET", true,
                "Vi har fått opplysninger om at du i perioden 1. januar 2025 til 31. januar 2025 ikke bor i Trondheim kommune. Du må bo i Trondheim kommune for å få aktivitetspenger."),
            Arguments.of("UDEFINERT", false,
                "Vi har fått opplysninger om at du fra 1. januar 2025 ikke lenger bor i Trondheim kommune. Du må bo i Trondheim kommune for å få aktivitetspenger."),
            Arguments.of("UDEFINERT", true,
                "Vi har fått opplysninger om at du i perioden 1. januar 2025 til 31. januar 2025 ikke bor i Trondheim kommune. Du må bo i Trondheim kommune for å få aktivitetspenger.")
        );
    }

    @ParameterizedTest
    @MethodSource("årsakOgTidsromKombinasjoner")
    void hovedsetning_er_lik_for_alle_årsak_og_tidsrom_kombinasjoner(String årsak, boolean erPeriode, String forventet) {
        Map<String, Object> data = grunndata(årsak, erPeriode);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        assertThat(tekster.get(0)).isEqualTo(new OppgaveAvsnitt(forventet));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"BRUKER", "FOLKEREGISTER", "ANNET"})
    void kildeblokk_dekker_alle_kildetyper(String kilde) {
        Map<String, Object> data = grunndata("IKKE_BOSATTADRESSE_I_TRONDHEIM", false);
        data.put("kilde", kilde);
        data.put("kildeFritekst", "Skatteetaten");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        String forventetLabel = switch (kilde) {
            case "BRUKER" -> "Deg";
            case "FOLKEREGISTER" -> "Folkeregisteret";
            default -> "Skatteetaten";
        };
        OppgaveTekst kildeAvsnitt = tekster.stream()
            .filter(t -> "Hvor har vi fått opplysningene fra?".equals(t.tittel()))
            .findFirst().orElseThrow();
        assertThat(kildeAvsnitt).isEqualTo(new OppgaveAvsnitt("Hvor har vi fått opplysningene fra?", forventetLabel));
    }

    @Test
    void annetFritekst_gir_eget_avsnitt_kun_for_årsak_annet() {
        Map<String, Object> data = grunndata("ANNET", false);
        data.put("kilde", "BRUKER");
        data.put("annetFritekst", "En spesifikk forklaring.");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        assertThat(tekster).contains(new OppgaveAvsnitt("Årsak", "En spesifikk forklaring."));
    }

    @Test
    void annetFritekst_avsnitt_utelates_når_årsak_ikke_er_annet() {
        Map<String, Object> data = grunndata("IKKE_BOSATTADRESSE_I_TRONDHEIM", false);
        data.put("kilde", "BRUKER");
        data.put("annetFritekst", "Skal ikke vises.");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        assertThat(tekster).noneMatch(t -> t instanceof OppgaveAvsnitt a && a.innhold().equals("Skal ikke vises."));
        // hovedsetning + kildeblokk + de 2 om-varsel-avsnittene, ingen fritekst-avsnitt, ingen frist (fristDato=null)
        assertThat(tekster).hasSize(4);
    }

    @Test
    void om_varsel_seksjonen_er_alltid_med() {
        Map<String, Object> data = grunndata("IKKE_BOSATTADRESSE_I_TRONDHEIM", false);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        assertThat(tekster).contains(OM_VARSEL_1, OM_VARSEL_2);
    }

    @Test
    void svarfrist_vises_kun_når_fristDato_er_satt() {
        Map<String, Object> data = grunndata("IKKE_BOSATTADRESSE_I_TRONDHEIM", false);
        data.put("kilde", "BRUKER");
        data.put("fristDato", "2025-02-01");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        assertThat(tekster).contains(
            new OppgaveAvsnitt("Fristen for å svare er senest <b>1. februar 2025</b>."));
    }

    @Test
    void svarfrist_utelates_når_fristDato_mangler() {
        Map<String, Object> data = grunndata("IKKE_BOSATTADRESSE_I_TRONDHEIM", false);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        assertThat(tekster).noneMatch(t -> t instanceof OppgaveAvsnitt a
            && a.innhold() != null && a.innhold().contains("Fristen for å"));
    }

    private static Map<String, Object> grunndata(String årsak, boolean erPeriode) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("årsak", årsak);
        data.put("erPeriode", erPeriode);
        data.put("fom", FOM.toString());
        data.put("tom", erPeriode ? TOM.toString() : null);
        data.put("annetFritekst", null);
        data.put("kildeFritekst", null);
        return data;
    }
}
