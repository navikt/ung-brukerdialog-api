package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.kontrollerregisterinntekt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveListe;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTabell;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.ArbeidOgFrilansRegisterInntektDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.RegisterinntektDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseRegisterInntektDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseType;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.OppgaveTekster;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.pdf.NorskBeløpFormat;
import no.nav.ung.brukerdialog.pdf.NorskDatoFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Kilde: {@code sif-brukerdialog/.../oppgavepaneler/avvik-registerinntekt/i18n/nb.ts}, med
 * forgreningen gjenskapt fra {@code AvvikRegisterinntektOppgavetekst.tsx} og
 * {@code avvikRegisterinntektOppgaveUtils.ts}.
 * <p>
 * <b>Bevisst forenkling:</b> gjenskaper ikke frontendens {@code .ingenOpplysninger}-variant, som
 * sammenligner med et tidligere brukerselvrapportert tall vi ikke har tilgang til i denne DTO-en.
 */
@OppgaveTypeRef(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT)
@ApplicationScoped
public class KontrollerRegisterinntektOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;
    private String aktivitetspengerInnsynBaseUrl;

    KontrollerRegisterinntektOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public KontrollerRegisterinntektOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl,
        @KonfigVerdi(value = "AKTIVITETSPENGER_INNSYN_BASE_URL") String aktivitetspengerInnsynBaseUrl
    ) {
        this.mappere = mappere;
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
        this.aktivitetspengerInnsynBaseUrl = aktivitetspengerInnsynBaseUrl;
    }

    @Override
    public String tittel(BrukerdialogOppgaveEntitet oppgave) {
        KontrollerRegisterinntektOppgavetypeDataDto dto = hentDto(oppgave);
        return "Tilbakemelding på inntekt i %s \u2013 %s".formatted(
            NorskDatoFormat.månedÅr(dto.fraOgMed()),
            OppgaveTekster.ytelsePreposisjonsfrase(oppgave.getYtelsetype()));
    }

    @Override
    public List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave) {
        KontrollerRegisterinntektOppgavetypeDataDto dto = hentDto(oppgave);
        RegisterinntektDTO registerinntekt = dto.registerinntekt();
        List<ArbeidOgFrilansRegisterInntektDTO> arbeid = registerinntekt.arbeidOgFrilansInntekter() != null
            ? registerinntekt.arbeidOgFrilansInntekter() : List.of();
        List<YtelseRegisterInntektDTO> ytelse = registerinntekt.ytelseInntekter() != null
            ? registerinntekt.ytelseInntekter() : List.of();

        boolean harArbeidsgiverInntekt = !arbeid.isEmpty();
        boolean harYtelseInntekt = !ytelse.isEmpty();
        boolean harInntekt = harArbeidsgiverInntekt || harYtelseInntekt;
        boolean harKunYtelseInntekt = harYtelseInntekt && !harArbeidsgiverInntekt;
        String rapporteringsmåned = NorskDatoFormat.måned(dto.fraOgMed());
        String ytelseNavn = OppgaveTekster.ytelseNavn(oppgave.getYtelsetype());

        List<OppgaveTekst> tekster = new ArrayList<>();
        if (harInntekt) {
            tekster.add(new OppgaveAvsnitt(harKunYtelseInntekt
                ? "Vi har fått disse opplysningene om ytelse fra Nav for %s:".formatted(rapporteringsmåned)
                : "Vi har fått disse opplysningene fra arbeidsgiver om inntekten din for %s:".formatted(rapporteringsmåned)));

            String kildeHeader = harYtelseInntekt && harArbeidsgiverInntekt ? "Arbeidsgiver/Nav-ytelse"
                : harYtelseInntekt ? "Nav-ytelse" : "Arbeidsgiver";
            List<List<String>> rader = new ArrayList<>();
            for (ArbeidOgFrilansRegisterInntektDTO i : arbeid) {
                String kilde = (i.arbeidsgiverNavn() != null && !i.arbeidsgiverNavn().isBlank())
                    ? i.arbeidsgiverNavn() : i.arbeidsgiverIdentifikator();
                rader.add(List.of(kilde, NorskBeløpFormat.kroner(i.inntekt())));
            }
            for (YtelseRegisterInntektDTO i : ytelse) {
                rader.add(List.of(ytelseTypeNavn(i.ytelsetype()), NorskBeløpFormat.kroner(i.inntekt())));
            }
            rader.add(List.of("Totalt", NorskBeløpFormat.kroner(registerinntekt.totalInntekt())));
            tekster.add(new OppgaveTabell(List.of(kildeHeader, "Inntekt før skatt"), rader));

            if (dto.gjelderDelerAvMåned()) {
                tekster.add(new OppgaveAvsnitt("Vi bruker ikke hele inntekten din, bare deler av den, når vi regner ut hvor mye penger du får. Det er fordi du ikke hadde %s hele måneden."
                    .formatted(ytelseNavn)));
            } else if (harKunYtelseInntekt) {
                tekster.add(new OppgaveAvsnitt("Vi bruker denne inntekten til å vurdere hvor mye du får utbetalt."));
            } else {
                tekster.add(new OppgaveAvsnitt("Vi bruker denne inntekten fra arbeidsgiver til å vurdere hvor mye du får utbetalt."));
            }
        } else {
            tekster.add(new OppgaveAvsnitt("Du har gitt oss beskjed om at du hadde inntekt i %s, men vi har ikke fått inn opplysninger fra arbeidsgiver om at du hadde inntekt i %s."
                .formatted(rapporteringsmåned, rapporteringsmåned)));
            tekster.add(new OppgaveAvsnitt("Vi bruker opplysningene fra arbeidsgiver når vi vurderer hvor mye du får utbetalt. Når vi ikke har mottatt noe fra arbeidsgiver, vil vi basere oss på at du ikke hadde inntekt i %s."
                .formatted(rapporteringsmåned)));
        }

        tekster.add(new OppgaveListe(List.of(
            "Hvis inntekten stemmer, krysser du av for Ja, inntekten stemmer.",
            "Hvis du mener at inntekten er feil, krysser du av på Nei, inntekten stemmer ikke og sender en tilbakemelding til oss om det."
        ), true));
        tekster.add(new OppgaveAvsnitt("Du svarer på Min side på nav.no."));
        tekster.add(new OppgaveAvsnitt("Jo fortere du svarer, jo fortere får du pengene utbetalt."));
        OppgaveTekster.leggTilSvarfrist(tekster, oppgave.getFristTid(), "svare", harKunYtelseInntekt
            ? "Hvis vi ikke hører fra deg innen svarfristen, bruker vi inntekten vi har fått oppgitt."
            : "Hvis vi ikke hører fra deg innen svarfristen, bruker vi inntekten som arbeidsgiver har oppgitt.");
        return tekster;
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return switch (oppgave.getYtelsetype()) {
            case AKTIVITETSPENGER -> aktivitetspengerInnsynBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
            case UNGDOMSYTELSE -> ungdomsprogramytelsenDeltakerBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
        };
    }

    private KontrollerRegisterinntektOppgavetypeDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (KontrollerRegisterinntektOppgavetypeDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }

    /** Visningsnavn for {@code YtelseType}, gjenskapt fra {@code ung-innsyn/src/i18n/nb.ts}. */
    private static String ytelseTypeNavn(YtelseType type) {
        return switch (type) {
            case DAGPENGER -> "Dagpenger";
            case SYKEPENGER -> "Sykepenger";
            case FORELDREPENGER -> "Foreldrepenger";
            case PLEIEPENGER -> "Pleiepenger";
            case OMSORGSPENGER -> "Omsorgspenger";
            case OPPLÆRINGSPENGER -> "Opplæringspenger";
            case AAP -> "Arbeidsavklaringspenger";
            case ANNET -> "Annet";
        };
    }
}
