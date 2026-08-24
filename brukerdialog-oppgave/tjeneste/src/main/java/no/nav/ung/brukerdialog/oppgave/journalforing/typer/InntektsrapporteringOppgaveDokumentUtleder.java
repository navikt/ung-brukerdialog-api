package no.nav.ung.brukerdialog.oppgave.journalforing.typer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.journalforing.pdf.NorskDatoFormat;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.InntektsrapporteringOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentTekster;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentUtleder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Brevteksten er tilpasset fra
 * {@code sif-brukerdialog/packages/ung-innsyn/.../oppgavepaneler/rapporter-inntekt/i18n/nb.ts} og
 * {@code RapporterInntektOppgavetekst.tsx}. «Les mer om inntekt»-innholdet
 * (eksempler på hva som regnes som inntekt) er i kilden en sammenleggbar {@code <ReadMore>}, men
 * skrives her ut som vanlig løpende tekst - et arkivert PDF-brev har ingen sammenleggbar
 * visning, og innholdet er en del av selve oppgavetekstens eget innhold (ikke en delt
 * regelverkshenvisning, i motsetning til {@code avvik-registerinntekt}s
 * {@code regelverkOgInnsyn}-blokk, som ikke hører til denne komponenten og derfor er utelatt der).
 */
@OppgaveTypeRef(OppgaveType.RAPPORTER_INNTEKT)
@ApplicationScoped
public class InntektsrapporteringOppgaveDokumentUtleder implements OppgaveDokumentUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;

    InntektsrapporteringOppgaveDokumentUtleder() {
        // for CDI proxy
    }

    @Inject
    public InntektsrapporteringOppgaveDokumentUtleder(@Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere) {
        this.mappere = mappere;
    }

    @Override
    public String utledTittel(BrukerdialogOppgaveEntitet oppgave) {
        InntektsrapporteringOppgavetypeDataDto dto = hentDto(oppgave);
        return "Inntekt i %s \u2013 %s".formatted(
            NorskDatoFormat.månedÅr(dto.fraOgMed()),
            OppgaveDokumentTekster.ytelsePreposisjonsfrase(oppgave.getYtelsetype()));
    }

    @Override
    public String malnavn() {
        return "typer/rapporter-inntekt";
    }

    @Override
    public Map<String, Object> utledInnholdsdata(BrukerdialogOppgaveEntitet oppgave) {
        InntektsrapporteringOppgavetypeDataDto dto = hentDto(oppgave);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("måned", NorskDatoFormat.måned(dto.fraOgMed()));
        data.put("gjelderDelerAvMåned", dto.gjelderDelerAvMåned());
        data.put("ytelseNavn", OppgaveDokumentTekster.ytelseNavn(oppgave.getYtelsetype()));
        OppgaveDokumentTekster.leggTilSvarfrist(data, oppgave.getFristTid());
        return data;
    }

    private InntektsrapporteringOppgavetypeDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (InntektsrapporteringOppgavetypeDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
