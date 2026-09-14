package no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.InntektsrapporteringOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.RAPPORTER_INNTEKT)
public class InntektsrapporteringOppgaveDataMapperFraEntitetTilDto implements OppgaveDataMapperFraEntitetTilDto {

    protected InntektsrapporteringOppgaveDataMapperFraEntitetTilDto() {
        // CDI proxy
    }

    @Override
    public OppgavetypeDataDto tilDto(OppgaveDataEntitet entitet, String varseltekst) {
        var e = (InntektsrapporteringOppgaveDataEntitet) entitet;
        return new InntektsrapporteringOppgavetypeDataDto(
            e.getFraOgMed(),
            e.getTilOgMed(),
            e.isGjelderDelerAvMåned(),
            varseltekst
        );
    }
}

