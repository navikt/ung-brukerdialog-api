package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.enterprise.inject.Instance;
import no.nav.ung.brukerdialog.pdf.PdfDokument;
import no.nav.ung.brukerdialog.pdf.PdfGenerator;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Rendrings-smoketest for alle 8 produksjonsmalene under {@code handlebars/typer/*.hbs}.
 * {@link OppgaveDokumentUtlederInnholdTest} verifiserer at Java-siden ({@code utledTittel}/
 * {@code utledInnholdsdata}) gir riktig data - denne testen verifiserer at den dataen faktisk
 * <b>rendres</b> riktig gjennom {@link PdfGenerator}: at hver mal og hver refererte partial
 * kompilerer, at hvert distinkte {@code {{#eq}}}/{{#if}}}-grenutfall (inkl. fallback-grenen for
 * ukjent {@code endringer}-kombinasjon) faktisk blir besøkt minst én gang, og at Java-datamodellens
 * nøkler stemmer overens med feltnavnene malene faktisk bruker (et navnefeil her ville gitt et
 * tomt/manglende tekstfragment i stedet for en synlig feil - se assertions under).
 * <p>
 * Handlebars validerer klammeparbalanse ({@code {{#if}}...{{/if}}}) for <b>hele</b> filen ved
 * kompilering, uavhengig av hvilken gren som faktisk rendres. Partial-referanser
 * ({@code {{> ...}}}) løses derimot lat, kun når den grenen faktisk besøkes - derfor må hver
 * partial-refererende gren (bl.a. alle fire grenene i {@code endret-periode.hbs}) dekkes av minst
 * ett scenario her for at testen skal fange en feil sti.
 * <p>
 * Bygger PDF-datamodellen for hånd, på nøyaktig samme form som
 * {@code JournalførOppgaveTask#byggPdfData}/{@code byggOppgaveData} - se de metodene der før du
 * endrer denne testens {@link #pdfDokument}.
 * <p>
 * {@link #oppretter_lesbar_pdf_for_visuell_kontroll} kjører de samme scenarioene, men skriver i
 * tillegg PDF-en til disk slik at man kan åpne og se hvordan brevene faktisk ser ut -
 * mønster hentet fra {@code k9-brukerdialog-prosessering} sine {@code *PdfGeneratorTest.kt}.
 */
class OppgaveDokumentUtlederRenderingTest {

    /**
     * Katalog for lesbare PDF-er skrevet ut av {@link #oppretter_lesbar_pdf_for_visuell_kontroll}
     * - relativt til modulens {@code basedir} (Maven Surefire sin arbeidskatalog som standard),
     * altså {@code brukerdialog-oppgave/tjeneste/target/pdf-preview/}. Gitignoret via
     * {@code pdf-preview/} i rot-{@code .gitignore} (dekkes i realiteten allerede av den
     * eksisterende bare {@code target}-regelen, men eksplisitt regel for tydelighet).
     */
    private static final Path PDF_PREVIEW_DIR = Paths.get("target", "pdf-preview");

    private final PdfGenerator pdfGenerator = new PdfGenerator();

    /**
     * Rydder og gjenoppretter {@link #PDF_PREVIEW_DIR} før hver testkjøring, slik at mappen
     * alltid reflekterer nøyaktig dagens scenario-sett - ingen foreldede filer fra
     * fjernede/omdøpte scenarioer henger igjen mellom kjøringer.
     */
    @BeforeAll
    static void ryddOppLesbarePdfer() throws IOException {
        if (Files.exists(PDF_PREVIEW_DIR)) {
            try (Stream<Path> filer = Files.walk(PDF_PREVIEW_DIR)) {
                for (Path fil : filer.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(fil);
                }
            }
        }
        Files.createDirectories(PDF_PREVIEW_DIR);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarioer")
    void rendrer_gyldig_pdf_med_forventet_innhold(RenderScenario scenario) {
        PdfDokument dokument = pdfDokument(scenario.utleder(), scenario.oppgave());

        byte[] pdf = pdfGenerator.genererPdf(dokument);
        assertThat(pdf).as("PDF-bytes for %s", scenario.beskrivelse()).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII))
            .as("PDF-magic-number for %s", scenario.beskrivelse())
            .isEqualTo("%PDF-");

        String html = pdfGenerator.tilHtml(dokument);
        assertThat(html)
            .as(scenario.beskrivelse())
            .contains(scenario.utleder().utledTittel(scenario.oppgave()))
            .contains("Kari Nordmann")
            .contains("01019099999")
            .contains(scenario.oppgave().getOppgavereferanse().toString())
            // Alle 8 maler skal si hvor svaret leveres, med en ekte lenke - ikke
            // bare et navn (og aldri "... her", som er umulig i et arkivert, statisk dokument).
            .contains("href=\"https://www.nav.no/minside\"")
            .doesNotContain("svaret ditt her")
            // Nav-logo + opprettelsesdato, høyrejustert i brevhodet ved siden av
            // navn/fødselsnummer. "15. januar 2025" er datoLang-formateringen av den hardkodede
            // "2025-01-15" i pdfDokument(...) - se NorskDatoFormat.
            .contains("classpath:handlebars/images/NAV_logo_digital_Red.png")
            .contains("15. januar 2025")
            .contains(scenario.forventetFragmenter().toArray(new String[0]));
    }

    /**
     * Skriver hvert scenario sin rendrede PDF til {@link #PDF_PREVIEW_DIR} for manuell visuell
     * kontroll (fonter, marger, linjeskift, sidetall) - noe rene tekst-assertions i
     * {@link #rendrer_gyldig_pdf_med_forventet_innhold} ikke fanger opp. Mønster hentet fra
     * {@code k9-brukerdialog-prosessering} sine {@code *PdfGeneratorTest.kt}-klasser.
     * Filnavnet er en slugifisert versjon av {@link RenderScenario#beskrivelse()}, så det er
     * trivielt å koble en fil på disk til scenariet i kildekoden.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarioer")
    void oppretter_lesbar_pdf_for_visuell_kontroll(RenderScenario scenario) throws IOException {
        PdfDokument dokument = pdfDokument(scenario.utleder(), scenario.oppgave());
        byte[] pdf = pdfGenerator.genererPdf(dokument);

        Path fil = PDF_PREVIEW_DIR.resolve(slug(scenario.beskrivelse()) + ".pdf");
        Files.write(fil, pdf);

        assertThat(fil).as("lesbar PDF for %s", scenario.beskrivelse()).exists();
        assertThat(Files.size(fil)).as("filstørrelse for %s", scenario.beskrivelse()).isPositive();
    }

    /**
     * Slugifiserer en scenario-beskrivelse til et trygt filnavn: små bokstaver, mellomrom/tegn
     * erstattet med {@code -}, ingen ledende/avsluttende bindestrek. Norske bokstaver (æøå)
     * beholdes - gyldig på både macOS/APFS og Linux/ext4.
     */
    private static String slug(String beskrivelse) {
        String slugifisert = beskrivelse.toLowerCase(Locale.ROOT).replaceAll("[^a-zæøå0-9]+", "-");
        return slugifisert.replaceAll("^-+|-+$", "");
    }

    /**
     * To dedikerte tester (utenfor sveipet over) som verifiserer at
     * {@code {{#isNotNull oppgave.svarfrist}}} faktisk utelater svarfrist-avsnittet når fristen
     * mangler - ikke bare at et tomt felt ikke gir en feil.
     */
    @Test
    void svarfrist_utelates_når_fristTid_mangler_bosted_opphør() {
        var utleder = new BekreftBostedOppgaveDokumentUtleder(mappereSomGir(
            new BekreftBostedOpphørOppgavetypeDataDto(LocalDate.of(2025, 3, 1), false, null,
                BostedsvilkårIkkeOppfyltÅrsak.UDEFINERT, BostedsavklaringKildeType.FOLKEREGISTER, null)));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.UNGDOMSYTELSE, null);

        String html = pdfGenerator.tilHtml(pdfDokument(utleder, oppgave));

        assertThat(html).doesNotContain("Fristen for å svare");
    }

    @Test
    void svarfrist_utelates_når_fristTid_mangler_søk_ytelse() {
        var utleder = new SøkYtelseOppgaveDokumentUtleder(mappereSomGir(
            new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 2, 1))));
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.SØK_YTELSE, OppgaveYtelsetype.AKTIVITETSPENGER, null);

        String html = pdfGenerator.tilHtml(pdfDokument(utleder, oppgave));

        assertThat(html).doesNotContain("Fristen for å søke");
    }

    // ---------------------------------------------------------------------------------------
    // Scenarioer - ett per distinkt gren på tvers av alle 8 maler, se klasse-javadoc
    // ---------------------------------------------------------------------------------------

    private record RenderScenario(String beskrivelse, OppgaveDokumentUtleder utleder,
                                   BrukerdialogOppgaveEntitet oppgave, List<String> forventetFragmenter) {
        @Override
        public String toString() {
            return beskrivelse;
        }
    }

    private static RenderScenario scenario(String beskrivelse, OppgaveDokumentUtleder utleder,
                                            BrukerdialogOppgaveEntitet oppgave, String... forventetFragmenter) {
        return new RenderScenario(beskrivelse, utleder, oppgave, List.of(forventetFragmenter));
    }

    private static Stream<Arguments> scenarioer() {
        return Stream.of(
            // --- bekreft-bosted.hbs: bundet/opphør, ikkeOppfyltForklaring, svarfrist ---
            Arguments.of(scenario("bosted - bundet periode, ANNET-årsak, svarfrist",
                new BekreftBostedOppgaveDokumentUtleder(mappereSomGir(new BekreftBostedOppgavetypeDataDto(
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), true,
                    "Bor midlertidig i utlandet", BostedsvilkårIkkeOppfyltÅrsak.ANNET, BostedsavklaringKildeType.ANNET, "en veileder hos Nav"))),
                oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.UNGDOMSYTELSE, LocalDateTime.of(2025, 2, 1, 0, 0)),
                "1. januar 2025", "31. januar 2025", "Bor i Trondheim: Ja", "Bor midlertidig i utlandet", "en veileder hos Nav",
                "Fristen for å svare er senest 1. februar 2025.")),

            Arguments.of(scenario("bosted - opphør, UDEFINERT-årsak, ingen svarfrist",
                new BekreftBostedOppgaveDokumentUtleder(mappereSomGir(new BekreftBostedOpphørOppgavetypeDataDto(
                    LocalDate.of(2025, 3, 1), false, null, BostedsvilkårIkkeOppfyltÅrsak.UDEFINERT, BostedsavklaringKildeType.FOLKEREGISTER, null))),
                oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.UNGDOMSYTELSE, null),
                "Dette gjelder fra og med", "1. mars 2025", "Bor i Trondheim: Nei")),

            // --- endret-startdato.hbs ---
            Arguments.of(scenario("endret startdato - ungdomsytelse, svarfrist",
                new EndretStartdatoOppgaveDokumentUtleder(mappereSomGir(
                    new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1)))),
                oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.UNGDOMSYTELSE, LocalDateTime.of(2025, 1, 20, 0, 0)),
                "endret startdatoen din i ungdomsprogrammet til", "1. februar 2025",
                "Fristen for å svare er senest 20. januar 2025.")),

            Arguments.of(scenario("endret startdato - aktivitetspenger, ingen svarfrist",
                new EndretStartdatoOppgaveDokumentUtleder(mappereSomGir(
                    new EndretStartdatoDataDto(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 2, 1)))),
                oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.AKTIVITETSPENGER, null),
                "endret startdatoen din for aktivitetspenger til", "1. mars 2025")),

            // --- endret-sluttdato.hbs: meldt-ut/endret ---
            Arguments.of(scenario("endret sluttdato - meldt ut, svarfrist",
                new EndretSluttdatoOppgaveDokumentUtleder(mappereSomGir(
                    new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), null))),
                oppgave(OppgaveType.BEKREFT_ENDRET_SLUTTDATO, OppgaveYtelsetype.UNGDOMSYTELSE, LocalDateTime.of(2025, 6, 1, 0, 0)),
                "meldt deg ut i ungdomsprogrammet med sluttdato", "30. juni 2025",
                "Fristen for å svare er senest 1. juni 2025.")),

            Arguments.of(scenario("endret sluttdato - endret (ikke meldt ut), ingen svarfrist",
                new EndretSluttdatoOppgaveDokumentUtleder(mappereSomGir(
                    new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 5, 31)))),
                oppgave(OppgaveType.BEKREFT_ENDRET_SLUTTDATO, OppgaveYtelsetype.UNGDOMSYTELSE, null),
                "endret sluttdatoen din i ungdomsprogrammet til", "30. juni 2025")),

            // --- endret-periode.hbs: alle 5 grener (STARTDATO/SLUTTDATO/meldt-ut/FJERNET/START_OG_SLUTT/fallback) ---
            Arguments.of(scenario("endret periode - gren STARTDATO, svarfrist",
                new EndretPeriodeOppgaveDokumentUtleder(mappereSomGir(new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 12, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO)))),
                oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, LocalDateTime.of(2025, 1, 15, 0, 0)),
                "endret startdatoen din i ungdomsprogrammet til", "1. februar 2025",
                "Fristen for å svare er senest 15. januar 2025.")),

            Arguments.of(scenario("endret periode - gren SLUTTDATO (ikke meldt ut), ingen svarfrist",
                new EndretPeriodeOppgaveDokumentUtleder(mappereSomGir(new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 31)),
                    Set.of(PeriodeEndringType.ENDRET_SLUTTDATO)))),
                oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null),
                "endret sluttdatoen din i ungdomsprogrammet til", "30. juni 2025")),

            Arguments.of(scenario("endret periode - gren SLUTTDATO, meldt ut via manglende forrige periode, svarfrist",
                new EndretPeriodeOppgaveDokumentUtleder(mappereSomGir(new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    null,
                    Set.of(PeriodeEndringType.ENDRET_SLUTTDATO)))),
                oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, LocalDateTime.of(2025, 5, 1, 0, 0)),
                "meldt deg ut i ungdomsprogrammet med sluttdato", "30. juni 2025",
                "Fristen for å svare er senest 1. mai 2025.")),

            Arguments.of(scenario("endret periode - gren FJERNET, ingen svarfrist",
                new EndretPeriodeOppgaveDokumentUtleder(mappereSomGir(new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)),
                    Set.of(PeriodeEndringType.FJERNET_PERIODE)))),
                oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null),
                "Veilederen din har meldt deg ut av ungdomsprogrammet fordi du ikke skal delta i programmet likevel.",
                "Du kan bare få ungdomsprogramytelsen hvis du deltar i programmet, og derfor stopper vi den.")),

            Arguments.of(scenario("endret periode - gren START_OG_SLUTT, svarfrist",
                new EndretPeriodeOppgaveDokumentUtleder(mappereSomGir(new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 8, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO, PeriodeEndringType.ENDRET_SLUTTDATO)))),
                oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, LocalDateTime.of(2025, 2, 15, 0, 0)),
                "ungdomsprogramytelsen i perioden", "1. mars 2025", "31. august 2025",
                "Fristen for å svare er senest 15. februar 2025.")),

            Arguments.of(scenario("endret periode - fallback (ANDRE_ENDRINGER), ingen svarfrist",
                new EndretPeriodeOppgaveDokumentUtleder(mappereSomGir(new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30)),
                    null,
                    Set.of(PeriodeEndringType.ANDRE_ENDRINGER)))),
                oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE, null),
                "Det er gjort en endring i perioden din i ungdomsprogrammet", "1. april 2025", "30. april 2025")),

            // --- bekreft-avvik-registerinntekt.hbs: ingen/kun-ytelse/arbeid+ytelse ---
            Arguments.of(scenario("avvik registerinntekt - ingen inntekt mottatt",
                new KontrollerRegisterinntektOppgaveDokumentUtleder(mappereSomGir(
                    new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                        new RegisterinntektDTO(List.of(), List.of()), false))),
                oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null),
                "Du har gitt oss beskjed om at du hadde inntekt i januar, men vi har ikke fått inn opplysninger fra arbeidsgiver")),

            Arguments.of(scenario("avvik registerinntekt - kun ytelse, deler av måned, svarfrist",
                new KontrollerRegisterinntektOppgaveDokumentUtleder(mappereSomGir(
                    new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28),
                        new RegisterinntektDTO(List.of(), List.of(
                            new YtelseRegisterInntektDTO(5000, YtelseType.DAGPENGER))), true))),
                oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, LocalDateTime.of(2025, 3, 10, 0, 0)),
                "Vi har fått disse opplysningene om ytelse fra Nav for februar", "Dagpenger", "5 000 kr",
                "ikke hadde ungdomsprogramytelsen hele måneden", "Fristen for å svare er senest 10. mars 2025.",
                "bruker vi inntekten vi har fått oppgitt")),

            Arguments.of(scenario("avvik registerinntekt - arbeid og ytelse kombinert, hel måned, ingen svarfrist",
                new KontrollerRegisterinntektOppgaveDokumentUtleder(mappereSomGir(
                    new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30),
                        new RegisterinntektDTO(
                            List.of(new ArbeidOgFrilansRegisterInntektDTO(20000, "999999999", "Bedriften AS")),
                            List.of(new YtelseRegisterInntektDTO(5000, YtelseType.AAP))), false))),
                oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, null),
                "Vi har fått disse opplysningene fra arbeidsgiver om inntekten din for april", "Bedriften AS",
                "20 000 kr", "Arbeidsavklaringspenger", "25 000 kr",
                "Vi bruker denne inntekten fra arbeidsgiver til å vurdere hvor mye du får utbetalt.")),

            // --- rapporter-inntekt.hbs ---
            Arguments.of(scenario("rapporter inntekt - deler av måned, svarfrist",
                new InntektsrapporteringOppgaveDokumentUtleder(mappereSomGir(
                    new InntektsrapporteringOppgavetypeDataDto(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30), true))),
                oppgave(OppgaveType.RAPPORTER_INNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE, LocalDateTime.of(2025, 5, 5, 0, 0)),
                "Gi oss beskjed hvis du hadde inntekt i april",
                "Du skal gi beskjed om hele inntekten du hadde i april, selv om du ikke hadde ungdomsprogramytelsen hele måneden.",
                "Fristen for å svare er senest 5. mai 2025.")),

            Arguments.of(scenario("rapporter inntekt - aktivitetspenger, hel måned, ingen svarfrist",
                new InntektsrapporteringOppgaveDokumentUtleder(mappereSomGir(
                    new InntektsrapporteringOppgavetypeDataDto(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31), false))),
                oppgave(OppgaveType.RAPPORTER_INNTEKT, OppgaveYtelsetype.AKTIVITETSPENGER, null),
                "Gi oss beskjed hvis du hadde inntekt i mai")),

            // --- sok-ytelse.hbs: ungdomsytelse/aktivitetspenger ---
            Arguments.of(scenario("søk ytelse - ungdomsytelse, svarfrist",
                new SøkYtelseOppgaveDokumentUtleder(mappereSomGir(
                    new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 1, 1)))),
                oppgave(OppgaveType.SØK_YTELSE, OppgaveYtelsetype.UNGDOMSYTELSE, LocalDateTime.of(2025, 1, 10, 0, 0)),
                "Du er meldt inn i ungdomsprogrammet. Nå kan du søke om ungdomsprogramytelsen.", "1. januar 2025",
                "Fristen for å søke er senest 10. januar 2025.")),

            Arguments.of(scenario("søk ytelse - aktivitetspenger, ingen svarfrist",
                new SøkYtelseOppgaveDokumentUtleder(mappereSomGir(
                    new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 2, 1)))),
                oppgave(OppgaveType.SØK_YTELSE, OppgaveYtelsetype.AKTIVITETSPENGER, null),
                "Du har søkt om aktivitetspenger.", "1. februar 2025")),

            // --- bekreft-opphor-ved-maksdato.hbs ---
            Arguments.of(scenario("opphør ved maksdato - ungdomsytelse, svarfrist",
                new BekreftOpphorVedMaksdatoOppgaveDokumentUtleder(mappereSomGir(
                    new BekreftOpphorVedMaksdatoOppgavetypeDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 6, 30)))),
                oppgave(OppgaveType.BEKREFT_OPPHOR_VED_MAKSDATO, OppgaveYtelsetype.UNGDOMSYTELSE, LocalDateTime.of(2025, 7, 1, 0, 0)),
                "Din siste dag med ungdomsprogramytelsen er", "30. juni 2025",
                "Fristen for å svare er senest 1. juli 2025.")),

            Arguments.of(scenario("opphør ved maksdato - aktivitetspenger, ingen svarfrist",
                new BekreftOpphorVedMaksdatoOppgaveDokumentUtleder(mappereSomGir(
                    new BekreftOpphorVedMaksdatoOppgavetypeDataDto(LocalDate.of(2025, 8, 31), LocalDate.of(2025, 8, 31)))),
                oppgave(OppgaveType.BEKREFT_OPPHOR_VED_MAKSDATO, OppgaveYtelsetype.AKTIVITETSPENGER, null),
                "Din siste dag med aktivitetspenger er", "31. august 2025"))
        );
    }

    // ---------------------------------------------------------------------------------------
    // Testoppsett - se OppgaveDokumentUtlederInnholdTest for samme mønster
    // ---------------------------------------------------------------------------------------

    /**
     * Bygger PDF-datamodellen på nøyaktig samme form som
     * {@code JournalførOppgaveTask#byggPdfData}/{@code byggOppgaveData} - se de metodene der.
     * Duplisert her (i stedet for gjort tilgjengelig fra produksjonskoden) fordi de er private
     * og kun binder sammen tittel/navn/fødselsnummer/oppgavedata til én {@code Map} - ingen
     * forretningslogikk å dele.
     */
    private static PdfDokument pdfDokument(OppgaveDokumentUtleder utleder, BrukerdialogOppgaveEntitet oppgave) {
        Map<String, Object> oppgaveData = new LinkedHashMap<>(utleder.utledInnholdsdata(oppgave));
        oppgaveData.putIfAbsent("oppgaveReferanse", oppgave.getOppgavereferanse().toString());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tittel", utleder.utledTittel(oppgave));
        data.put("opprettetDato", "2025-01-15");
        data.put("navn", "Kari Nordmann");
        data.put("fødselsnummer", "01019099999");
        data.put("oppgave", oppgaveData);

        return new PdfDokument(utleder.malnavn(), data);
    }

    private static BrukerdialogOppgaveEntitet oppgave(OppgaveType oppgaveType, OppgaveYtelsetype ytelsetype,
                                                        LocalDateTime fristTid) {
        return new BrukerdialogOppgaveEntitet(UUID.randomUUID(), oppgaveType, new AktørId("1234567890123"),
            ytelsetype, fristTid);
    }

    /**
     * Mocker CDI-oppslaget {@code Instance<OppgaveDataMapperFraEntitetTilDto>} slik at
     * {@link OppgaveDataMapperFraEntitetTilDto#finnTjeneste} returnerer en mapper som gir
     * {@code dto} uansett input - se {@code OppgaveDokumentUtlederInnholdTest} for samme mønster
     * og forklaring på {@code isResolvable()}-stubbingen.
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
