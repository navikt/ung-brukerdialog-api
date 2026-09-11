package no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.InntektsrapporteringOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.OppgaveTekster;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.pdf.NorskDatoFormat;
import no.nav.ung.brukerdialog.pdf.OppgaveTekstfragmentRenderer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@OppgaveTypeRef(OppgaveType.RAPPORTER_INNTEKT)
@ApplicationScoped
public class InntektsrapporteringOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;
    private String aktivitetspengerInnsynBaseUrl;
    private OppgaveTekstfragmentRenderer renderer;

    InntektsrapporteringOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public InntektsrapporteringOppgaveInnholdUtleder(
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
        InntektsrapporteringOppgavetypeDataDto dto = hentDto(oppgave);
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
        InntektsrapporteringOppgavetypeDataDto dto = hentDto(oppgave);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("månedNavn", NorskDatoFormat.måned(dto.fraOgMed()));
        data.put("ytelsetype", oppgave.getYtelsetype().name());
        data.put("gjelderDelerAvMåned", dto.gjelderDelerAvMåned());
        data.put("fristDato", OppgaveTekster.fristDato(oppgave.getFristTid()));

        return renderer.rendre("tekstfragmenter/inntektsrapportering/inntektsrapportering", data);
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return switch (oppgave.getYtelsetype()) {
            case AKTIVITETSPENGER -> aktivitetspengerInnsynBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
            case UNGDOMSYTELSE -> ungdomsprogramytelsenDeltakerBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
        };
    }

    private InntektsrapporteringOppgavetypeDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (InntektsrapporteringOppgavetypeDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
