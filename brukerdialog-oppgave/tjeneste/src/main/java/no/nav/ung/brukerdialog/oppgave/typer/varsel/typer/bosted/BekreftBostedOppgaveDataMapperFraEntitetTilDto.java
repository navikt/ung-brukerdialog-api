package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bosted;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_BOSTED)
public class BekreftBostedOppgaveDataMapperFraEntitetTilDto implements OppgaveDataMapperFraEntitetTilDto {

    protected BekreftBostedOppgaveDataMapperFraEntitetTilDto() {
        // CDI proxy
    }

    @Override
    public OppgavetypeDataDto tilDto(OppgaveDataEntitet entitet) {
        var e = (BekreftBostedOppgaveDataEntitet) entitet;
        return new BekreftBostedOppgavetypeDataDto(e.getFraOgMed(), e.getTilOgMed(), e.getErBosattITrondheim());
    }
}
