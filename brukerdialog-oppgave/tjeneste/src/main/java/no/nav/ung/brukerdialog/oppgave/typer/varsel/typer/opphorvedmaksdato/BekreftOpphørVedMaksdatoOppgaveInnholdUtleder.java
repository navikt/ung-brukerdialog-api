package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.opphorvedmaksdato;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.opphorvedmaksdato.BekreftOpphorVedMaksdatoOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.OppgaveTekster;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.pdf.OppgaveTekstfragmentRenderer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@OppgaveTypeRef(OppgaveType.BEKREFT_OPPHOR_VED_MAKSDATO)
@ApplicationScoped
public class BekreftOpphørVedMaksdatoOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;
    private OppgaveTekstfragmentRenderer renderer;

    BekreftOpphørVedMaksdatoOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public BekreftOpphørVedMaksdatoOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl,
        OppgaveTekstfragmentRenderer renderer
    ) {
        this.mappere = mappere;
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
        this.renderer = renderer;
    }

    @Override
    public String undertittel(BrukerdialogOppgaveEntitet oppgave) {
        return "Sluttdato";
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
        BekreftOpphorVedMaksdatoOppgavetypeDataDto dto = hentDto(oppgave);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ytelsetype", oppgave.getYtelsetype().name());
        data.put("sluttdato", dto.sluttdato().toString());
        data.put("fristDato", OppgaveTekster.fristDato(oppgave.getFristTid()));

        return renderer.rendre("tekstfragmenter/opphor_ved_maksdato/opphor_ved_maksdato", data);
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return ungdomsprogramytelsenDeltakerBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
    }

    private BekreftOpphorVedMaksdatoOppgavetypeDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (BekreftOpphorVedMaksdatoOppgavetypeDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
