package no.nav.ung.brukerdialog.oppgave.journalforing.typer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.journalforing.pdf.NorskDatoFormat;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.ArbeidOgFrilansRegisterInntektDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.RegisterinntektDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseRegisterInntektDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseType;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentTekster;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentUtleder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
public class KontrollerRegisterinntektOppgaveDokumentUtleder implements OppgaveDokumentUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;

    KontrollerRegisterinntektOppgaveDokumentUtleder() {
        // for CDI proxy
    }

    @Inject
    public KontrollerRegisterinntektOppgaveDokumentUtleder(@Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere) {
        this.mappere = mappere;
    }

    @Override
    public String utledTittel(BrukerdialogOppgaveEntitet oppgave) {
        KontrollerRegisterinntektOppgavetypeDataDto dto = hentDto(oppgave);
        return "Tilbakemelding på inntekt i %s \u2013 %s".formatted(
            NorskDatoFormat.månedÅr(dto.fraOgMed()),
            OppgaveDokumentTekster.ytelsePreposisjonsfrase(oppgave.getYtelsetype()));
    }

    @Override
    public String malnavn() {
        return "typer/bekreft-avvik-registerinntekt";
    }

    @Override
    public Map<String, Object> utledInnholdsdata(BrukerdialogOppgaveEntitet oppgave) {
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

        List<Map<String, Object>> inntektsposter = new ArrayList<>();
        for (ArbeidOgFrilansRegisterInntektDTO i : arbeid) {
            Map<String, Object> post = new LinkedHashMap<>();
            post.put("kilde", (i.arbeidsgiverNavn() != null && !i.arbeidsgiverNavn().isBlank())
                ? i.arbeidsgiverNavn() : i.arbeidsgiverIdentifikator());
            post.put("beløp", i.inntekt());
            inntektsposter.add(post);
        }
        for (YtelseRegisterInntektDTO i : ytelse) {
            Map<String, Object> post = new LinkedHashMap<>();
            post.put("kilde", ytelseTypeNavn(i.ytelsetype()));
            post.put("beløp", i.inntekt());
            inntektsposter.add(post);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("harInntekt", harInntekt);
        data.put("harKunYtelseInntekt", harKunYtelseInntekt);
        data.put("gjelderDelerAvMåned", dto.gjelderDelerAvMåned());
        data.put("inntektsposter", inntektsposter);
        data.put("kildeHeader", harYtelseInntekt && harArbeidsgiverInntekt ? "Arbeidsgiver/Nav-ytelse"
            : harYtelseInntekt ? "Nav-ytelse" : "Arbeidsgiver");
        data.put("totalInntekt", registerinntekt.totalInntekt());
        data.put("rapporteringsmåned", NorskDatoFormat.måned(dto.fraOgMed()));
        data.put("ytelseNavn", OppgaveDokumentTekster.ytelseNavn(oppgave.getYtelsetype()));
        OppgaveDokumentTekster.leggTilSvarfrist(data, oppgave.getFristTid());
        return data;
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
