package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.enterprise.inject.Instance;
import java.lang.annotation.Annotation;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.EndretPeriodeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.PeriodeDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.PeriodeEndringType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretsluttdato.EndretSluttdatoDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretstartdato.EndretStartdatoDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.InntektsrapporteringOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.ArbeidOgFrilansRegisterInntektDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.RegisterinntektDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseRegisterInntektDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.opphorvedmaksdato.BekreftOpphorVedMaksdatoOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.BekreftBostedOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.BekreftOpphorVedMaksdatoOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.EndretPeriodeOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.EndretSluttdatoOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.EndretStartdatoOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.InntektsrapporteringOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.KontrollerRegisterinntektOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.SøkYtelseOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tester det kuraterte PDF-innholdet per oppgavetype. Dekker:
 * <ul>
 *     <li>tittel, {@code malnavn()} og innholdsdata for alle 8 {@link OppgaveType}-verdier
 *     (én representativ "lykkelig sti"-scenario per type)</li>
 *     <li>alle grener i {@link EndretPeriodeOppgaveDokumentUtleder} (startdato/sluttdato/
 *     meldt-ut/fjernet/start-og-slutt/fallback), inkludert at innholdet ikke drifter fra de
 *     dedikerte typene for de grenene som deler tekst (jf. javadoc i typer-klassene)</li>
 *     <li>{@link BekreftBostedOppgaveDokumentUtleder}: bundet vs. opphør, samt alle
 *     {@link BostedsvilkårIkkeOppfyltÅrsak}-verdier</li>
 *     <li>{@link KontrollerRegisterinntektOppgaveDokumentUtleder}: inntektskombinasjoner og
 *     {@link YtelseType}-visningsnavn</li>
 *     <li>ytelseskvalifikator (ungdomsytelse vs. aktivitetspenger) og svarfrist-visning</li>
 * </ul>
 */
class OppgaveDokumentUtlederInnholdTest {

    // ---------------------------------------------------------------------------------------
    // Uttømmende sveip: tittel + malnavn + innhold for alle 8 oppgavetyper
    // ---------------------------------------------------------------------------------------

    @ParameterizedTest
    @EnumSource(OppgaveType.class)
    void utleder_gir_forventet_tittel_malnavn_og_innhold(OppgaveType oppgaveType) {
        Scenario scenario = scenarioFor(oppgaveType);
        BrukerdialogOppgaveEntitet oppgave = oppgave(oppgaveType, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        String tittel = scenario.utleder().utledTittel(oppgave);
        String malnavn = scenario.utleder().malnavn();
        Map<String, Object> innhold = scenario.utleder().utledInnholdsdata(oppgave);

        assertThat(tittel).as("tittel for %s", oppgaveType).isEqualTo(scenario.forventetTittel());
        assertThat(malnavn).as("malnavn for %s", oppgaveType).isEqualTo(scenario.forventetMalnavn());
        assertThat(innhold).as("innhold for %s", oppgaveType)
            .containsExactlyInAnyOrderEntriesOf(scenario.forventetInnhold());
    }

    private record Scenario(OppgaveDokumentUtleder utleder, String forventetTittel, String forventetMalnavn,
                             Map<String, Object> forventetInnhold) {
    }

    private static Scenario scenarioFor(OppgaveType oppgaveType) {
        return switch (oppgaveType) {
            case BEKREFT_BOSTED -> new Scenario(
                new BekreftBostedOppgaveDokumentUtleder(mappereSomGir(new BekreftBostedOppgavetypeDataDto(
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), true, null,
                    BostedsvilkårIkkeOppfyltÅrsak.ANNET, BostedsavklaringKildeType.BRUKER, null))),
                "Bekrefte bosted for aktivitetspenger",
                "typer/bekreft-bosted",
                Map.of(
                    "fom", "2025-01-01",
                    "tom", "2025-01-31",
                    "erBosattITrondheim", "Ja",
                    "ikkeOppfyltForklaring", "Annet.",
                    "kildeForklaring", "Vi har fått opplysninger om dette fra deg."));

            case BEKREFT_ENDRET_STARTDATO -> new Scenario(
                new EndretStartdatoOppgaveDokumentUtleder(mappereSomGir(
                    new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1)))),
                "Tilbakemelding på endret startdato i ungdomsprogrammet",
                "typer/endret-startdato",
                Map.of(
                    "nyStartdato", "2025-02-01",
                    "forrigeStartdato", "2025-01-01",
                    "ytelsePreposisjonsfrase", "i ungdomsprogrammet"));

            case BEKREFT_ENDRET_SLUTTDATO -> new Scenario(
                new EndretSluttdatoOppgaveDokumentUtleder(mappereSomGir(
                    new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 5, 31)))),
                "Tilbakemelding på endret sluttdato i ungdomsprogrammet",
                "typer/endret-sluttdato",
                Map.of(
                    "nySluttdato", "2025-06-30",
                    "forrigeSluttdato", "2025-05-31",
                    "erMeldtUt", false,
                    "ytelsePreposisjonsfrase", "i ungdomsprogrammet"));

            case BEKREFT_ENDRET_PERIODE -> new Scenario(
                new EndretPeriodeOppgaveDokumentUtleder(mappereSomGir(new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO)))),
                "Tilbakemelding på endret startdato i ungdomsprogrammet",
                "typer/endret-periode",
                Map.of(
                    "nyStartdato", "2025-02-01",
                    "forrigeStartdato", "2025-01-01",
                    "ytelsePreposisjonsfrase", "i ungdomsprogrammet",
                    "periodeEndringType", "STARTDATO"));

            case BEKREFT_AVVIK_REGISTERINNTEKT -> new Scenario(
                new KontrollerRegisterinntektOppgaveDokumentUtleder(mappereSomGir(
                    new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                        new RegisterinntektDTO(List.of(), List.of()), false))),
                "Tilbakemelding på inntekt i januar 2025 \u2013 i ungdomsprogrammet",
                "typer/bekreft-avvik-registerinntekt",
                new HashMap<>(Map.of(
                    "harInntekt", false,
                    "harKunYtelseInntekt", false,
                    "gjelderDelerAvMåned", false,
                    "inntektsposter", List.of(),
                    "kildeHeader", "Arbeidsgiver",
                    "totalInntekt", 0,
                    "rapporteringsmåned", "januar",
                    "ytelseNavn", "ungdomsprogramytelsen")));

            case RAPPORTER_INNTEKT -> new Scenario(
                new InntektsrapporteringOppgaveDokumentUtleder(mappereSomGir(
                    new InntektsrapporteringOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), false))),
                "Inntekt i januar 2025 \u2013 i ungdomsprogrammet",
                "typer/rapporter-inntekt",
                Map.of(
                    "måned", "januar",
                    "gjelderDelerAvMåned", false,
                    "ytelseNavn", "ungdomsprogramytelsen"));

            case SØK_YTELSE -> new Scenario(
                new SøkYtelseOppgaveDokumentUtleder(mappereSomGir(
                    new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 1, 1)))),
                "Søknad for ungdomsprogramytelsen",
                "typer/sok-ytelse",
                Map.of(
                    "infotekst", "Du er meldt inn i ungdomsprogrammet. Nå kan du søke om ungdomsprogramytelsen.",
                    "fomDato", "2025-01-01"));

            case BEKREFT_OPPHOR_VED_MAKSDATO -> new Scenario(
                new BekreftOpphorVedMaksdatoOppgaveDokumentUtleder(mappereSomGir(
                    new BekreftOpphorVedMaksdatoOppgavetypeDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 6, 30)))),
                "Tilbakemelding på sluttdato i ungdomsprogrammet",
                "typer/bekreft-opphor-ved-maksdato",
                Map.of(
                    "sluttdato", "2025-06-30",
                    "ytelseNavn", "ungdomsprogramytelsen"));
        };
    }

    // ---------------------------------------------------------------------------------------
    // EndretPeriode: alle grener (inkl. fallback for ukjent kombinasjon)
    // ---------------------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("endretPeriodeScenarioer")
    void endretPeriode_velger_riktig_gren(EndretPeriodeDataDto dto, String forventetTittel, String forventetGren) {
        var utleder = new EndretPeriodeOppgaveDokumentUtleder(mappereSomGir(dto));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(utleder.utledTittel(oppgave)).isEqualTo(forventetTittel);
        assertThat(utleder.utledInnholdsdata(oppgave)).containsEntry("periodeEndringType", forventetGren);
    }

    private static Stream<Arguments> endretPeriodeScenarioer() {
        return Stream.of(
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 12, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO)),
                "Tilbakemelding på endret startdato i ungdomsprogrammet", "STARTDATO"),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 31)),
                    Set.of(PeriodeEndringType.ENDRET_SLUTTDATO)),
                "Tilbakemelding på endret sluttdato i ungdomsprogrammet", "SLUTTDATO"),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    null,
                    Set.of(PeriodeEndringType.ENDRET_SLUTTDATO)),
                "Tilbakemelding på sluttdato i ungdomsprogrammet", "SLUTTDATO"),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    Set.of(PeriodeEndringType.FJERNET_PERIODE)),
                "Tilbakemelding på stans av ungdomsprogramytelsen", "FJERNET"),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 8, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO, PeriodeEndringType.ENDRET_SLUTTDATO)),
                "Tilbakemelding på ny start- og sluttdato for ungdomsprogramytelsen", "START_OG_SLUTT"),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    null,
                    Set.of(PeriodeEndringType.ANDRE_ENDRINGER)),
                "Tilbakemelding på endring i perioden i ungdomsprogrammet", "UKJENT")
        );
    }

    @Test
    void endretPeriode_startdato_gren_drifter_ikke_fra_dedikert_type() {
        var periodeUtleder = new EndretPeriodeOppgaveDokumentUtleder(mappereSomGir(new EndretPeriodeDataDto(
            new PeriodeDTO(LocalDate.of(2025, 2, 1), null),
            new PeriodeDTO(LocalDate.of(2025, 1, 1), null),
            Set.of(PeriodeEndringType.ENDRET_STARTDATO))));
        var dedikertUtleder = new EndretStartdatoOppgaveDokumentUtleder(mappereSomGir(
            new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(periodeUtleder.utledTittel(oppgave)).isEqualTo(dedikertUtleder.utledTittel(oppgave));

        Map<String, Object> periodeInnhold = new HashMap<>(periodeUtleder.utledInnholdsdata(oppgave));
        periodeInnhold.remove("periodeEndringType");
        assertThat(periodeInnhold).isEqualTo(dedikertUtleder.utledInnholdsdata(oppgave));
    }

    @Test
    void endretPeriode_sluttdato_gren_drifter_ikke_fra_dedikert_type() {
        var periodeUtleder = new EndretPeriodeOppgaveDokumentUtleder(mappereSomGir(new EndretPeriodeDataDto(
            new PeriodeDTO(null, LocalDate.of(2025, 6, 30)),
            new PeriodeDTO(null, LocalDate.of(2025, 5, 31)),
            Set.of(PeriodeEndringType.ENDRET_SLUTTDATO))));
        var dedikertUtleder = new EndretSluttdatoOppgaveDokumentUtleder(mappereSomGir(
            new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 5, 31))));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(periodeUtleder.utledTittel(oppgave)).isEqualTo(dedikertUtleder.utledTittel(oppgave));

        Map<String, Object> periodeInnhold = new HashMap<>(periodeUtleder.utledInnholdsdata(oppgave));
        periodeInnhold.remove("periodeEndringType");
        assertThat(periodeInnhold).isEqualTo(dedikertUtleder.utledInnholdsdata(oppgave));
    }

    // ---------------------------------------------------------------------------------------
    // EndretSluttdato: meldt-ut vs. vanlig endring
    // ---------------------------------------------------------------------------------------

    @Test
    void endretSluttdato_meldtUt_når_forrigeSluttdato_mangler() {
        var utleder = new EndretSluttdatoOppgaveDokumentUtleder(mappereSomGir(
            new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), null)));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_SLUTTDATO, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(utleder.utledTittel(oppgave)).isEqualTo("Tilbakemelding på sluttdato i ungdomsprogrammet");
        Map<String, Object> innhold = utleder.utledInnholdsdata(oppgave);
        assertThat(innhold).containsEntry("erMeldtUt", true).containsEntry("nySluttdato", "2025-06-30");
        assertThat(innhold).doesNotContainKey("forrigeSluttdato");
    }

    // ---------------------------------------------------------------------------------------
    // BekreftBosted: bundet vs. opphør, og alle BostedsvilkårIkkeOppfyltÅrsak-verdier
    // ---------------------------------------------------------------------------------------

    @Test
    void bekreftBosted_bundet_periode_uten_ikkeOppfyltÅrsak() {
        var utleder = new BekreftBostedOppgaveDokumentUtleder(mappereSomGir(new BekreftBostedOppgavetypeDataDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), true, null, BostedsvilkårIkkeOppfyltÅrsak.UDEFINERT, BostedsavklaringKildeType.BRUKER, null)));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        Map<String, Object> innhold = utleder.utledInnholdsdata(oppgave);
        assertThat(innhold).containsEntry("fom", "2025-01-01")
            .containsEntry("tom", "2025-01-31")
            .containsEntry("erBosattITrondheim", "Ja")
            .doesNotContainKey("ikkeOppfyltForklaring");
    }

    @Test
    void bekreftBosted_opphør_periode_uten_tom() {
        var utleder = new BekreftBostedOppgaveDokumentUtleder(mappereSomGir(new BekreftBostedOpphørOppgavetypeDataDto(
            LocalDate.of(2025, 1, 1), false, null, BostedsvilkårIkkeOppfyltÅrsak.IKKE_BOSATTADRESSE_I_TRONDHEIM, BostedsavklaringKildeType.BRUKER, null)));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        Map<String, Object> innhold = utleder.utledInnholdsdata(oppgave);
        assertThat(innhold).containsEntry("fom", "2025-01-01")
            .containsEntry("erBosattITrondheim", "Nei")
            .containsEntry("ikkeOppfyltForklaring", "Du er ikke registrert med bostedsadresse i Trondheim.")
            .doesNotContainKey("tom");
    }

    @Test
    void bekreftBosted_tittel_er_uavhengig_av_ytelsetype() {
        var utleder = new BekreftBostedOppgaveDokumentUtleder(mappereSomGir(new BekreftBostedOppgavetypeDataDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), true, null, BostedsvilkårIkkeOppfyltÅrsak.UDEFINERT, BostedsavklaringKildeType.BRUKER, null)));

        assertThat(utleder.utledTittel(oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.UNGDOMSYTELSE, null)))
            .isEqualTo("Bekrefte bosted for aktivitetspenger");
        assertThat(utleder.utledTittel(oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.AKTIVITETSPENGER, null)))
            .isEqualTo("Bekrefte bosted for aktivitetspenger");
    }

    @ParameterizedTest
    @EnumSource(BostedsvilkårIkkeOppfyltÅrsak.class)
    void bostedIkkeOppfyltForklaring_dekker_alle_årsaker(BostedsvilkårIkkeOppfyltÅrsak årsak) {
        String forklaring = OppgaveDokumentTekster.bostedIkkeOppfyltForklaring(årsak, "min fritekst");
        switch (årsak) {
            case UDEFINERT -> assertThat(forklaring).isNull();
            case ANNET -> assertThat(forklaring).isEqualTo("min fritekst");
            default -> assertThat(forklaring).isNotBlank();
        }
    }

    @Test
    void bostedIkkeOppfyltForklaring_annet_uten_fritekst_faller_tilbake_til_generisk_tekst() {
        assertThat(OppgaveDokumentTekster.bostedIkkeOppfyltForklaring(BostedsvilkårIkkeOppfyltÅrsak.ANNET, null))
            .isEqualTo("Annet.");
        assertThat(OppgaveDokumentTekster.bostedIkkeOppfyltForklaring(BostedsvilkårIkkeOppfyltÅrsak.ANNET, "   "))
            .isEqualTo("Annet.");
    }

    // ---------------------------------------------------------------------------------------
    // KontrollerRegisterinntekt: inntektskombinasjoner
    // ---------------------------------------------------------------------------------------

    @Test
    void avvikRegisterinntekt_ingen_inntekt() {
        var utleder = new KontrollerRegisterinntektOppgaveDokumentUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(), List.of()), false)));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        Map<String, Object> innhold = utleder.utledInnholdsdata(oppgave);
        assertThat(innhold.get("harInntekt")).isEqualTo(false);
        assertThat(innhold.get("harKunYtelseInntekt")).isEqualTo(false);
        assertThat(innhold.get("kildeHeader")).isEqualTo("Arbeidsgiver");
        assertThat((List<?>) innhold.get("inntektsposter")).isEmpty();
        assertThat(innhold.get("totalInntekt")).isEqualTo(0);
    }

    @Test
    void avvikRegisterinntekt_kun_arbeidsinntekt() {
        var arbeid = new ArbeidOgFrilansRegisterInntektDTO(25000, "999999999", "Bedriften AS");
        var utleder = new KontrollerRegisterinntektOppgaveDokumentUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(arbeid), List.of()), false)));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        Map<String, Object> innhold = utleder.utledInnholdsdata(oppgave);
        assertThat(innhold.get("harInntekt")).isEqualTo(true);
        assertThat(innhold.get("harKunYtelseInntekt")).isEqualTo(false);
        assertThat(innhold.get("kildeHeader")).isEqualTo("Arbeidsgiver");
        assertThat(innhold.get("inntektsposter")).isEqualTo(List.of(Map.of("kilde", "Bedriften AS", "beløp", 25000)));
        assertThat(innhold.get("totalInntekt")).isEqualTo(25000);
    }

    @Test
    void avvikRegisterinntekt_arbeidsgiver_uten_navn_faller_tilbake_til_identifikator() {
        var arbeid = new ArbeidOgFrilansRegisterInntektDTO(10000, "999999999", null);
        var utleder = new KontrollerRegisterinntektOppgaveDokumentUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(arbeid), List.of()), false)));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        Map<String, Object> innhold = utleder.utledInnholdsdata(oppgave);
        assertThat(innhold.get("inntektsposter")).isEqualTo(List.of(Map.of("kilde", "999999999", "beløp", 10000)));
    }

    @Test
    void avvikRegisterinntekt_kun_ytelseinntekt() {
        var ytelse = new YtelseRegisterInntektDTO(5000, YtelseType.DAGPENGER);
        var utleder = new KontrollerRegisterinntektOppgaveDokumentUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(), List.of(ytelse)), true)));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        Map<String, Object> innhold = utleder.utledInnholdsdata(oppgave);
        assertThat(innhold.get("harInntekt")).isEqualTo(true);
        assertThat(innhold.get("harKunYtelseInntekt")).isEqualTo(true);
        assertThat(innhold.get("gjelderDelerAvMåned")).isEqualTo(true);
        assertThat(innhold.get("kildeHeader")).isEqualTo("Nav-ytelse");
        assertThat(innhold.get("inntektsposter")).isEqualTo(List.of(Map.of("kilde", "Dagpenger", "beløp", 5000)));
    }

    @Test
    void avvikRegisterinntekt_arbeid_og_ytelse_kombinert() {
        var arbeid = new ArbeidOgFrilansRegisterInntektDTO(20000, "999999999", "Bedriften AS");
        var ytelse = new YtelseRegisterInntektDTO(5000, YtelseType.AAP);
        var utleder = new KontrollerRegisterinntektOppgaveDokumentUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(arbeid), List.of(ytelse)), false)));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        Map<String, Object> innhold = utleder.utledInnholdsdata(oppgave);
        assertThat(innhold.get("harInntekt")).isEqualTo(true);
        assertThat(innhold.get("harKunYtelseInntekt")).isEqualTo(false);
        assertThat(innhold.get("kildeHeader")).isEqualTo("Arbeidsgiver/Nav-ytelse");
        assertThat(innhold.get("inntektsposter")).isEqualTo(List.of(
            Map.of("kilde", "Bedriften AS", "beløp", 20000),
            Map.of("kilde", "Arbeidsavklaringspenger", "beløp", 5000)));
        assertThat(innhold.get("totalInntekt")).isEqualTo(25000);
    }

    @ParameterizedTest
    @MethodSource("ytelseTypeVisningsnavn")
    void avvikRegisterinntekt_ytelseType_visningsnavn(YtelseType type, String forventetNavn) {
        var ytelse = new YtelseRegisterInntektDTO(100, type);
        var utleder = new KontrollerRegisterinntektOppgaveDokumentUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(), List.of(ytelse)), false)));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        Map<String, Object> innhold = utleder.utledInnholdsdata(oppgave);
        assertThat(innhold.get("inntektsposter")).isEqualTo(List.of(Map.of("kilde", forventetNavn, "beløp", 100)));
    }

    private static Stream<Arguments> ytelseTypeVisningsnavn() {
        return Stream.of(
            Arguments.of(YtelseType.DAGPENGER, "Dagpenger"),
            Arguments.of(YtelseType.SYKEPENGER, "Sykepenger"),
            Arguments.of(YtelseType.FORELDREPENGER, "Foreldrepenger"),
            Arguments.of(YtelseType.OMSORGSPENGER, "Omsorgspenger"),
            Arguments.of(YtelseType.PLEIEPENGER, "Pleiepenger"),
            Arguments.of(YtelseType.OPPLÆRINGSPENGER, "Opplæringspenger"),
            Arguments.of(YtelseType.AAP, "Arbeidsavklaringspenger"),
            Arguments.of(YtelseType.ANNET, "Annet")
        );
    }

    // ---------------------------------------------------------------------------------------
    // Ytelseskvalifikator (ungdomsytelse vs. aktivitetspenger) og svarfrist
    // ---------------------------------------------------------------------------------------

    @Test
    void søkYtelse_aktivitetspenger_gir_egen_tittel_og_infotekst() {
        var utleder = new SøkYtelseOppgaveDokumentUtleder(mappereSomGir(
            new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 3, 1))));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.SØK_YTELSE, OppgaveYtelsetype.AKTIVITETSPENGER, null);

        assertThat(utleder.utledTittel(oppgave)).isEqualTo("Søknad om aktivitetspenger");
        assertThat(utleder.utledInnholdsdata(oppgave)).containsEntry("infotekst", "Du har søkt om aktivitetspenger.");
    }

    @Test
    void endretStartdato_aktivitetspenger_bruker_riktig_preposisjonsfrase() {
        var utleder = new EndretStartdatoOppgaveDokumentUtleder(mappereSomGir(
            new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.AKTIVITETSPENGER, null);

        assertThat(utleder.utledTittel(oppgave)).isEqualTo("Tilbakemelding på endret startdato for aktivitetspenger");
        assertThat(utleder.utledInnholdsdata(oppgave)).containsEntry("ytelsePreposisjonsfrase", "for aktivitetspenger");
    }

    @Test
    void svarfrist_tas_med_når_satt_og_utelates_når_null() {
        var utleder = new EndretStartdatoOppgaveDokumentUtleder(mappereSomGir(
            new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))));

        BrukerdialogOppgaveEntitet medFrist = oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.UNGDOMSYTELSE,
            LocalDateTime.of(2025, 2, 15, 12, 0));
        assertThat(utleder.utledInnholdsdata(medFrist)).containsEntry("svarfrist", "2025-02-15");

        BrukerdialogOppgaveEntitet utenFrist = oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.UNGDOMSYTELSE, null);
        assertThat(utleder.utledInnholdsdata(utenFrist)).doesNotContainKey("svarfrist");
    }

    // ---------------------------------------------------------------------------------------
    // Testoppsett
    // ---------------------------------------------------------------------------------------

    private static BrukerdialogOppgaveEntitet oppgave(OppgaveType oppgaveType, OppgaveYtelsetype ytelsetype,
                                                        LocalDateTime fristTid) {
        return new BrukerdialogOppgaveEntitet(UUID.randomUUID(), oppgaveType, new AktørId("1234567890123"),
            ytelsetype, fristTid);
    }

    /**
     * Mocker CDI-oppslaget {@code Instance<OppgaveDataMapperFraEntitetTilDto>} slik at
     * {@link OppgaveDataMapperFraEntitetTilDto#finnTjeneste} returnerer en mapper som gir
     * {@code dto} uansett input. Annotasjonen/oppgavetypen i det faktiske oppslaget er derfor
     * uten betydning for testene her.
     */
    private static Instance<OppgaveDataMapperFraEntitetTilDto> mappereSomGir(OppgavetypeDataDto dto) {
        OppgaveDataMapperFraEntitetTilDto mapper = mock(OppgaveDataMapperFraEntitetTilDto.class);
        when(mapper.tilDto(any())).thenReturn(dto);

        @SuppressWarnings("unchecked")
        Instance<OppgaveDataMapperFraEntitetTilDto> instance = mock(Instance.class);
        when(instance.select(any(Annotation.class))).thenReturn(instance);
        when(instance.isResolvable()).thenReturn(true);
        when(instance.get()).thenReturn(mapper);
        return instance;
    }
}
