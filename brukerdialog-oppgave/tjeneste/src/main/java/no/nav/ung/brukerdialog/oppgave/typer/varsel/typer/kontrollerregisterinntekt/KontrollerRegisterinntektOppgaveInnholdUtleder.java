package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.kontrollerregisterinntekt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
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
import no.nav.ung.brukerdialog.pdf.OppgaveTekstfragmentRenderer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@OppgaveTypeRef(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT)
@ApplicationScoped
public class KontrollerRegisterinntektOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;
    private String aktivitetspengerInnsynBaseUrl;
    private OppgaveTekstfragmentRenderer renderer;

    KontrollerRegisterinntektOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public KontrollerRegisterinntektOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl,
        @KonfigVerdi(value = "AKTIVITETSPENGER_INNSYN_BASE_URL") String aktivitetspengerInnsynBaseUrl,
        OppgaveTekstfragmentRenderer renderer
    ) {
        this.mappere = mappere;
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
        this.aktivitetspengerInnsynBaseUrl = aktivitetspengerInnsynBaseUrl;
        this.renderer = renderer;
    }

    @Override
    public String undertittel(BrukerdialogOppgaveEntitet oppgave) {
        KontrollerRegisterinntektOppgavetypeDataDto dto = hentDto(oppgave);
        return "Inntekt i %s".formatted(NorskDatoFormat.månedÅr(dto.fraOgMed()));
    }

    @Override
    public List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave) {
        return rendre(oppgave).alle();
    }

    @Override
    public List<OppgaveTekst> varselInnhold(BrukerdialogOppgaveEntitet oppgave) {
        return rendre(oppgave).varselInnhold();
    }

    private OppgaveTekstfragmentRenderer.Resultat rendre(BrukerdialogOppgaveEntitet oppgave) {
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

        String kildeHeader = harYtelseInntekt && harArbeidsgiverInntekt ? "Arbeidsgiver/Nav-ytelse"
            : harYtelseInntekt ? "Nav-ytelse" : "Arbeidsgiver";

        List<Map<String, Object>> rader = new ArrayList<>();
        for (ArbeidOgFrilansRegisterInntektDTO i : arbeid) {
            String kilde = (i.arbeidsgiverNavn() != null && !i.arbeidsgiverNavn().isBlank())
                ? i.arbeidsgiverNavn() : i.arbeidsgiverIdentifikator();
            rader.add(Map.of("kilde", kilde, "beløp", NorskBeløpFormat.kroner(i.inntekt())));
        }
        for (YtelseRegisterInntektDTO i : ytelse) {
            rader.add(Map.of("kilde", ytelseTypeNavn(i.ytelsetype()), "beløp", NorskBeløpFormat.kroner(i.inntekt())));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("harInntekt", harInntekt);
        data.put("harKunYtelseInntekt", harKunYtelseInntekt);
        data.put("gjelderDelerAvMåned", dto.gjelderDelerAvMåned());
        data.put("rapporteringsmåned", rapporteringsmåned);
        data.put("ytelseNavn", ytelseNavn);
        data.put("kildeHeader", kildeHeader);
        data.put("rader", rader);
        data.put("totalBeløp", NorskBeløpFormat.kroner(registerinntekt.totalInntekt()));
        data.put("fristDato", OppgaveTekster.fristDato(oppgave.getFristTid()));

        return renderer.rendre("tekstfragmenter/kontroller_registerinntekt/kontroller_registerinntekt", data);
    }

    private static String ytelseTypeNavn(YtelseType ytelsetype) {
        return switch (ytelsetype) {
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
}
