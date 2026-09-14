package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.endretsluttdato;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretsluttdato.EndretSluttdatoDataDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_ENDRET_SLUTTDATO)
public class EndretSluttdatoOppgaveDataMapperFraEntitetTilDto implements OppgaveDataMapperFraEntitetTilDto {

    protected EndretSluttdatoOppgaveDataMapperFraEntitetTilDto() {
        // CDI proxy
    }

    @Override
    public OppgavetypeDataDto tilDto(OppgaveDataEntitet entitet, String varseltekst) {
        var e = (EndretSluttdatoOppgaveDataEntitet) entitet;
        return new EndretSluttdatoDataDto(e.getNySluttdato(), e.getForrigeSluttdato(), varseltekst);
    }
}

