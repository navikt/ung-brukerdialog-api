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

/**
 * Én gren per årsakskode, slik at en ny kode i {@code AndreLivsoppholdsytelserIkkeOppfyltÅrsak} som ikke får egen tekst blir synlig her.
 */
class BekreftAndreLivsoppholdsytelserTekstfragmentRendererTest {

    private static final String MALNAVN = "tekstfragmenter/livsopphold/bekreft_andre_livsoppholdsytelser";
    private static final LocalDate FOM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2025, 1, 31);

    private static final String ANDRE_SETNING_FMT =
        " Du kan ikke få aktivitetspenger samtidig som du får %s.";

    private static final OppgaveAvsnitt OM_VARSEL_1 = new OppgaveAvsnitt(
        "Om «Varsel om nye opplysninger»",
        "Dette varselet sendes ut slik at du får mulighet til å komme med en tilbakemelding på opplysningene før Nav fatter vedtak. Tilbakemeldingen sendes inn via Min side på nav.no.");
    private static final OppgaveAvsnitt OM_VARSEL_2 = new OppgaveAvsnitt(
        "Hvis vi ikke hører noe fra deg, bruker Nav opplysningene over når vedtaket fattes.");

    private final OppgaveTekstfragmentRenderer renderer = new OppgaveTekstfragmentRenderer();

    static Stream<Arguments> årsakerOgYtelsesnavn() {
        return Stream.of(
            Arguments.of("MOTTAR_ARBEIDSAVKLARINGSPENGER", "arbeidsavklaringspenger"),
            Arguments.of("MOTTAR_TILTAKSPENGER", "tiltakspenger"),
            Arguments.of("MOTTAR_KVALIFISERINGSSTØNAD", "kvalifiseringsstønad"),
            Arguments.of("MOTTAR_DAGPENGER", "dagpenger"),
            Arguments.of("MOTTAR_FORELDREPENGER", "foreldrepenger"),
            Arguments.of("MOTTAR_SVANGERSKAPSPENGER", "svangerskapspenger"),
            Arguments.of("MOTTAR_UFØRETRYGD", "uføretrygd"),
            Arguments.of("MOTTAR_INTRODUKSJONSSTØNAD", "introduksjonsstønad"),
            Arguments.of("MOTTAR_BARNEPENSJON", "barnepensjon"),
            Arguments.of("MOTTAR_ANNEN_YTELSE", "en annen ytelse til livsopphold"),
            Arguments.of("UDEFINERT", "en annen ytelse til livsopphold")
        );
    }

    @ParameterizedTest
    @MethodSource("årsakerOgYtelsesnavn")
    void hovedsetning_navngir_ytelsen_for_hver_årsak(String årsak, String ytelsesnavn) {
        Map<String, Object> data = grunndata(årsak, true);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        assertThat(tekster.get(0)).isEqualTo(new OppgaveAvsnitt(
            "Vi har fått opplysninger om at du i perioden 1. januar 2025 til 31. januar 2025 får "
                + ytelsesnavn + "." + String.format(ANDRE_SETNING_FMT, ytelsesnavn)));
    }

    @ParameterizedTest
    @MethodSource("årsakerOgYtelsesnavn")
    void hovedsetning_bruker_åpen_periode_ved_opphør(String årsak, String ytelsesnavn) {
        Map<String, Object> data = grunndata(årsak, false);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        assertThat(tekster.get(0)).isEqualTo(new OppgaveAvsnitt(
            "Vi har fått opplysninger om at du fra 1. januar 2025 får " + ytelsesnavn + "."
                + String.format(ANDRE_SETNING_FMT, ytelsesnavn)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"BRUKER", "NAV", "ANNET"})
    void kildeblokk_dekker_alle_kildetyper(String kilde) {
        Map<String, Object> data = grunndata("MOTTAR_DAGPENGER", false);
        data.put("kilde", kilde);
        data.put("kildeFritekst", "kommunen");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        String forventetLabel = switch (kilde) {
            case "BRUKER" -> "Deg";
            case "NAV" -> "Nav";
            default -> "kommunen";
        };
        OppgaveTekst kildeAvsnitt = tekster.stream()
            .filter(t -> "Hvor har vi fått opplysningene fra?".equals(t.tittel()))
            .findFirst().orElseThrow();
        assertThat(kildeAvsnitt).isEqualTo(new OppgaveAvsnitt("Hvor har vi fått opplysningene fra?", forventetLabel));
    }

    @Test
    void årsakFritekst_gir_eget_avsnitt() {
        Map<String, Object> data = grunndata("MOTTAR_ANNEN_YTELSE", false);
        data.put("kilde", "BRUKER");
        data.put("årsakFritekst", "Du mottar stønad til livsopphold fra en annen offentlig ordning.");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        assertThat(tekster).contains(new OppgaveAvsnitt("Årsak", "Du mottar stønad til livsopphold fra en annen offentlig ordning."));
    }

    @Test
    void årsakFritekst_avsnitt_utelates_når_fritekst_mangler() {
        Map<String, Object> data = grunndata("MOTTAR_DAGPENGER", false);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        // hovedsetning + kildeblokk + de 2 om-varsel-avsnittene, ingen fritekst-avsnitt, ingen frist (fristDato=null)
        assertThat(tekster).hasSize(4);
    }

    @Test
    void om_varsel_seksjonen_er_alltid_med() {
        Map<String, Object> data = grunndata("MOTTAR_DAGPENGER", false);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        assertThat(tekster).contains(OM_VARSEL_1, OM_VARSEL_2);
    }

    @Test
    void om_varsel_seksjonen_er_ikke_del_av_varselinnholdet() {
        Map<String, Object> data = grunndata("MOTTAR_DAGPENGER", false);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> varselInnhold = renderer.rendre(MALNAVN, data).varselInnhold();

        assertThat(varselInnhold).doesNotContain(OM_VARSEL_1, OM_VARSEL_2);
    }

    @Test
    void svarfrist_vises_kun_når_fristDato_er_satt() {
        Map<String, Object> data = grunndata("MOTTAR_DAGPENGER", false);
        data.put("kilde", "BRUKER");
        data.put("fristDato", "2025-02-01");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data).alle();

        assertThat(tekster).contains(
            new OppgaveAvsnitt("Fristen for å svare er senest <b>1. februar 2025</b>."));
    }

    @Test
    void svarfrist_utelates_når_fristDato_mangler() {
        Map<String, Object> data = grunndata("MOTTAR_DAGPENGER", false);
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
        data.put("årsakFritekst", null);
        data.put("kildeFritekst", null);
        return data;
    }
}
