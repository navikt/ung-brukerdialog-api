package no.nav.ung.brukerdialog.pdf;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BekreftAktivitetTekstfragmentRendererTest {

    private static final String MALNAVN = "tekstfragmenter/aktivitet/bekreft_aktivitet";
    private static final LocalDate FOM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2025, 1, 31);

    private static final OppgaveAvsnitt OM_VARSEL_1 = new OppgaveAvsnitt(
        "Om «Varsel om nye opplysninger»",
        "Dette varselet sendes ut slik at du får mulighet til å komme med en tilbakemelding på opplysningene før Nav fatter vedtak. Tilbakemeldingen sendes inn via Min side på nav.no.");
    private static final OppgaveAvsnitt OM_VARSEL_2 = new OppgaveAvsnitt(
        "Hvis vi ikke hører noe fra deg, bruker Nav opplysningene over når vedtaket fattes.");

    private final OppgaveTekstfragmentRenderer renderer = new OppgaveTekstfragmentRenderer();

    @ParameterizedTest
    @ValueSource(strings = {"ANNET", "UDEFINERT"})
    void hovedsetning_avgrenset_periode(String årsak) {
        Map<String, Object> data = grunndata(årsak, false);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        assertThat(tekster.get(0)).isEqualTo(new OppgaveAvsnitt(
            "Vi har fått opplysninger om at du i perioden 1. januar 2025 til 31. januar 2025 ikke er i aktivitet. Du må være i aktivitet for å få aktivitetspenger."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ANNET", "UDEFINERT"})
    void hovedsetning_bruker_åpen_periode_ved_opphør(String årsak) {
        Map<String, Object> data = grunndata(årsak, true);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        assertThat(tekster.get(0)).isEqualTo(new OppgaveAvsnitt(
            "Vi har fått opplysninger om at du fra 1. januar 2025 ikke lenger er i aktivitet. Du må være i aktivitet for å få aktivitetspenger."));
    }

    @ParameterizedTest
    @MethodSource("årsakOgTidsromKombinasjoner")
    void hovedsetning_for_spesifikke_årsaker(String årsak, boolean erOpphør, String forventet) {
        Map<String, Object> data = grunndata(årsak, erOpphør);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        assertThat(tekster.get(0)).isEqualTo(new OppgaveAvsnitt(forventet));
    }

    static Stream<Arguments> årsakOgTidsromKombinasjoner() {
        return Stream.of(
            Arguments.of("FRAVÆR_FRA_PLANLAGT_AKTIVITET", true,
                "Vi har fått opplysninger om at du fra 1. januar 2025 ikke lenger deltar i aktivitetene som er planlagt for deg. Du må følge aktivitetsplanen din for å få aktivitetspenger."),
            Arguments.of("FRAVÆR_FRA_PLANLAGT_AKTIVITET", false,
                "Vi har fått opplysninger om at du i perioden 1. januar 2025 til 31. januar 2025 ikke deltar i aktivitetene som er planlagt for deg. Du må følge aktivitetsplanen din for å få aktivitetspenger."),
            Arguments.of("AVSTÅR_FRA_TILBUD_OM_ARBEID_UTEN_RIMELIG_GRUNN", true,
                "Vi har fått opplysninger om at du 1. januar 2025 ikke startet i en jobb som du kunne ha startet i. Du kan miste retten til aktivitetspenger fra 1. januar 2025 siden du ikke startet i denne jobben."),
            Arguments.of("AVSTÅR_FRA_TILBUD_OM_ARBEID_UTEN_RIMELIG_GRUNN", false,
                "Vi har fått opplysninger om at du 1. januar 2025 ikke startet i en jobb som du kunne ha startet i. Du kan miste retten til aktivitetspenger i perioden 1. januar 2025 til 31. januar 2025 siden du ikke startet i denne jobben.")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"BRUKER", "NAV", "ANNET"})
    void kildeblokk_dekker_alle_kildetyper(String kilde) {
        Map<String, Object> data = grunndata("ANNET", true);
        data.put("kilde", kilde);
        data.put("kildeFritekst", "veilederen din");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        String forventetLabel = switch (kilde) {
            case "BRUKER" -> "Deg";
            case "NAV" -> "Nav";
            default -> "veilederen din";
        };
        OppgaveTekst kildeAvsnitt = tekster.stream()
            .filter(t -> "Hvor har vi fått opplysningene fra?".equals(t.tittel()))
            .findFirst().orElseThrow();
        assertThat(kildeAvsnitt).isEqualTo(new OppgaveAvsnitt("Hvor har vi fått opplysningene fra?", forventetLabel));
    }

    @Test
    void årsakFritekst_gir_eget_avsnitt() {
        Map<String, Object> data = grunndata("ANNET", true);
        data.put("kilde", "BRUKER");
        data.put("årsakFritekst", "Du har ikke møtt til den avtalte aktiviteten.");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        assertThat(tekster).contains(new OppgaveAvsnitt("Årsak", "Du har ikke møtt til den avtalte aktiviteten."));
    }

    @Test
    void årsakFritekst_avsnitt_utelates_når_fritekst_mangler() {
        Map<String, Object> data = grunndata("UDEFINERT", true);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        // hovedsetning + kildeblokk + de 2 om-varsel-avsnittene, ingen fritekst-avsnitt, ingen frist (fristDato=null)
        assertThat(tekster).hasSize(4);
    }

    @Test
    void om_varsel_seksjonen_er_alltid_med() {
        Map<String, Object> data = grunndata("ANNET", true);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        assertThat(tekster).contains(OM_VARSEL_1, OM_VARSEL_2);
    }


    @Test
    void svarfrist_vises_kun_når_fristDato_er_satt() {
        Map<String, Object> data = grunndata("ANNET", true);
        data.put("kilde", "BRUKER");
        data.put("fristDato", "2025-02-01");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        assertThat(tekster).contains(
            new OppgaveAvsnitt("Fristen for å svare er senest <b>1. februar 2025</b>."));
    }

    @Test
    void svarfrist_utelates_når_fristDato_mangler() {
        Map<String, Object> data = grunndata("ANNET", true);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        assertThat(tekster).noneMatch(t -> t instanceof OppgaveAvsnitt a
            && a.innhold() != null && a.innhold().contains("Fristen for å"));
    }

    private static Map<String, Object> grunndata(String årsak, boolean erOpphør) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("årsak", årsak);
        data.put("erOpphør", erOpphør);
        data.put("fom", FOM.toString());
        data.put("tom", erOpphør ? null : TOM.toString());
        data.put("årsakFritekst", null);
        data.put("kildeFritekst", null);
        return data;
    }
}
