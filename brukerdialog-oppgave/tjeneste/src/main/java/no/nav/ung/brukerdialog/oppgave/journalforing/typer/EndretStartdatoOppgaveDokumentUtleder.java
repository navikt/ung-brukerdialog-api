package no.nav.ung.brukerdialog.oppgave.journalforing.typer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretstartdato.EndretStartdatoDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentTekster;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentUtleder;

import java.util.Map;

/**
 * Kilde: {@code sif-brukerdialog/.../oppgavepaneler/endret-startdato/i18n/nb.ts} (samme tekst
 * som på Min Side / ungdomsprogram-deltaker).
 * <p>
 * Delt med {@link EndretPeriodeOppgaveDokumentUtleder} (gren {@code ENDRET_STARTDATO}) via
 * {@link OppgaveDokumentTekster}, slik at teksten ikke kan drifte i to retninger.
 */
@OppgaveTypeRef(OppgaveType.BEKREFT_ENDRET_STARTDATO)
@ApplicationScoped
public class EndretStartdatoOppgaveDokumentUtleder implements OppgaveDokumentUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;

    EndretStartdatoOppgaveDokumentUtleder() {
        // for CDI proxy
    }

    @Inject
    public EndretStartdatoOppgaveDokumentUtleder(@Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere) {
        this.mappere = mappere;
    }

    @Override
    public String utledTittel(BrukerdialogOppgaveEntitet oppgave) {
        return OppgaveDokumentTekster.endretStartdatoTittel(oppgave.getYtelsetype());
    }

    @Override
    public String malnavn() {
        return "typer/endret-startdato";
    }

    @Override
    public Map<String, Object> utledInnholdsdata(BrukerdialogOppgaveEntitet oppgave) {
        EndretStartdatoDataDto dto = (EndretStartdatoDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
        return OppgaveDokumentTekster.endretStartdatoInnhold(
            dto.nyStartdato(), dto.forrigeStartdato(), oppgave.getYtelsetype(), oppgave.getFristTid());
    }
}
