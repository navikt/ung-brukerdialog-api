package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.endretsluttdato;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretsluttdato.EndretSluttdatoDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.OppgaveTekster;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.pdf.OppgaveTekstfragmentRenderer;

import java.util.List;

@OppgaveTypeRef(OppgaveType.BEKREFT_ENDRET_SLUTTDATO)
@ApplicationScoped
public class EndretSluttdatoOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;
    private OppgaveTekstfragmentRenderer renderer;

    EndretSluttdatoOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public EndretSluttdatoOppgaveInnholdUtleder(
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
        EndretSluttdatoDataDto dto = hentDto(oppgave);
        boolean erMeldtUt = OppgaveTekster.erMeldtUt(dto.forrigeSluttdato());
        return OppgaveTekster.endretSluttdatoTittel(erMeldtUt);
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
        EndretSluttdatoDataDto dto = hentDto(oppgave);
        return OppgaveTekster.endretSluttdatoInnhold(
            renderer, dto.nySluttdato(), dto.forrigeSluttdato(), oppgave.getYtelsetype(), oppgave.getFristTid());
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return ungdomsprogramytelsenDeltakerBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
    }

    private EndretSluttdatoDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (EndretSluttdatoDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
