package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.endretperiode;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.EndretPeriodeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.PeriodeDTO;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_ENDRET_PERIODE)
public class EndretPeriodeOppgaveDataMapperFraEntitetTilDto implements OppgaveDataMapperFraEntitetTilDto {

    protected EndretPeriodeOppgaveDataMapperFraEntitetTilDto() {
        // CDI proxy
    }

    @Override
    public OppgavetypeDataDto tilDto(OppgaveDataEntitet entitet, String varseltekst) {
        var e = (EndretPeriodeOppgaveDataEntitet) entitet;

        PeriodeDTO nyPeriode = e.getNyPeriodeFom() != null
            ? new PeriodeDTO(e.getNyPeriodeFom(), e.getNyPeriodeTom())
            : null;

        PeriodeDTO forrigePeriode = e.getForrigePeriodeFom() != null
            ? new PeriodeDTO(e.getForrigePeriodeFom(), e.getForrigePeriodeTom())
            : null;

        return new EndretPeriodeDataDto(nyPeriode, forrigePeriode, e.getEndringer(), varseltekst);
    }
}

