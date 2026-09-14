package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bistand;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BekreftBistandOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BekreftBistandOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_BISTAND)
public class BekreftBistandOppgaveDataMapperFraEntitetTilDto implements OppgaveDataMapperFraEntitetTilDto {

    protected BekreftBistandOppgaveDataMapperFraEntitetTilDto() {
        // CDI proxy
    }

    @Override
    public OppgavetypeDataDto tilDto(OppgaveDataEntitet entitet) {
        var e = (BekreftBistandOppgaveDataEntitet) entitet;
        if (e.getTom() == null) {
            return new BekreftBistandOpphørOppgavetypeDataDto(e.getFom(), e.getIkkeOppfyltÅrsak(), e.getIkkeOppfyltÅrsakFritekstbeskrivelse(), e.getKilde(), e.getKildeFritekst());
        }
        return new BekreftBistandOppgavetypeDataDto(e.getFom(), e.getTom(), e.getIkkeOppfyltÅrsak(), e.getIkkeOppfyltÅrsakFritekstbeskrivelse(), e.getKilde(), e.getKildeFritekst());
    }
}
