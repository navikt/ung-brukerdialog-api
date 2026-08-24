package no.nav.ung.brukerdialog.oppgave.journalforing.typer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretsluttdato.EndretSluttdatoDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentTekster;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentUtleder;

import java.util.Map;

/**
 * Brevteksten er tilpasset fra
 * {@code sif-brukerdialog/packages/ung-innsyn/.../oppgavepaneler/endret-sluttdato/i18n/nb.ts} og
 * {@code .../meldt-ut/i18n/nb.ts}. {@link EndretSluttdatoDataDto#forrigeSluttdato()} er nullable:
 * {@code null} betyr at dette er første gang en sluttdato settes (bruker meldes
 * ut), ikke en endring av en eksisterende - {@link OppgaveDokumentTekster#erMeldtUt} skiller de to.
 * <p>
 * Tittel og innholdsdata bygges av {@link OppgaveDokumentTekster}, delt med
 * {@link EndretPeriodeOppgaveDokumentUtleder} (gren {@code ENDRET_SLUTTDATO}) slik at teksten
 * ikke kan drifte i to retninger.
 */
@OppgaveTypeRef(OppgaveType.BEKREFT_ENDRET_SLUTTDATO)
@ApplicationScoped
public class EndretSluttdatoOppgaveDokumentUtleder implements OppgaveDokumentUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;

    EndretSluttdatoOppgaveDokumentUtleder() {
        // for CDI proxy
    }

    @Inject
    public EndretSluttdatoOppgaveDokumentUtleder(@Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere) {
        this.mappere = mappere;
    }

    @Override
    public String utledTittel(BrukerdialogOppgaveEntitet oppgave) {
        EndretSluttdatoDataDto dto = hentDto(oppgave);
        boolean erMeldtUt = OppgaveDokumentTekster.erMeldtUt(dto.forrigeSluttdato());
        return OppgaveDokumentTekster.endretSluttdatoTittel(oppgave.getYtelsetype(), erMeldtUt);
    }

    @Override
    public String malnavn() {
        return "typer/endret-sluttdato";
    }

    @Override
    public Map<String, Object> utledInnholdsdata(BrukerdialogOppgaveEntitet oppgave) {
        EndretSluttdatoDataDto dto = hentDto(oppgave);
        return OppgaveDokumentTekster.endretSluttdatoInnhold(
            dto.nySluttdato(), dto.forrigeSluttdato(), oppgave.getYtelsetype(), oppgave.getFristTid());
    }

    private EndretSluttdatoDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (EndretSluttdatoDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
