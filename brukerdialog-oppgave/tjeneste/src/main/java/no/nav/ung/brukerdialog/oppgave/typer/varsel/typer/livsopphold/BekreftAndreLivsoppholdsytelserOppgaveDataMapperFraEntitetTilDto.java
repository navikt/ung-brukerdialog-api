package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.livsopphold;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER)
public class BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraEntitetTilDto implements OppgaveDataMapperFraEntitetTilDto {

    protected BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraEntitetTilDto() {
        // CDI proxy
    }

    @Override
    public OppgavetypeDataDto tilDto(OppgaveDataEntitet entitet, String varseltekst) {
        var e = (BekreftAndreLivsoppholdsytelserOppgaveDataEntitet) entitet;
        if (e.getTom() == null) {
            return new BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto(e.getFom(), e.getIkkeOppfyltÅrsak(), e.getIkkeOppfyltÅrsakFritekstbeskrivelse(), e.getKilde(), e.getKildeFritekst(), varseltekst);
        }
        return new BekreftAndreLivsoppholdsytelserOppgavetypeDataDto(e.getFom(), e.getTom(), e.getIkkeOppfyltÅrsak(), e.getIkkeOppfyltÅrsakFritekstbeskrivelse(), e.getKilde(), e.getKildeFritekst(), varseltekst);
    }
}
