package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.inject.Instance;
import java.lang.annotation.Annotation;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgavePunktliste;
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
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.opphorvedmaksdato.BekreftOpphørVedMaksdatoOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.pdf.OppgaveTekstfragmentRenderer;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OppgaveInnholdUtlederInnholdTest {

    private static final String UNGDOMSPROGRAM_BASE_URL = "https://ungdomsprogram-deltaker.example";
    private static final String AKTIVITETSPENGER_BASE_URL = "https://aktivitetspenger-innsyn.example";

    // ---------------------------------------------------------------------------------------
    // Uttømmende sveip: tittel + tekster + varselLenke for alle 8 oppgavetyper
    // ---------------------------------------------------------------------------------------

    @ParameterizedTest
    @EnumSource(OppgaveType.class)
    void utleder_gir_forventet_undertittel_tekster_og_varselLenke(OppgaveType oppgaveType) {
        Scenario scenario = scenarioFor(oppgaveType);
        BrukerdialogOppgaveEntitet oppgave = oppgave(oppgaveType, standardYtelsetypeFor(oppgaveType), null);
        OppgaveInnholdUtleder utleder = scenario.utleder();

        String tittel = utleder.tittel(oppgave);
        String undertittel = utleder.undertittel(oppgave);
        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        String varselLenke = utleder.varselLenke(oppgave);

        assertThat(tittel).as("tittel skal være generisk for alle oppgavetyper (%s)", oppgaveType)
            .isEqualTo(OppgaveTekster.VARSEL_OM_NYE_OPPLYSNINGER_TITTEL);
        assertThat(undertittel).as("undertittel for %s", oppgaveType).isEqualTo(scenario.forventetUndertittel());
        assertThat(tekster.get(0)).as("første tekstblokk skal være et avsnitt (varselteksten) for %s", oppgaveType)
            .isInstanceOf(OppgaveAvsnitt.class);
        assertThat(tekster).as("tekster for %s", oppgaveType).containsExactlyElementsOf(scenario.forventetTekster());

        assertThat(varselLenke).as("varselLenke for %s", oppgaveType).isEqualTo(scenario.varselLenkeHarOppgavereferanseSuffiks()
            ? scenario.forventetVarselLenkeBaseUrl() + "/oppgave" + oppgave.getOppgavereferanse()
            : scenario.forventetVarselLenkeBaseUrl());
    }

    @ParameterizedTest
    @EnumSource(OppgaveType.class)
    void frist_vises_i_tekster_for_alle_oppgavetyper_når_satt(OppgaveType oppgaveType) {
        LocalDateTime fristTid = LocalDateTime.of(2025, 3, 15, 12, 0);
        Scenario scenario = scenarioFor(oppgaveType);
        BrukerdialogOppgaveEntitet oppgave = oppgave(oppgaveType, standardYtelsetypeFor(oppgaveType), fristTid);

        List<OppgaveTekst> tekster = scenario.utleder().tekster(oppgave);

        assertThat(tekster)
            .as("tekster for %s skal inneholde frist-frasen når fristTid er satt", oppgaveType)
            .anyMatch(tekst -> tekst instanceof OppgaveAvsnitt avsnitt && avsnitt.innhold().contains("er senest <b>15. mars 2025</b>."));
    }

    @ParameterizedTest
    @EnumSource(OppgaveType.class)
    void varselInnhold_er_delmengde_av_tekster_og_utelater_om_varsel(OppgaveType oppgaveType) {
        Scenario scenario = scenarioFor(oppgaveType);
        BrukerdialogOppgaveEntitet oppgave = oppgave(oppgaveType, standardYtelsetypeFor(oppgaveType), null);
        OppgaveInnholdUtleder utleder = scenario.utleder();

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        List<OppgaveTekst> varselInnhold = utleder.varselInnhold(oppgave);

        assertThat(tekster).as("tekster (%s) skal inneholde alle elementene i varselInnhold", oppgaveType)
            .containsAll(varselInnhold);
        assertThat(varselInnhold).as("varselInnhold (%s) skal ikke inneholde «Om «Varsel om nye opplysninger»»-avsnittene", oppgaveType)
            .doesNotContain(OM_VARSEL_1, OM_VARSEL_2);
    }

    @ParameterizedTest
    @EnumSource(OppgaveType.class)
    void min_side_varseltekst_er_innenfor_500_tegns_grensen_for_alle_typer(OppgaveType oppgaveType) {
        Scenario scenario = scenarioFor(oppgaveType);
        BrukerdialogOppgaveEntitet oppgave = oppgave(oppgaveType, standardYtelsetypeFor(oppgaveType), null);

        String varselTekst = ((OppgaveAvsnitt) scenario.utleder().tekster(oppgave).getFirst()).innhold();

        assertThat(varselTekst.length())
            .as("min-side-varselteksten (%s) er %d tegn, må være maks 500", oppgaveType, varselTekst.length())
            .isLessThanOrEqualTo(500);
    }

    private static OppgaveYtelsetype standardYtelsetypeFor(OppgaveType oppgaveType) {
        return oppgaveType == OppgaveType.BEKREFT_BOSTED
            ? OppgaveYtelsetype.AKTIVITETSPENGER
            : OppgaveYtelsetype.UNGDOMSYTELSE;
    }

    @Test
    void validerVarselTekstLengde_godtar_500_tegn_men_kaster_ved_501() {
        String femHundreTegn = "x".repeat(500);
        String femHundreOgÉnTegn = "x".repeat(501);

        OppgaveTekster.validerVarselTekstLengde(femHundreTegn, OppgaveType.SØK_YTELSE);

        assertThatIllegalStateException()
            .isThrownBy(() -> OppgaveTekster.validerVarselTekstLengde(femHundreOgÉnTegn, OppgaveType.SØK_YTELSE))
            .withMessageContaining("501")
            .withMessageContaining("500")
            .withMessageContaining("SØK_YTELSE");
    }

    private record Scenario(OppgaveInnholdUtleder utleder, String forventetUndertittel, List<OppgaveTekst> forventetTekster,
                             String forventetVarselLenkeBaseUrl, boolean varselLenkeHarOppgavereferanseSuffiks) {
    }

    private static final String STANDARD_SVAR_SETNING_1 =
        "Du får denne meldingen slik at du kan komme med en tilbakemelding på datoen. Du svarer på Min side på nav.no.";
    private static final String STANDARD_SVAR_SETNING_3 =
        "Har du en tilbakemelding? Ta kontakt med veilederen din først. Når dere har snakket sammen, sender du inn svaret ditt.";

    private static final OppgaveAvsnitt OM_VARSEL_1 = new OppgaveAvsnitt("Om «Varsel om nye opplysninger»",
        "Dette varselet sendes ut slik at du får mulighet til å komme med en tilbakemelding på opplysningene før Nav fatter vedtak. Tilbakemeldingen sendes inn via Min side på nav.no.");
    private static final OppgaveAvsnitt OM_VARSEL_2 =
        new OppgaveAvsnitt("Hvis vi ikke hører noe fra deg, bruker Nav opplysningene over når vedtaket fattes.");

    private static Scenario scenarioFor(OppgaveType oppgaveType) {
        return switch (oppgaveType) {
            case BEKREFT_BOSTED -> new Scenario(
                new BekreftBostedOppgaveInnholdUtleder(mappereSomGir(new BekreftBostedOppgavetypeDataDto(
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), true, null,
                    BostedsvilkårIkkeOppfyltÅrsak.ANNET, BostedsavklaringKildeType.BRUKER, null)), AKTIVITETSPENGER_BASE_URL,
                    new OppgaveTekstfragmentRenderer()),
                "Bostedsadresse",
                List.of(
                    new OppgaveAvsnitt("Vi har fått opplysninger om at du i perioden 1. januar 2025 til 31. januar 2025 ikke bor i Trondheim kommune. Du må bo i Trondheim kommune for å få aktivitetspenger."),
                    new OppgaveAvsnitt("Årsak", "Annet."),
                    new OppgaveAvsnitt("Hvor har vi fått opplysningene fra?", "Deg"),
                    OM_VARSEL_1, OM_VARSEL_2),
                AKTIVITETSPENGER_BASE_URL, true);

            case BEKREFT_ENDRET_STARTDATO -> new Scenario(
                new EndretStartdatoOppgaveInnholdUtleder(mappereSomGir(
                    new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer()),
                "Endret startdato",
                List.of(
                    new OppgaveAvsnitt("Veilederen din har endret startdatoen din i ungdomsprogrammet til <b>1. februar 2025</b>."),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_1),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_3),
                    OM_VARSEL_1, OM_VARSEL_2),
                UNGDOMSPROGRAM_BASE_URL, true);

            case BEKREFT_ENDRET_SLUTTDATO -> new Scenario(
                new EndretSluttdatoOppgaveInnholdUtleder(mappereSomGir(
                    new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 5, 31))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer()),
                "Endret sluttdato",
                List.of(
                    new OppgaveAvsnitt("Veilederen din har endret sluttdatoen din i ungdomsprogrammet til <b>30. juni 2025</b>."),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_1),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_3),
                    OM_VARSEL_1, OM_VARSEL_2),
                UNGDOMSPROGRAM_BASE_URL, true);

            case BEKREFT_ENDRET_PERIODE -> new Scenario(
                new EndretPeriodeOppgaveInnholdUtleder(mappereSomGir(new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer()),
                "Endret startdato",
                List.of(
                    new OppgaveAvsnitt("Veilederen din har endret startdatoen din i ungdomsprogrammet til <b>1. februar 2025</b>."),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_1),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_3),
                    OM_VARSEL_1, OM_VARSEL_2),
                UNGDOMSPROGRAM_BASE_URL, true);

            case BEKREFT_AVVIK_REGISTERINNTEKT -> new Scenario(
                new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
                    new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                        new RegisterinntektDTO(List.of(), List.of()), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL, new OppgaveTekstfragmentRenderer()),
                "Inntekt i januar 2025",
                List.of(
                    new OppgaveAvsnitt("Du har gitt oss beskjed om at du hadde inntekt i januar, men vi har ikke fått inn opplysninger fra arbeidsgiver om at du hadde inntekt i januar."),
                    new OppgaveAvsnitt("Vi bruker opplysningene fra arbeidsgiver når vi vurderer hvor mye du får utbetalt. Når vi ikke har mottatt noe fra arbeidsgiver, vil vi basere oss på at du ikke hadde inntekt i januar."),
                    new OppgaveAvsnitt("Du svarer på Min side på nav.no."),
                    new OppgaveAvsnitt("Jo fortere du svarer, jo fortere får du pengene utbetalt."),
                    OM_VARSEL_1, OM_VARSEL_2),
                UNGDOMSPROGRAM_BASE_URL, true);

            case RAPPORTER_INNTEKT -> new Scenario(
                new InntektsrapporteringOppgaveInnholdUtleder(mappereSomGir(
                    new InntektsrapporteringOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), false)),
                    UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL, new OppgaveTekstfragmentRenderer()),
                "Inntekt i januar 2025",
                List.of(
                    new OppgaveAvsnitt("Gi oss beskjed hvis du hadde inntekt i januar. Inntekt er lønn, men det kan også være for eksempel etterbetaling, feriepenger, overtid og tillegg for ubekvem arbeidstid."),
                    new OppgaveAvsnitt("Inntekt er som regel lønnen du får fra en arbeidsgiver, men det kan være mange andre ting også. De vanligste formene for inntekt utenom lønn, er:"),
                    new OppgavePunktliste(List.of(
                        "etterbetaling", "feriepenger", "overtid",
                        "tillegg for kveld, natt, helg og helligdag (ubekvem arbeidstid)",
                        "tips", "frilansinntekt", "inntekt fra aksjeselskap (AS)")),
                    new OppgaveAvsnitt("Du kan lese mer om hva som regnes som inntekt i skatteloven §§ 5.10 til 5.15."),
                    new OppgaveAvsnitt("Du svarer på Min side på nav.no.")),
                UNGDOMSPROGRAM_BASE_URL, true);

            case SØK_YTELSE -> new Scenario(
                new SøkYtelseOppgaveInnholdUtleder(mappereSomGir(
                    new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer()),
                "Søknad",
                List.of(
                    new OppgaveAvsnitt("Du er meldt inn i ungdomsprogrammet. Nå kan du søke om ungdomsprogramytelsen."),
                    new OppgaveAvsnitt("Startdato: <b>1. januar 2025</b>"),
                    new OppgaveAvsnitt("Du finner søknaden på Min side på nav.no.")),
                // Bevisst avvik, videreført fra opprinnelig oppførsel - se SøkYtelseOppgaveInnholdUtleder.
                UNGDOMSPROGRAM_BASE_URL, false);

            case BEKREFT_OPPHOR_VED_MAKSDATO -> new Scenario(
                new BekreftOpphørVedMaksdatoOppgaveInnholdUtleder(mappereSomGir(
                    new BekreftOpphorVedMaksdatoOppgavetypeDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 6, 30))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer()),
                "Sluttdato",
                List.of(
                    new OppgaveAvsnitt("Din siste dag med ungdomsprogramytelsen er <b>30. juni 2025</b>. Det er fordi du har brukt opp dagene du kan motta ungdomsprogramytelsen."),
                    new OppgaveAvsnitt(STANDARD_SVAR_SETNING_1),
                    OM_VARSEL_1, OM_VARSEL_2),
                UNGDOMSPROGRAM_BASE_URL, true);
        };
    }

    // ---------------------------------------------------------------------------------------
    // EndretPeriode: alle grener (inkl. fallback for ukjent kombinasjon)
    // ---------------------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("endretPeriodeScenarioer")
    void endretPeriode_velger_riktig_gren(EndretPeriodeDataDto dto, String forventetUndertittel, OppgaveAvsnitt forventetFørsteTekst) {
        var utleder = new EndretPeriodeOppgaveInnholdUtleder(mappereSomGir(dto), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(utleder.undertittel(oppgave)).isEqualTo(forventetUndertittel);
        assertThat(utleder.tekster(oppgave).get(0)).isEqualTo(forventetFørsteTekst);
    }

    private static Stream<Arguments> endretPeriodeScenarioer() {
        return Stream.of(
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 12, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO)),
                "Endret startdato",
                new OppgaveAvsnitt("Veilederen din har endret startdatoen din i ungdomsprogrammet til <b>1. februar 2025</b>.")),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 31)),
                    Set.of(PeriodeEndringType.ENDRET_SLUTTDATO)),
                "Endret sluttdato",
                new OppgaveAvsnitt("Veilederen din har endret sluttdatoen din i ungdomsprogrammet til <b>30. juni 2025</b>.")),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    null,
                    Set.of(PeriodeEndringType.ENDRET_SLUTTDATO)),
                "Sluttdato",
                new OppgaveAvsnitt("Veilederen din har meldt deg ut i ungdomsprogrammet med sluttdato <b>30. juni 2025</b>.")),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    Set.of(PeriodeEndringType.FJERNET_PERIODE)),
                "Stans",
                new OppgaveAvsnitt("Veilederen din har meldt deg ut av ungdomsprogrammet fordi du ikke skal delta i programmet likevel.")),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 8, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO, PeriodeEndringType.ENDRET_SLUTTDATO)),
                "Ny start- og sluttdato",
                new OppgaveAvsnitt("Veilederen din har endret start- og sluttdatoen din i ungdomsprogrammet. Vi vil derfor endre start- og sluttdatoen for ungdomsprogramytelsen også.")),
            Arguments.of(
                new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    null,
                    Set.of(PeriodeEndringType.ANDRE_ENDRINGER)),
                "Endring i perioden",
                new OppgaveAvsnitt("Det er gjort en endring i perioden din i ungdomsprogrammet, med virkning fra <b>1. januar 2025</b> til <b>31. januar 2025</b>."))
        );
    }

    @Test
    void endretPeriode_startdato_gren_drifter_ikke_fra_dedikert_type() {
        var periodeUtleder = new EndretPeriodeOppgaveInnholdUtleder(mappereSomGir(new EndretPeriodeDataDto(
            new PeriodeDTO(LocalDate.of(2025, 2, 1), null),
            new PeriodeDTO(LocalDate.of(2025, 1, 1), null),
            Set.of(PeriodeEndringType.ENDRET_STARTDATO))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer());
        var dedikertUtleder = new EndretStartdatoOppgaveInnholdUtleder(mappereSomGir(
            new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(periodeUtleder.undertittel(oppgave)).isEqualTo(dedikertUtleder.undertittel(oppgave));
        assertThat(periodeUtleder.tekster(oppgave)).isEqualTo(dedikertUtleder.tekster(oppgave));
    }

    @Test
    void endretPeriode_sluttdato_gren_drifter_ikke_fra_dedikert_type() {
        var periodeUtleder = new EndretPeriodeOppgaveInnholdUtleder(mappereSomGir(new EndretPeriodeDataDto(
            new PeriodeDTO(null, LocalDate.of(2025, 6, 30)),
            new PeriodeDTO(null, LocalDate.of(2025, 5, 31)),
            Set.of(PeriodeEndringType.ENDRET_SLUTTDATO))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer());
        var dedikertUtleder = new EndretSluttdatoOppgaveInnholdUtleder(mappereSomGir(
            new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 5, 31))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(periodeUtleder.undertittel(oppgave)).isEqualTo(dedikertUtleder.undertittel(oppgave));
        assertThat(periodeUtleder.tekster(oppgave)).isEqualTo(dedikertUtleder.tekster(oppgave));
    }

    // ---------------------------------------------------------------------------------------
    // EndretSluttdato: meldt-ut vs. vanlig endring
    // ---------------------------------------------------------------------------------------

    @Test
    void endretSluttdato_meldtUt_når_forrigeSluttdato_mangler() {
        var utleder = new EndretSluttdatoOppgaveInnholdUtleder(mappereSomGir(
            new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), null)), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_SLUTTDATO, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThat(utleder.undertittel(oppgave)).isEqualTo("Sluttdato");
        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        assertThat(tekster.get(0)).isEqualTo(
            new OppgaveAvsnitt("Veilederen din har meldt deg ut i ungdomsprogrammet med sluttdato <b>30. juni 2025</b>."));
    }

    // ---------------------------------------------------------------------------------------
    // BekreftBosted: bundet vs. opphør, og alle BostedsvilkårIkkeOppfyltÅrsak/-kildeType-verdier
    // ---------------------------------------------------------------------------------------

    @Test
    void bekreftBosted_bundet_periode_uten_ikkeOppfyltÅrsak() {
        var utleder = new BekreftBostedOppgaveInnholdUtleder(mappereSomGir(new BekreftBostedOppgavetypeDataDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), true, null, BostedsvilkårIkkeOppfyltÅrsak.UDEFINERT,
            BostedsavklaringKildeType.BRUKER, null)), AKTIVITETSPENGER_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.AKTIVITETSPENGER, null);

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        assertThat(tekster).containsExactly(
            new OppgaveAvsnitt("Vi har fått opplysninger om at du i perioden 1. januar 2025 til 31. januar 2025 ikke bor i Trondheim kommune. Du må bo i Trondheim kommune for å få aktivitetspenger."),
            // Ingen fritekst-avsnitt her - UDEFINERT er ikke ANNET, se OppgaveTekstfragmentRendererTest.
            new OppgaveAvsnitt("Hvor har vi fått opplysningene fra?", "Deg"),
            OM_VARSEL_1, OM_VARSEL_2);
    }

    @Test
    void bekreftBosted_opphør_periode_uten_tom() {
        var utleder = new BekreftBostedOppgaveInnholdUtleder(mappereSomGir(new BekreftBostedOpphørOppgavetypeDataDto(
            LocalDate.of(2025, 1, 1), false, null, BostedsvilkårIkkeOppfyltÅrsak.IKKE_BOSATTADRESSE_I_TRONDHEIM,
            BostedsavklaringKildeType.BRUKER, null)), AKTIVITETSPENGER_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.AKTIVITETSPENGER, null);

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        assertThat(tekster).containsExactly(
            new OppgaveAvsnitt("Vi har fått opplysninger om at du fra 1. januar 2025 ikke lenger bor i Trondheim kommune. Du må ha bostedsadresse i Trondheim kommune for å få aktivitetspenger."),
            new OppgaveAvsnitt("Hvor har vi fått opplysningene fra?", "Deg"),
            OM_VARSEL_1, OM_VARSEL_2);
    }

    @Test
    void bekreftBosted_feil_ytelsetype_kaster_illegalstateexception() {
        var utleder = new BekreftBostedOppgaveInnholdUtleder(mappereSomGir(new BekreftBostedOppgavetypeDataDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), true, null, BostedsvilkårIkkeOppfyltÅrsak.UDEFINERT,
            BostedsavklaringKildeType.BRUKER, null)), AKTIVITETSPENGER_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        assertThatIllegalStateException()
            .isThrownBy(() -> utleder.tekster(oppgave))
            .withMessageContaining("BEKREFT_BOSTED")
            .withMessageContaining("AKTIVITETSPENGER")
            .withMessageContaining("UNGDOMSYTELSE");
    }
    // Dekning for alle (årsak × dato/periode)-kombinasjoner, kildetyper og ANNET-fritekst er nå
    // testet direkte mot Handlebars-malen i OppgaveTekstfragmentRendererTest (pdf-modulen), siden
    // OppgaveTekster.bostedVarselTekst/bostedAnnetFritekst/bostedKildeLabel er fjernet - selve
    // forgreiningslogikken flyttet til tekstfragmenter/bosted/bekreft_bosted.hbs.

    // ---------------------------------------------------------------------------------------
    // KontrollerRegisterinntekt: inntektskombinasjoner
    // ---------------------------------------------------------------------------------------

    @Test
    void avvikRegisterinntekt_ingen_inntekt() {
        var utleder = new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(), List.of()), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL, new OppgaveTekstfragmentRenderer());
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
                new RegisterinntektDTO(List.of(arbeid), List.of()), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        OppgaveTabell tabell = tabell(tekster, 1);
        assertThat(tabell.kolonneOverskrifter()).containsExactly("Arbeidsgiver", "Inntekt før skatt");
        assertThat(tabell.rader()).containsExactly(List.of("Bedriften AS", "25\u00A0000\u00A0kr"), List.of("Totalt", "25\u00A0000\u00A0kr"));
        assertThat(avsnitt(tekster, 2).innhold())
            .isEqualTo("Vi bruker denne inntekten fra arbeidsgiver til å vurdere hvor mye du får utbetalt.");
    }

    @Test
    void avvikRegisterinntekt_arbeidsgiver_uten_navn_faller_tilbake_til_identifikator() {
        var arbeid = new ArbeidOgFrilansRegisterInntektDTO(10000, "999999999", null);
        var utleder = new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(arbeid), List.of()), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        OppgaveTabell tabell = tabell(utleder.tekster(oppgave), 1);
        assertThat(tabell.rader()).contains(List.of("999999999", "10\u00A0000\u00A0kr"));
    }

    @Test
    void avvikRegisterinntekt_kun_ytelseinntekt() {
        var ytelse = new YtelseRegisterInntektDTO(5000, YtelseType.DAGPENGER);
        var utleder = new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(), List.of(ytelse)), true)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        OppgaveTabell tabell = tabell(tekster, 1);
        assertThat(tabell.kolonneOverskrifter()).containsExactly("Nav-ytelse", "Inntekt før skatt");
        assertThat(tabell.rader()).containsExactly(List.of("Dagpenger", "5\u00A0000\u00A0kr"), List.of("Totalt", "5\u00A0000\u00A0kr"));
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
                new RegisterinntektDTO(List.of(arbeid), List.of(ytelse)), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        List<OppgaveTekst> tekster = utleder.tekster(oppgave);
        OppgaveTabell tabell = tabell(tekster, 1);
        assertThat(tabell.kolonneOverskrifter()).containsExactly("Arbeidsgiver/Nav-ytelse", "Inntekt før skatt");
        assertThat(tabell.rader()).containsExactly(
            List.of("Bedriften AS", "20\u00A0000\u00A0kr"),
            List.of("Arbeidsavklaringspenger", "5\u00A0000\u00A0kr"),
            List.of("Totalt", "25\u00A0000\u00A0kr"));
        assertThat(avsnitt(tekster, 2).innhold())
            .isEqualTo("Vi bruker denne inntekten fra arbeidsgiver til å vurdere hvor mye du får utbetalt.");
    }

    @ParameterizedTest
    @MethodSource("ytelseTypeVisningsnavn")
    void avvikRegisterinntekt_ytelseType_visningsnavn(YtelseType type, String forventetNavn) {
        var ytelse = new YtelseRegisterInntektDTO(100, type);
        var utleder = new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
            new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new RegisterinntektDTO(List.of(), List.of(ytelse)), false)), UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        OppgaveTabell tabell = tabell(utleder.tekster(oppgave), 1);
        assertThat(tabell.rader()).containsExactly(List.of(forventetNavn, "100\u00A0kr"), List.of("Totalt", "100\u00A0kr"));
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
    void søkYtelse_aktivitetspenger_kaster_illegalstateexception() {
        var utleder = new SøkYtelseOppgaveInnholdUtleder(mappereSomGir(
            new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 3, 1))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.SØK_YTELSE, OppgaveYtelsetype.AKTIVITETSPENGER, null);

        assertThatIllegalStateException()
            .isThrownBy(() -> utleder.tekster(oppgave))
            .withMessageContaining("SØK_YTELSE")
            .withMessageContaining("UNGDOMSYTELSE")
            .withMessageContaining("AKTIVITETSPENGER");
    }

    @Test
    void endretStartdato_aktivitetspenger_bruker_riktig_preposisjonsfrase() {
        var utleder = new EndretStartdatoOppgaveInnholdUtleder(mappereSomGir(
            new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer());
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.AKTIVITETSPENGER, null);

        assertThat(utleder.undertittel(oppgave)).isEqualTo("Endret startdato");
        assertThat(avsnitt(utleder.tekster(oppgave), 0)).isEqualTo(
            new OppgaveAvsnitt("Veilederen din har endret startdatoen din for aktivitetspenger til <b>1. februar 2025</b>."));
    }

    @Test
    void svarfrist_tas_med_når_satt_og_utelates_når_null() {
        var utleder = new EndretStartdatoOppgaveInnholdUtleder(mappereSomGir(
            new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL, new OppgaveTekstfragmentRenderer());

        BrukerdialogOppgaveEntitet medFrist = oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.UNGDOMSYTELSE,
            LocalDateTime.of(2025, 2, 15, 12, 0));
        List<OppgaveTekst> teksterMedFrist = utleder.tekster(medFrist);
        assertThat(teksterMedFrist).hasSize(6);
        assertThat(avsnitt(teksterMedFrist, 3)).isEqualTo(
            new OppgaveAvsnitt("Fristen for å svare er senest <b>15. februar 2025</b>."));

        BrukerdialogOppgaveEntitet utenFrist = oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.UNGDOMSYTELSE, null);
        assertThat(utleder.tekster(utenFrist)).hasSize(5);
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
