package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bistand;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BekreftBistandOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BekreftBistandOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BistandsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BistandsvilkårIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraDtoTilEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.time.LocalDate;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_BISTAND)
public class BekreftBistandOppgaveDataMapperFraDtoTilEntitet implements OppgaveDataMapperFraDtoTilEntitet {

    protected BekreftBistandOppgaveDataMapperFraDtoTilEntitet() {
        // CDI proxy
    }

    @Override
    public OppgaveDataEntitet map(OppgavetypeDataDto data) {
        return switch (data) {
            case BekreftBistandOppgavetypeDataDto(
                LocalDate fom,
                LocalDate tom,
                BistandsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                String ikkeOppfyltÅrsakFritekstbeskrivelse,
                BistandsavklaringKildeType kilde,
                String kildeFritekst
            ) -> new BekreftBistandOppgaveDataEntitet(fom, tom, ikkeOppfyltÅrsak, ikkeOppfyltÅrsakFritekstbeskrivelse, kilde, kildeFritekst);
            case BekreftBistandOpphørOppgavetypeDataDto(
                LocalDate fom,
                BistandsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                String ikkeOppfyltÅrsakFritekstbeskrivelse,
                BistandsavklaringKildeType kilde,
                String kildeFritekst
            ) -> new BekreftBistandOppgaveDataEntitet(fom, null, ikkeOppfyltÅrsak, ikkeOppfyltÅrsakFritekstbeskrivelse, kilde, kildeFritekst);
            default -> throw new IllegalArgumentException("Ugyldig data type: " + data.getClass());
        };
    }
}
