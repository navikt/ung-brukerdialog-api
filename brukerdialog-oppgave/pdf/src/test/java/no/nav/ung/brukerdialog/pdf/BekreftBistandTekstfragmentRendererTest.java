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
 * Samme rolle for bistandsfragmentet som {@link OppgaveTekstfragmentRendererTest} har for
 * bostedsfragmentet: dekker hver gren i malen direkte, uten å gå veien om innholdsutlederen.
 */
class BekreftBistandTekstfragmentRendererTest {

    private static final String MALNAVN = "tekstfragmenter/bistand/bekreft_bistand";
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
            Arguments.of("IKKE_14A_VEDTAK", false,
                "Vi har fått opplysninger om at du fra 1. januar 2025 ikke lenger har et vedtak fra Nav om behov for bistand til å komme i arbeid. Du må ha et slikt vedtak for å få aktivitetspenger."),
            Arguments.of("IKKE_14A_VEDTAK", true,
                "Vi har fått opplysninger om at du i perioden 1. januar 2025 til 31. januar 2025 ikke har et vedtak fra Nav om behov for bistand til å komme i arbeid. Du må ha et slikt vedtak for å få aktivitetspenger."),
            Arguments.of("UDEFINERT", false,
                "Vi har fått opplysninger om at du ikke lenger trenger hjelp fra Nav til å få jobb eller utdanning. Det betyr at du fra 1. januar 2025 ikke lenger får aktivitetspenger."),
            Arguments.of("UDEFINERT", true,
                "Vi har fått opplysninger om at du ikke trenger hjelp fra Nav til å få jobb eller utdanning. Det betyr at du i perioden 1. januar 2025 til 31. januar 2025 ikke får aktivitetspenger."),
            Arguments.of("KOMMET_I_UTDANNING", false,
                "Vi har fått opplysninger om at du har begynt på en utdanning og ikke lenger trenger hjelp fra Nav til å få jobb eller utdanning. Det betyr at du fra 1. januar 2025 ikke lenger får aktivitetspenger."),
            Arguments.of("KOMMET_I_UTDANNING", true,
                "Vi har fått opplysninger om at du har begynt på en utdanning og ikke trenger hjelp fra Nav til å få jobb eller utdanning. Det betyr at du i perioden 1. januar 2025 til 31. januar 2025 ikke får aktivitetspenger."),
            Arguments.of("KOMMET_I_ARBEID", false,
                "Vi har fått opplysninger om at du har begynt å jobbe og ikke lenger trenger hjelp fra Nav til å få jobb eller utdanning. Det betyr at du fra 1. januar 2025 ikke lenger får aktivitetspenger."),
            Arguments.of("KOMMET_I_ARBEID", true,
                "Vi har fått opplysninger om at du har begynt å jobbe og ikke trenger hjelp fra Nav til å få jobb eller utdanning. Det betyr at du i perioden 1. januar 2025 til 31. januar 2025 ikke får aktivitetspenger."),
            Arguments.of("ANNET", false,
                "Vi har fått opplysninger om at du ikke lenger trenger hjelp fra Nav til å få jobb eller utdanning. Det betyr at du fra 1. januar 2025 ikke lenger får aktivitetspenger."),
            Arguments.of("ANNET", true,
                "Vi har fått opplysninger om at du ikke trenger hjelp fra Nav til å få jobb eller utdanning. Det betyr at du i perioden 1. januar 2025 til 31. januar 2025 ikke får aktivitetspenger.")
        );
    }

    @ParameterizedTest
    @MethodSource("årsakOgTidsromKombinasjoner")
    void hovedsetning_dekker_alle_årsak_og_tidsrom_kombinasjoner(String årsak, boolean erPeriode, String forventet) {
        Map<String, Object> data = grunndata(årsak, erPeriode);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        assertThat(tekster.get(0)).isEqualTo(new OppgaveAvsnitt(forventet));
    }

    @ParameterizedTest
    @ValueSource(strings = {"BRUKER", "ANNET"})
    void kildeblokk_dekker_alle_kildetyper(String kilde) {
        Map<String, Object> data = grunndata("IKKE_14A_VEDTAK", false);
        data.put("kilde", kilde);
        data.put("kildeFritekst", "veilederen din");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        String forventetLabel = "BRUKER".equals(kilde) ? "Deg" : "veilederen din";
        OppgaveTekst kildeAvsnitt = tekster.stream()
            .filter(t -> "Hvor har vi fått opplysningene fra?".equals(t.tittel()))
            .findFirst().orElseThrow();
        assertThat(kildeAvsnitt).isEqualTo(new OppgaveAvsnitt("Hvor har vi fått opplysningene fra?", forventetLabel));
    }

    @Test
    void årsakFritekst_gir_eget_avsnitt() {
        Map<String, Object> data = grunndata("IKKE_14A_VEDTAK", false);
        data.put("kilde", "BRUKER");
        data.put("årsakFritekst", "Oppfølgingsvedtaket ditt ble avsluttet i desember.");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        assertThat(tekster).contains(new OppgaveAvsnitt("Årsak", "Oppfølgingsvedtaket ditt ble avsluttet i desember."));
    }

    @Test
    void årsakFritekst_avsnitt_utelates_når_fritekst_mangler() {
        Map<String, Object> data = grunndata("IKKE_14A_VEDTAK", false);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        // hovedsetning + kildeblokk + de 2 om-varsel-avsnittene, ingen fritekst-avsnitt, ingen frist (fristDato=null)
        assertThat(tekster).hasSize(4);
    }

    @Test
    void om_varsel_seksjonen_er_alltid_med() {
        Map<String, Object> data = grunndata("IKKE_14A_VEDTAK", false);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        assertThat(tekster).contains(OM_VARSEL_1, OM_VARSEL_2);
    }


    @Test
    void svarfrist_vises_kun_når_fristDato_er_satt() {
        Map<String, Object> data = grunndata("IKKE_14A_VEDTAK", false);
        data.put("kilde", "BRUKER");
        data.put("fristDato", "2025-02-01");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

        assertThat(tekster).contains(
            new OppgaveAvsnitt("Fristen for å svare er senest <b>1. februar 2025</b>."));
    }

    @Test
    void svarfrist_utelates_når_fristDato_mangler() {
        Map<String, Object> data = grunndata("IKKE_14A_VEDTAK", false);
        data.put("kilde", "BRUKER");

        List<OppgaveTekst> tekster = renderer.rendre(MALNAVN, data);

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
