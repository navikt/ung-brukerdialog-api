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

import java.util.List;

/**
 * Kilde: {@code sif-brukerdialog/.../oppgavepaneler/endret-sluttdato/i18n/nb.ts} og
 * {@code .../meldt-ut/i18n/nb.ts}. {@code forrigeSluttdato() == null} betyr «meldt ut» (første
 * sluttdato, ikke en endring) - se {@link OppgaveTekster#erMeldtUt}.
 * <p>
 * Delt med {@code EndretPeriodeOppgaveInnholdUtleder} (gren {@code SLUTTDATO}) via
 * {@link OppgaveTekster}, slik at teksten ikke kan drifte i to retninger.
 */
@OppgaveTypeRef(OppgaveType.BEKREFT_ENDRET_SLUTTDATO)
@ApplicationScoped
public class EndretSluttdatoOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;

    EndretSluttdatoOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public EndretSluttdatoOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl
    ) {
        this.mappere = mappere;
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
    }

    @Override
    public String tittel(BrukerdialogOppgaveEntitet oppgave) {
        EndretSluttdatoDataDto dto = hentDto(oppgave);
        boolean erMeldtUt = OppgaveTekster.erMeldtUt(dto.forrigeSluttdato());
        return OppgaveTekster.endretSluttdatoTittel(oppgave.getYtelsetype(), erMeldtUt);
    }

    @Override
    public List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave) {
        EndretSluttdatoDataDto dto = hentDto(oppgave);
        return OppgaveTekster.endretSluttdatoInnhold(
            dto.nySluttdato(), dto.forrigeSluttdato(), oppgave.getYtelsetype(), oppgave.getFristTid());
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
