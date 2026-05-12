package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.automatiskopphor;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.automatiskopphor.BekreftAutomatiskOpphorOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_AUTOMATISK_OPPHOR)
public class BekreftAutomatiskOpphorOppgaveDataMapperFraEntitetTilDto implements OppgaveDataMapperFraEntitetTilDto {

    protected BekreftAutomatiskOpphorOppgaveDataMapperFraEntitetTilDto() {
        // CDI proxy
    }

    @Override
    public OppgavetypeDataDto tilDto(OppgaveDataEntitet entitet) {
        var e = (BekreftAutomatiskOpphorOppgaveDataEntitet) entitet;
        return new BekreftAutomatiskOpphorOppgavetypeDataDto(e.getSluttdato(), e.getMaxDato());
    }
}
