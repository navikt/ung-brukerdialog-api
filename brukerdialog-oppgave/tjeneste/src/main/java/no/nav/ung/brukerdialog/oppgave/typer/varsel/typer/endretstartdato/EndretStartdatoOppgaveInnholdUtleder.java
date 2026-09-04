package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.endretstartdato;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretstartdato.EndretStartdatoDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.OppgaveTekster;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;

import java.util.List;

/**
 * Kilde: {@code sif-brukerdialog/.../oppgavepaneler/endret-startdato/i18n/nb.ts} (samme tekst
 * som på Min Side / ungdomsprogram-deltaker).
 * <p>
 * Delt med {@code EndretPeriodeOppgaveInnholdUtleder} (gren {@code STARTDATO}) via
 * {@link OppgaveTekster}, slik at teksten ikke kan drifte i to retninger.
 */
@OppgaveTypeRef(OppgaveType.BEKREFT_ENDRET_STARTDATO)
@ApplicationScoped
public class EndretStartdatoOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;

    EndretStartdatoOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public EndretStartdatoOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl
    ) {
        this.mappere = mappere;
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
    }

    @Override
    public String tittel(BrukerdialogOppgaveEntitet oppgave) {
        return OppgaveTekster.endretStartdatoTittel(oppgave.getYtelsetype());
    }

    @Override
    public List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave) {
        EndretStartdatoDataDto dto = hentDto(oppgave);
        return OppgaveTekster.endretStartdatoInnhold(
            dto.nyStartdato(), dto.forrigeStartdato(), oppgave.getYtelsetype(), oppgave.getFristTid());
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return ungdomsprogramytelsenDeltakerBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
    }

    private EndretStartdatoDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (EndretStartdatoDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
