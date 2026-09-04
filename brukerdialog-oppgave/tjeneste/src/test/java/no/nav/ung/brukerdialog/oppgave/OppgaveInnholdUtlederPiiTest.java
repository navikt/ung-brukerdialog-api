package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.inject.Instance;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveListe;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTabell;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsavklaringKildeType;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regresjonstest mot personopplysningslekkasje. {@link OppgaveInnholdUtleder} sin kontrakt sier
 * eksplisitt (se klassejavadoc der) at ingen implementasjon skal legge fødselsnummer eller navn
 * inn i teksten - kalleren (kun aktuelt for PDF-en) flettter dette inn selv, kun i brevhodet.
 * <p>
 * Denne testen kjører alle 8 produksjonsimplementasjonene mot en datarik variant av sin DTO
 * (fritekst-forklaringer, arbeidsgivernavn, org.nr, beløp o.l. - de personopplysningene som
 * FAKTISK er lov, se javadoc) og verifiserer at ingen av dem noensinne inneholder:
 * <ol>
 *     <li>11 sammenhengende siffer (fødselsnummer-formatet)</li>
 *     <li>selve aktørId-strengen til test-oppgaven (forsvar mot at en implementasjon ved en feil
 *     limer {@code oppgave.getAktørId()} rett inn i teksten)</li>
 * </ol>
 * i {@link OppgaveInnholdUtleder#tittel}, {@link OppgaveInnholdUtleder#tekster} eller
 * {@link OppgaveInnholdUtleder#varselLenke}. Utfyllende dekning av det faktiske tekstinnholdet
 * (inkl. eksakt likhet på hele {@code tekster()}-lista) finnes i
 * {@link OppgaveInnholdUtlederInnholdTest} - denne testen er et smalt, men bredt-dekkende
 * sikkerhetsnett på tvers av alle 8 typer, ikke en erstatning for den.
 */
class OppgaveInnholdUtlederPiiTest {

    private static final String UNGDOMSPROGRAM_BASE_URL = "https://ungdomsprogram-deltaker.example";
    private static final String AKTIVITETSPENGER_BASE_URL = "https://aktivitetspenger-innsyn.example";

    /**
     * Bevisst 11 sifre - formet som et fødselsnummer, selv om en {@link AktørId} i virkeligheten
     * er 13 sifre internt i Nav. Denne verdien fanger dermed opp BEGGE feilmåtene (rå
     * aktørId-lekkasje OG en reell fødselsnummer-formet streng) i én og samme sjekk.
     */
    private static final String MISTENKELIG_AKTØR_ID = "12345678901";

    private static final Pattern FØDSELSNUMMER_MØNSTER = Pattern.compile("\\d{11}");

    @ParameterizedTest(name = "{0}")
    @MethodSource("utledereMedDatarikeOppgaver")
    void tekster_inneholder_aldri_fødselsnummer_eller_aktørId(String beskrivelse, OppgaveInnholdUtleder utleder,
                                                                BrukerdialogOppgaveEntitet oppgave) {
        List<String> alleTekstLeaves = alleTekstLeaves(utleder, oppgave);

        assertThat(alleTekstLeaves).as("tekstinnhold for %s", beskrivelse).isNotEmpty();
        assertThat(alleTekstLeaves)
            .as("tekstinnhold for %s skal aldri inneholde aktørId-en", beskrivelse)
            .noneMatch(tekst -> tekst.contains(MISTENKELIG_AKTØR_ID));
        assertThat(alleTekstLeaves)
            .as("tekstinnhold for %s skal aldri inneholde et fødselsnummer-formet siffer-mønster", beskrivelse)
            .noneMatch(tekst -> FØDSELSNUMMER_MØNSTER.matcher(tekst).find());
    }

    private static List<String> alleTekstLeaves(OppgaveInnholdUtleder utleder, BrukerdialogOppgaveEntitet oppgave) {
        List<String> leaves = new ArrayList<>();
        leaves.add(utleder.tittel(oppgave));
        leaves.add(utleder.varselLenke(oppgave));
        for (OppgaveTekst tekst : utleder.tekster(oppgave)) {
            leaves.addAll(leaves(tekst));
        }
        return leaves;
    }

    private static List<String> leaves(OppgaveTekst tekst) {
        return switch (tekst) {
            case OppgaveAvsnitt avsnitt -> ikkeNull(avsnitt.tittel(), avsnitt.innhold());
            case OppgaveListe liste -> {
                List<String> punkter = new ArrayList<>(ikkeNull(liste.tittel()));
                punkter.addAll(liste.punkter());
                yield punkter;
            }
            case OppgaveTabell tabell -> {
                List<String> celler = new ArrayList<>(ikkeNull(tabell.tittel()));
                celler.addAll(tabell.kolonneOverskrifter());
                tabell.rader().forEach(celler::addAll);
                yield celler;
            }
        };
    }

    private static List<String> ikkeNull(String... verdier) {
        return Arrays.stream(verdier).filter(Objects::nonNull).toList();
    }

    // ---------------------------------------------------------------------------------------
    // Én datarik oppgave per type - se OppgaveInnholdUtlederInnholdTest for uttømmende
    // gren-for-gren-dekning av selve tekstinnholdet.
    // ---------------------------------------------------------------------------------------

    private static Stream<Arguments> utledereMedDatarikeOppgaver() {
        return Stream.of(
            Arguments.of("bekreft bosted",
                new BekreftBostedOppgaveInnholdUtleder(mappereSomGir(new BekreftBostedOppgavetypeDataDto(
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), true,
                    "Bor midlertidig i utlandet", BostedsvilkårIkkeOppfyltÅrsak.ANNET,
                    BostedsavklaringKildeType.ANNET, "en veileder hos Nav")), AKTIVITETSPENGER_BASE_URL),
                oppgave(OppgaveType.BEKREFT_BOSTED, OppgaveYtelsetype.UNGDOMSYTELSE)),

            Arguments.of("bekreft opphør ved maksdato",
                new BekreftOpphorVedMaksdatoOppgaveInnholdUtleder(mappereSomGir(
                    new BekreftOpphorVedMaksdatoOppgavetypeDataDto(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 6, 30))),
                    UNGDOMSPROGRAM_BASE_URL),
                oppgave(OppgaveType.BEKREFT_OPPHOR_VED_MAKSDATO, OppgaveYtelsetype.UNGDOMSYTELSE)),

            Arguments.of("endret periode - START_OG_SLUTT",
                new EndretPeriodeOppgaveInnholdUtleder(mappereSomGir(new EndretPeriodeDataDto(
                    new PeriodeDTO(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 8, 31)),
                    new PeriodeDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30)),
                    Set.of(PeriodeEndringType.ENDRET_STARTDATO, PeriodeEndringType.ENDRET_SLUTTDATO))),
                    UNGDOMSPROGRAM_BASE_URL),
                oppgave(OppgaveType.BEKREFT_ENDRET_PERIODE, OppgaveYtelsetype.UNGDOMSYTELSE)),

            Arguments.of("endret sluttdato - meldt ut",
                new EndretSluttdatoOppgaveInnholdUtleder(mappereSomGir(
                    new EndretSluttdatoDataDto(LocalDate.of(2025, 6, 30), null)), UNGDOMSPROGRAM_BASE_URL),
                oppgave(OppgaveType.BEKREFT_ENDRET_SLUTTDATO, OppgaveYtelsetype.UNGDOMSYTELSE)),

            Arguments.of("endret startdato",
                new EndretStartdatoOppgaveInnholdUtleder(mappereSomGir(
                    new EndretStartdatoDataDto(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL),
                oppgave(OppgaveType.BEKREFT_ENDRET_STARTDATO, OppgaveYtelsetype.UNGDOMSYTELSE)),

            Arguments.of("kontroller registerinntekt - arbeid og ytelse kombinert",
                new KontrollerRegisterinntektOppgaveInnholdUtleder(mappereSomGir(
                    new KontrollerRegisterinntektOppgavetypeDataDto(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30),
                        new RegisterinntektDTO(
                            List.of(new ArbeidOgFrilansRegisterInntektDTO(20000, "999999999", "Bedriften AS")),
                            List.of(new YtelseRegisterInntektDTO(5000, YtelseType.AAP))), false)),
                    UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL),
                oppgave(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE)),

            Arguments.of("rapporter inntekt",
                new InntektsrapporteringOppgaveInnholdUtleder(mappereSomGir(
                    new InntektsrapporteringOppgavetypeDataDto(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30), true)),
                    UNGDOMSPROGRAM_BASE_URL, AKTIVITETSPENGER_BASE_URL),
                oppgave(OppgaveType.RAPPORTER_INNTEKT, OppgaveYtelsetype.UNGDOMSYTELSE)),

            Arguments.of("søk ytelse",
                new SøkYtelseOppgaveInnholdUtleder(mappereSomGir(
                    new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 1, 1))), UNGDOMSPROGRAM_BASE_URL),
                oppgave(OppgaveType.SØK_YTELSE, OppgaveYtelsetype.UNGDOMSYTELSE))
        );
    }

    private static BrukerdialogOppgaveEntitet oppgave(OppgaveType oppgaveType, OppgaveYtelsetype ytelsetype) {
        return new BrukerdialogOppgaveEntitet(java.util.UUID.randomUUID(), oppgaveType,
            new AktørId(MISTENKELIG_AKTØR_ID), ytelsetype, LocalDateTime.of(2025, 12, 31, 0, 0));
    }

    /**
     * Mocker CDI-oppslaget {@code Instance<OppgaveDataMapperFraEntitetTilDto>} slik at
     * {@link OppgaveDataMapperFraEntitetTilDto#finnTjeneste} returnerer en mapper som gir
     * {@code dto} uansett input - se {@code OppgaveInnholdUtlederInnholdTest} for samme mønster.
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
