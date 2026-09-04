package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.inject.Instance;
import java.lang.annotation.Annotation;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveListe;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTabell;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
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
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.InntektsrapporteringOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse.SøkYtelseOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bosted.BekreftBostedOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.endretperiode.EndretPeriodeOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.endretsluttdato.EndretSluttdatoOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.endretstartdato.EndretStartdatoOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.opphorvedmaksdato.BekreftOpphorVedMaksdatoOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tester det kuraterte brevinnholdet (PDF/min-side-varsel/DTO) per oppgavetype. Dekker:
 * <ul>
 *     <li>tittel og {@code tekster()} for alle 8 {@link OppgaveType}-verdier (én representativ
 *     "lykkelig sti"-scenario per type), samt at {@code varselLenke()} peker riktig sted</li>
 *     <li>alle grener i {@link EndretPeriodeOppgaveInnholdUtleder} (startdato/sluttdato/
 *     meldt-ut/fjernet/start-og-slutt/fallback), inkludert at innholdet ikke drifter fra de
 *     dedikerte typene for de grenene som deler tekst (jf. javadoc i typer-klassene)</li>
 *     <li>{@link BekreftBostedOppgaveInnholdUtleder}: bundet vs. opphør, samt alle
 *     {@link BostedsvilkårIkkeOppfyltÅrsak}- og {@link BostedsavklaringKildeType}-verdier</li>
 *     <li>{@link KontrollerRegisterinntektOppgaveInnholdUtleder}: inntektskombinasjoner og
 *     {@link YtelseType}-visningsnavn</li>
 *     <li>ytelseskvalifikator (ungdomsytelse vs. aktivitetspenger) og svarfrist-visning</li>
 * </ul>
 * Siden {@link OppgaveAvsnitt}/{@link OppgaveListe}/{@link OppgaveTabell} er records, kan hele
 * {@code tekster()}-lister sammenlignes med vanlig {@code equals} - ikke bare enkeltnøkler slik
 * det gamle {@code Map<String,Object>}-innholdet krevde.
 */
class OppgaveInnholdUtlederInnholdTest {

    private static final String UNGDOMSPROGRAM_BASE_URL = "https://ungdomsprogram-deltaker.example";
    private static final String AKTIVITETSPENGER_BASE_URL = "https://aktivitetspenger-innsyn.example";

    // ---------------------------------------------------------------------------------------
    // Uttømmende sveip: tittel + tekster + varselLenke for alle 8 oppgavetyper
    // ---------------------------------------------------------------------------------------

    @ParameterizedTest
    @EnumSource(OppgaveType.class)
    void utleder_gir_forventet_tittel_tekster_og_varselLenke(OppgaveType oppgaveType) {
        Scenario scenario = scenarioFor(oppgaveType);
        BrukerdialogOppgaveEntitet oppgave = oppgave(oppgaveType, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        String tittel = scenario.utleder().tittel(oppgave);
        List<OppgaveTekst> tekster = scenario.utleder().tekster(oppgave);
        String varselLenke = scenario.utleder().varselLenke(oppgave);

        assertThat(tittel).as("tittel for %s", oppgaveType).isEqualTo(scenario.forventetTittel());
        assertThat(tekster.get(0)).as("første tekstblokk skal være et avsnitt (varselteksten) for %s", oppgaveType)
            .isInstanceOf(OppgaveAvsnitt.class);
        assertThat(tekster).as("tekster for %s", oppgaveType).containsExactlyElementsOf(scenario.forventetTekster());
        assertThat(varselLenke).as("varselLenke for %s", oppgaveType).isEqualTo(scenario.varselLenkeHarOppgavereferanseSuffiks()
            ? scenario.forventetVarselLenkeBaseUrl() + "/oppgave" + oppgave.getOppgavereferanse()
            : scenario.forventetVarselLenkeBaseUrl());
    }

    private record Scenario(OppgaveInnholdUtleder utleder, String forventetTittel, List<OppgaveTekst> forventetTekster,
                             String forventetVarselLenkeBaseUrl, boolean varselLenkeHarOppgavereferanseSuffiks) {
    }

    private static final String STANDARD_SVAR_SETNING_1 =
        "Du får denne meldingen slik at du kan komme med en tilbakemelding på datoen. Du svarer på Min side på nav.no.";
    private static final String STANDARD_SVAR_SETNING_2 =
        "Ingen tilbakemelding? Kryss av på \"Nei\" med en gang og send inn svaret ditt. Jo fortere du svarer, jo fortere får vi behandlet saken din.";
    private static final String STANDARD_SVAR_SETNING_3 =
        "Har du en tilbakemelding? Ta kontakt med veilederen din først. Når dere har snakket sammen, sender du inn svaret ditt.";

    private static Scenario scenarioFor(OppgaveType oppgaveType) {
        return switch (oppgaveType) {
            case BEKREFT_BOSTED -> new Scenario(
                new BekreftBostedOppgaveInnholdUtleder(mappereSomGir(new BekreftBostedOppgavetypeDataDto(
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), true, null,
                    BostedsvilkårIkkeOppfyltÅrsak.ANNET, BostedsavklaringKildeType.BRUKER, null)), AKTIVITETSPENGER_BASE_URL),
                "Bekrefte bosted for aktivitetspenger",
                List.of(
                    new OppgaveAvsnitt("Du har fått en oppgave om å bekrefte bosted for aktivitetspenger."),
                    new OppgaveAvsnitt("Periode: 1. januar 2025 til 31. januar 2025.", true),
                    new OppgaveAvsnitt("Bor i Trondheim: Ja"),
                    new OppgaveAvsnitt("Annet."),
                    new OppgaveAvsnitt("Vi har fått opplysninger om dette fra deg."),
                    new OppgaveAvsnitt("Du får denne meldingen slik at du kan komme med en tilbakemelding på dette. Du svarer på Min side på nav.no."),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_2),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_3)),
                AKTIVITETSPENGER_BASE_URL, true);

            case BEKREFT_ENDRET_STARTDATO -> new Scenario(
                new EndretStartdatoOppgaveInnholdUtleder(mappereSomGir(
                    new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL),
                "Tilbakemelding på endret startdato i ungdomsprogrammet",
                List.of(
                    new OppgaveAvsnitt("Veilederen din har endret startdatoen din i ungdomsprogrammet til 1. februar 2025.", true),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_1),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_2),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_3)),
                UNGDOMSPROGRAM_BASE_URL, true);

            case BEKREFT_ENDRET_SLUTTDATO -> new Scenario(
                new EndretSluttdatoOppgaveInnholdUtleder(mappereSomGir(
                    new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 5, 31))), UNGDOMSPROGRAM_BASE_URL),
                "Tilbakemelding på endret sluttdato i ungdomsprogrammet",
                List.of(
                    new OppgaveAvsnitt("Veilederen din har endret sluttdatoen din i ungdomsprogrammet til 30. juni 2025.", true),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_1),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_2),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_3)),
                UNGDOMSPROGRAM_BASE_URL, true);

            case BEKREFT_ENDRET_PERIODE -> new Scenario(
                new EndretPeriodeOppgaveInnholdUtleder(mappereSomGir(new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO))), UNGDOMSPROGRAM_BASE_URL),
                "Tilbakemelding på endret startdato i ungdomsprogrammet",
                List.of(
                    new OppgaveAvsnitt("Veilederen din har endret startdatoen din i ungdomsprogrammet til 1. februar 2025.", true),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_1),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_2),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_3)),
                UNGDOMSPROGRAM_BASE_URL, true);

            case BEKREFT_AVVIK_REGISTERINNTEKT -> new Scenario(
                new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
                    new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                        new RegisterinntektDTO(List.of(), List.of()), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL),
                "Tilbakemelding på inntekt i januar 2025 \u2013 i ungdomsprogrammet",
                List.of(
                    new OppgaveAvsnitt("Du har gitt oss beskjed om at du hadde inntekt i januar, men vi har ikke fått inn opplysninger fra arbeidsgiver om at du hadde inntekt i januar."),
                    new OppgaveAvsnitt("Vi bruker opplysningene fra arbeidsgiver når vi vurderer hvor mye du får utbetalt. Når vi ikke har mottatt noe fra arbeidsgiver, vil vi basere oss på at du ikke hadde inntekt i januar."),
                    new OppgaveListe(List.of(
                        "Hvis inntekten stemmer, krysser du av for Ja, inntekten stemmer.",
                        "Hvis du mener at inntekten er feil, krysser du av på Nei, inntekten stemmer ikke og sender en tilbakemelding til oss om det."
                    ), true),
                    new OppgaveAvsnitt("Du svarer på Min side på nav.no."),
                    new OppgaveAvsnitt("Jo fortere du svarer, jo fortere får du pengene utbetalt.")),
                UNGDOMSPROGRAM_BASE_URL, true);

            case RAPPORTER_INNTEKT -> new Scenario(
                new InntektsrapporteringOppgaveInnholdUtleder(mappereSomGir(
                    new InntektsrapporteringOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), false)),
                    UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL),
                "Inntekt i januar 2025 \u2013 i ungdomsprogrammet",
                List.of(
                    new OppgaveAvsnitt("Gi oss beskjed hvis du hadde inntekt i januar. Inntekt er lønn, men det kan også være for eksempel etterbetaling, feriepenger, overtid og tillegg for ubekvem arbeidstid."),
                    new OppgaveAvsnitt("Inntekt er som regel lønnen du får fra en arbeidsgiver, men det kan være mange andre ting også. De vanligste formene for inntekt utenom lønn, er:"),
                    new OppgaveListe(List.of(
                        "etterbetaling", "feriepenger", "overtid",
                        "tillegg for kveld, natt, helg og helligdag (ubekvem arbeidstid)",
                        "tips", "frilansinntekt", "inntekt fra aksjeselskap (AS)")),
                    new OppgaveAvsnitt("Du kan lese mer om hva som regnes som inntekt i skatteloven §§ 5.10 til 5.15."),
                    new OppgaveAvsnitt("Du svarer på Min side på nav.no."),
                    new OppgaveAvsnitt("Hvis du hadde inntekt, krysser du av for Ja.", true),
                    new OppgaveAvsnitt("Hvis du ikke hadde inntekt, krysser du av på Nei eller lar være å svare.", true)),
                UNGDOMSPROGRAM_BASE_URL, true);

            case SØK_YTELSE -> new Scenario(
                new SøkYtelseOppgaveInnholdUtleder(mappereSomGir(
                    new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL),
                "Søknad for ungdomsprogramytelsen",
                List.of(
                    new OppgaveAvsnitt("Du er meldt inn i ungdomsprogrammet. Nå kan du søke om ungdomsprogramytelsen."),
                    new OppgaveAvsnitt("Startdato: 1. januar 2025", true),
                    new OppgaveAvsnitt("Du finner søknaden på Min side på nav.no.")),
                // Bevisst avvik, videreført fra opprinnelig oppførsel - se SøkYtelseOppgaveInnholdUtleder.
                UNGDOMSPROGRAM_BASE_URL, false);

            case BEKREFT_OPPHOR_VED_MAKSDATO -> new Scenario(
                new BekreftOpphorVedMaksdatoOppgaveInnholdUtleder(mappereSomGir(
                    new BekreftOpphorVedMaksdatoOppgavetypeDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 6, 30))), UNGDOMSPROGRAM_BASE_URL),
                "Tilbakemelding på sluttdato i ungdomsprogrammet",
                List.of(
                    new OppgaveAvsnitt("Din siste dag med ungdomsprogramytelsen er 30. juni 2025. Det er fordi du har brukt opp dagene du kan motta ungdomsprogramytelsen.", true),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_1),
                    new OppgaveAvsnitt("Ingen tilbakemelding? Kryss av på \"Nei\" med en gang og send inn svaret ditt.")),
                UNGDOMSPROGRAM_BASE_URL, true);
        };
    }

    // ---------------------------------------------------------------------------------------
    // EndretPeriode: alle grener (inkl. fallback for ukjent kombinasjon)
    // ---------------------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("endretPeriodeScenarioer")
    void endretPeriode_velger_riktig_gren(EndretPeriodeDataDto dto, String forventetTittel, OppgaveAvsnitt forventetFørsteTekst) {
        var utleder = new EndretPeriodeOppgaveInnholdUtleder(mappereSomGir(dto), UNGDOMSPROGRAM_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(utleder.tittel(oppgave)).isEqualTo(forventetTittel);
        assertThat(utleder.tekster(oppgave).get(0)).isEqualTo(forventetFørsteTekst);
    }

    private static Stream<Arguments> endretPeriodeScenarioer() {
        return Stream.of(
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 12, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO)),
                "Tilbakemelding på endret startdato i ungdomsprogrammet",
                new OppgaveAvsnitt("Veilederen din har endret startdatoen din i ungdomsprogrammet til 1. februar 2025.", true)),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 31)),
                    Set.of(PeriodeEndringType.ENDRET_SLUTTDATO)),
                "Tilbakemelding på endret sluttdato i ungdomsprogrammet",
                new OppgaveAvsnitt("Veilederen din har endret sluttdatoen din i ungdomsprogrammet til 30. juni 2025.", true)),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    null,
                    Set.of(PeriodeEndringType.ENDRET_SLUTTDATO)),
                "Tilbakemelding på sluttdato i ungdomsprogrammet",
                new OppgaveAvsnitt("Veilederen din har meldt deg ut i ungdomsprogrammet med sluttdato 30. juni 2025.", true)),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    Set.of(PeriodeEndringType.FJERNET_PERIODE)),
                "Tilbakemelding på stans av ungdomsprogramytelsen",
                new OppgaveAvsnitt("Veilederen din har meldt deg ut av ungdomsprogrammet fordi du ikke skal delta i programmet likevel.")),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 8, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO, PeriodeEndringType.ENDRET_SLUTTDATO)),
                "Tilbakemelding på ny start- og sluttdato for ungdomsprogramytelsen",
                new OppgaveAvsnitt("Veilederen din har endret start- og sluttdatoen din i ungdomsprogrammet. Vi vil derfor endre start- og sluttdatoen for ungdomsprogramytelsen også.")),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    null,
                    Set.of(PeriodeEndringType.ANDRE_ENDRINGER)),
                "Tilbakemelding på endring i perioden i ungdomsprogrammet",
                new OppgaveAvsnitt("Det er gjort en endring i perioden din i ungdomsprogrammet, med virkning fra 1. januar 2025 til 31. januar 2025.", true))
        );
    }

    @Test
    void endretPeriode_startdato_gren_drifter_ikke_fra_dedikert_type() {
        var periodeUtleder = new EndretPeriodeOppgaveInnholdUtleder(mappereSomGir(new EndretPeriodeDataDto(
            new PeriodeDTO(LocalDate.of(2025, 2, 1), null),
            new PeriodeDTO(LocalDate.of(2025, 1, 1), null),
            Set.of(PeriodeEndringType.ENDRET_STARTDATO))), UNGDOMSPROGRAM_BASE_URL);
        var dedikertUtleder = new EndretStartdatoOppgaveInnholdUtleder(mappereSomGir(
            new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(periodeUtleder.tittel(oppgave)).isEqualTo(dedikertUtleder.tittel(oppgave));
        assertThat(periodeUtleder.tekster(oppgave)).isEqualTo(dedikertUtleder.tekster(oppgave));
    }

    @Test
    void endretPeriode_sluttdato_gren_drifter_ikke_fra_dedikert_type() {
        var periodeUtleder = new EndretPeriodeOppgaveInnholdUtleder(mappereSomGir(new EndretPeriodeDataDto(
            new PeriodeDTO(null, LocalDate.of(2025, 6, 30)),
            new PeriodeDTO(null, LocalDate.of(2025, 5, 31)),
            Set.of(PeriodeEndringType.ENDRET_SLUTTDATO))), UNGDOMSPROGRAM_BASE_URL);
        var dedikertUtleder = new EndretSluttdatoOppgaveInnholdUtleder(mappereSomGir(
            new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 5, 31))), UNGDOMSPROGRAM_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(periodeUtleder.tittel(oppgave)).isEqualTo(dedikertUtleder.tittel(oppgave));
        assertThat(periodeUtleder.tekster(oppgave)).isEqualTo(dedikertUtleder.tekster(oppgave));
    }

    // ---------------------------------------------------------------------------------------
    // EndretSluttdato: meldt-ut vs. vanlig endring
    // ---------------------------------------------------------------------------------------

    @Test
    void endretSluttdato_meldtUt_når_forrigeSluttdato_mangler() {
        var utleder = new EndretSluttdatoOppgaveInnholdUtleder(mappereSomGir(
            new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), null)), UNGDOMSPROGRAM_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_SLUTTDATO, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(utleder.tittel(oppgave)).isEqualTo("Tilbakemelding på sluttdato i ungdomsprogrammet");
        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        assertThat(tekster.get(0)).isEqualTo(
            new OppgaveAvsnitt("Veilederen din har meldt deg ut i ungdomsprogrammet med sluttdato 30. juni 2025.", true));
    }

    // ---------------------------------------------------------------------------------------
    // BekreftBosted: bundet vs. opphør, og alle BostedsvilkårIkkeOppfyltÅrsak/-kildeType-verdier
    // ---------------------------------------------------------------------------------------

    @Test
    void bekreftBosted_bundet_periode_uten_ikkeOppfyltÅrsak() {
        var utleder = new BekreftBostedOppgaveInnholdUtleder(mappereSomGir(new BekreftBostedOppgavetypeDataDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), true, null, BostedsvilkårIkkeOppfyltÅrsak.UDEFINERT,
            BostedsavklaringKildeType.BRUKER, null)), AKTIVITETSPENGER_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        assertThat(tekster).containsExactly(
            new OppgaveAvsnitt("Du har fått en oppgave om å bekrefte bosted for aktivitetspenger."),
            new OppgaveAvsnitt("Periode: 1. januar 2025 til 31. januar 2025.", true),
            new OppgaveAvsnitt("Bor i Trondheim: Ja"),
            // Ingen ikkeOppfyltForklaring-avsnitt her - UDEFINERT gir null, se bostedIkkeOppfyltForklaring_dekker_alle_årsaker.
            new OppgaveAvsnitt("Vi har fått opplysninger om dette fra deg."),
            new OppgaveAvsnitt("Du får denne meldingen slik at du kan komme med en tilbakemelding på dette. Du svarer på Min side på nav.no."),
            new OppgaveAvsnitt(STANDARD_SVAR_SETNING_2),
            new OppgaveAvsnitt(STANDARD_SVAR_SETNING_3));
    }

    @Test
    void bekreftBosted_opphør_periode_uten_tom() {
        var utleder = new BekreftBostedOppgaveInnholdUtleder(mappereSomGir(new BekreftBostedOpphørOppgavetypeDataDto(
            LocalDate.of(2025, 1, 1), false, null, BostedsvilkårIkkeOppfyltÅrsak.IKKE_BOSATTADRESSE_I_TRONDHEIM,
            BostedsavklaringKildeType.BRUKER, null)), AKTIVITETSPENGER_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        assertThat(tekster).containsExactly(
            new OppgaveAvsnitt("Du har fått en oppgave om å bekrefte bosted for aktivitetspenger."),
            new OppgaveAvsnitt("Dette gjelder fra og med 1. januar 2025.", true),
            new OppgaveAvsnitt("Bor i Trondheim: Nei"),
            new OppgaveAvsnitt("Du er ikke registrert med bostedsadresse i Trondheim."),
            new OppgaveAvsnitt("Vi har fått opplysninger om dette fra deg."),
            new OppgaveAvsnitt("Du får denne meldingen slik at du kan komme med en tilbakemelding på dette. Du svarer på Min side på nav.no."),
            new OppgaveAvsnitt(STANDARD_SVAR_SETNING_2),
            new OppgaveAvsnitt(STANDARD_SVAR_SETNING_3));
    }

    @Test
    void bekreftBosted_tittel_er_uavhengig_av_ytelsetype() {
        var utleder = new BekreftBostedOppgaveInnholdUtleder(mappereSomGir(new BekreftBostedOppgavetypeDataDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), true, null, BostedsvilkårIkkeOppfyltÅrsak.UDEFINERT,
            BostedsavklaringKildeType.BRUKER, null)), AKTIVITETSPENGER_BASE_URL);

        assertThat(utleder.tittel(oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.UNGDOMSYTELSE, null)))
            .isEqualTo("Bekrefte bosted for aktivitetspenger");
        assertThat(utleder.tittel(oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.AKTIVITETSPENGER, null)))
            .isEqualTo("Bekrefte bosted for aktivitetspenger");
    }

    @ParameterizedTest
    @EnumSource(BostedsvilkårIkkeOppfyltÅrsak.class)
    void bostedIkkeOppfyltForklaring_dekker_alle_årsaker(BostedsvilkårIkkeOppfyltÅrsak årsak) {
        String forklaring = OppgaveTekster.bostedIkkeOppfyltForklaring(årsak, "min fritekst");
        switch (årsak) {
            case UDEFINERT -> assertThat(forklaring).isNull();
            case ANNET -> assertThat(forklaring).isEqualTo("min fritekst");
            default -> assertThat(forklaring).isNotBlank();
        }
    }

    @Test
    void bostedIkkeOppfyltForklaring_annet_uten_fritekst_faller_tilbake_til_generisk_tekst() {
        assertThat(OppgaveTekster.bostedIkkeOppfyltForklaring(BostedsvilkårIkkeOppfyltÅrsak.ANNET, null))
            .isEqualTo("Annet.");
        assertThat(OppgaveTekster.bostedIkkeOppfyltForklaring(BostedsvilkårIkkeOppfyltÅrsak.ANNET, "   "))
            .isEqualTo("Annet.");
    }

    @ParameterizedTest
    @EnumSource(BostedsavklaringKildeType.class)
    void bostedKildeForklaring_dekker_alle_kildetyper(BostedsavklaringKildeType kilde) {
        String forklaring = OppgaveTekster.bostedKildeForklaring(kilde, "en veileder hos Nav");
        assertThat(forklaring).isNotBlank();
        if (kilde == BostedsavklaringKildeType.ANNET) {
            assertThat(forklaring).contains("en veileder hos Nav");
        }
    }

    // ---------------------------------------------------------------------------------------
    // KontrollerRegisterinntekt: inntektskombinasjoner
    // ---------------------------------------------------------------------------------------

    @Test
    void avvikRegisterinntekt_ingen_inntekt() {
        var utleder = new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(), List.of()), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        assertThat(tekster).noneMatch(OppgaveTabell.class::isInstance);
        assertThat(avsnitt(tekster, 0).innhold()).isEqualTo(
            "Du har gitt oss beskjed om at du hadde inntekt i januar, men vi har ikke fått inn opplysninger fra arbeidsgiver om at du hadde inntekt i januar.");
    }

    @Test
    void avvikRegisterinntekt_kun_arbeidsinntekt() {
        var arbeid = new ArbeidOgFrilansRegisterInntektDTO(25000, "999999999", "Bedriften AS");
        var utleder = new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(arbeid), List.of()), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        OppgaveTabell tabell = tabell(tekster, 1);
        assertThat(tabell.kolonneOverskrifter()).containsExactly("Arbeidsgiver", "Inntekt før skatt");
        assertThat(tabell.rader()).containsExactly(List.of("Bedriften AS", "25 000 kr"), List.of("Totalt", "25 000 kr"));
        assertThat(avsnitt(tekster, 2).innhold())
            .isEqualTo("Vi bruker denne inntekten fra arbeidsgiver til å vurdere hvor mye du får utbetalt.");
    }

    @Test
    void avvikRegisterinntekt_arbeidsgiver_uten_navn_faller_tilbake_til_identifikator() {
        var arbeid = new ArbeidOgFrilansRegisterInntektDTO(10000, "999999999", null);
        var utleder = new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(arbeid), List.of()), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        OppgaveTabell tabell = tabell(utleder.tekster(oppgave), 1);
        assertThat(tabell.rader()).contains(List.of("999999999", "10 000 kr"));
    }

    @Test
    void avvikRegisterinntekt_kun_ytelseinntekt() {
        var ytelse = new YtelseRegisterInntektDTO(5000, YtelseType.DAGPENGER);
        var utleder = new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(), List.of(ytelse)), true)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        OppgaveTabell tabell = tabell(tekster, 1);
        assertThat(tabell.kolonneOverskrifter()).containsExactly("Nav-ytelse", "Inntekt før skatt");
        assertThat(tabell.rader()).containsExactly(List.of("Dagpenger", "5 000 kr"), List.of("Totalt", "5 000 kr"));
        // gjelderDelerAvMåned=true har forrang foran harKunYtelseInntekt - se if/else-rekkefølgen i utlederen.
        assertThat(avsnitt(tekster, 2).innhold()).isEqualTo(
            "Vi bruker ikke hele inntekten din, bare deler av den, når vi regner ut hvor mye penger du får. Det er fordi du ikke hadde ungdomsprogramytelsen hele måneden.");
    }

    @Test
    void avvikRegisterinntekt_arbeid_og_ytelse_kombinert() {
        var arbeid = new ArbeidOgFrilansRegisterInntektDTO(20000, "999999999", "Bedriften AS");
        var ytelse = new YtelseRegisterInntektDTO(5000, YtelseType.AAP);
        var utleder = new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(arbeid), List.of(ytelse)), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        OppgaveTabell tabell = tabell(tekster, 1);
        assertThat(tabell.kolonneOverskrifter()).containsExactly("Arbeidsgiver/Nav-ytelse", "Inntekt før skatt");
        assertThat(tabell.rader()).containsExactly(
            List.of("Bedriften AS", "20 000 kr"),
            List.of("Arbeidsavklaringspenger", "5 000 kr"),
            List.of("Totalt", "25 000 kr"));
        assertThat(avsnitt(tekster, 2).innhold())
            .isEqualTo("Vi bruker denne inntekten fra arbeidsgiver til å vurdere hvor mye du får utbetalt.");
    }

    @ParameterizedTest
    @MethodSource("ytelseTypeVisningsnavn")
    void avvikRegisterinntekt_ytelseType_visningsnavn(YtelseType type, String forventetNavn) {
        var ytelse = new YtelseRegisterInntektDTO(100, type);
        var utleder = new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(), List.of(ytelse)), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        OppgaveTabell tabell = tabell(utleder.tekster(oppgave), 1);
        assertThat(tabell.rader()).containsExactly(List.of(forventetNavn, "100 kr"), List.of("Totalt", "100 kr"));
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
        var utleder = new SøkYtelseOppgaveInnholdUtleder(mappereSomGir(
            new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 3, 1))), UNGDOMSPROGRAM_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.SØK_YTELSE, OppgaveYtelsetype.AKTIVITETSPENGER, null);

        assertThat(utleder.tittel(oppgave)).isEqualTo("Søknad om aktivitetspenger");
        assertThat(avsnitt(utleder.tekster(oppgave), 0).innhold()).isEqualTo("Du har søkt om aktivitetspenger.");
    }

    @Test
    void endretStartdato_aktivitetspenger_bruker_riktig_preposisjonsfrase() {
        var utleder = new EndretStartdatoOppgaveInnholdUtleder(mappereSomGir(
            new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL);
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.AKTIVITETSPENGER, null);

        assertThat(utleder.tittel(oppgave)).isEqualTo("Tilbakemelding på endret startdato for aktivitetspenger");
        assertThat(avsnitt(utleder.tekster(oppgave), 0)).isEqualTo(
            new OppgaveAvsnitt("Veilederen din har endret startdatoen din for aktivitetspenger til 1. februar 2025.", true));
    }

    @Test
    void svarfrist_tas_med_når_satt_og_utelates_når_null() {
        var utleder = new EndretStartdatoOppgaveInnholdUtleder(mappereSomGir(
            new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL);

        BrukerdialogOppgaveEntitet medFrist = oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.UNGDOMSYTELSE,
            LocalDateTime.of(2025, 2, 15, 12, 0));
        List<OppgaveTekst> teksterMedFrist = utleder.tekster(medFrist);
        assertThat(teksterMedFrist).hasSize(6);
        assertThat(avsnitt(teksterMedFrist, 4)).isEqualTo(
            new OppgaveAvsnitt("Fristen for å svare er senest 15. februar 2025.", true));
        assertThat(avsnitt(teksterMedFrist, 5).innhold()).isEqualTo(
            "Hvis vi ikke hører fra deg innen svarfristen har gått ut, bruker vi 1. februar 2025 som startdato når vi behandler saken din.");

        BrukerdialogOppgaveEntitet utenFrist = oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.UNGDOMSYTELSE, null);
        assertThat(utleder.tekster(utenFrist)).hasSize(4);
    }

    // ---------------------------------------------------------------------------------------
    // Testoppsett
    // ---------------------------------------------------------------------------------------

    private static OppgaveAvsnitt avsnitt(List<OppgaveTekst> tekster, int index) {
        return (OppgaveAvsnitt) tekster.get(index);
    }

    private static OppgaveTabell tabell(List<OppgaveTekst> tekster, int index) {
        return (OppgaveTabell) tekster.get(index);
    }

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
