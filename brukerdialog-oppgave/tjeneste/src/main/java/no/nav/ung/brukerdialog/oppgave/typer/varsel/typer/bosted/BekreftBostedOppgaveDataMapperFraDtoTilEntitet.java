package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bosted;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraDtoTilEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.time.LocalDate;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_BOSTED)
public class BekreftBostedOppgaveDataMapperFraDtoTilEntitet implements OppgaveDataMapperFraDtoTilEntitet {

    protected BekreftBostedOppgaveDataMapperFraDtoTilEntitet() {
        // CDI proxy
    }

    @Override
    public OppgaveDataEntitet map(OppgavetypeDataDto data) {
        return switch (data) {
            case BekreftBostedOppgavetypeDataDto(
                LocalDate fom,
                LocalDate tom,
                Boolean erBosattITrondheim,
                String ikkeOppfyltÅrsakFritekstbeskrivelse,
                BostedsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                BostedsavklaringKildeType kilde,
                String kildeFritekst
            ) -> new BekreftBostedOppgaveDataEntitet(fom, tom, erBosattITrondheim, ikkeOppfyltÅrsakFritekstbeskrivelse, ikkeOppfyltÅrsak, kilde, kildeFritekst);
            case BekreftBostedOpphørOppgavetypeDataDto(
                LocalDate fom,
                Boolean erBosattITrondheim,
                String ikkeOppfyltÅrsakFritekstbeskrivelse,
                BostedsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                BostedsavklaringKildeType kilde,
                String kildeFritekst
            ) -> new BekreftBostedOppgaveDataEntitet(fom, null, erBosattITrondheim, ikkeOppfyltÅrsakFritekstbeskrivelse, ikkeOppfyltÅrsak, kilde, kildeFritekst);
            default -> throw new IllegalArgumentException("Ugyldig data type: " + data.getClass());
        };
    }
}
