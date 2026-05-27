package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.opphorvedmaksdato;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.opphorvedmaksdato.BekreftOpphorVedMaksdatoOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraDtoTilEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_OPPHOR_VED_MAKSDATO)
public class BekreftOpphorVedMaksdatoOppgaveDataMapperFraDtoTilEntitet implements OppgaveDataMapperFraDtoTilEntitet {

    protected BekreftOpphorVedMaksdatoOppgaveDataMapperFraDtoTilEntitet() {
        // CDI proxy
    }

    @Override
    public OppgaveDataEntitet map(OppgavetypeDataDto data) {
        var dto = (BekreftOpphorVedMaksdatoOppgavetypeDataDto) data;
        return new BekreftOpphorVedMaksdatoOppgaveDataEntitet(dto.sluttdato(), dto.maxDato());
    }
}
