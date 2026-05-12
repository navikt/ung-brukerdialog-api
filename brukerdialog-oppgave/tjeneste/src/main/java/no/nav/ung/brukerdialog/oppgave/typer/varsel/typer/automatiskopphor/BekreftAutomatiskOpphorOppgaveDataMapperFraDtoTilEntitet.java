package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.automatiskopphor;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.automatiskopphor.BekreftAutomatiskOpphorOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraDtoTilEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_AUTOMATISK_OPPHOR)
public class BekreftAutomatiskOpphorOppgaveDataMapperFraDtoTilEntitet implements OppgaveDataMapperFraDtoTilEntitet {

    protected BekreftAutomatiskOpphorOppgaveDataMapperFraDtoTilEntitet() {
        // CDI proxy
    }

    @Override
    public OppgaveDataEntitet map(OppgavetypeDataDto data) {
        var dto = (BekreftAutomatiskOpphorOppgavetypeDataDto) data;
        return new BekreftAutomatiskOpphorOppgaveDataEntitet(dto.sluttdato(), dto.maxDato());
    }
}
