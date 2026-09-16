package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.livsopphold;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserAvklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraDtoTilEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.time.LocalDate;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER)
public class BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraDtoTilEntitet implements OppgaveDataMapperFraDtoTilEntitet {

    protected BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraDtoTilEntitet() {
        // CDI proxy
    }

    @Override
    public OppgaveDataEntitet map(OppgavetypeDataDto data) {
        return switch (data) {
            case BekreftAndreLivsoppholdsytelserOppgavetypeDataDto(
                LocalDate fom,
                LocalDate tom,
                AndreLivsoppholdsytelserIkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                String ikkeOppfyltÅrsakFritekstbeskrivelse,
                AndreLivsoppholdsytelserAvklaringKildeType kilde,
                String kildeFritekst,
                String _
            ) -> new BekreftAndreLivsoppholdsytelserOppgaveDataEntitet(fom, tom, ikkeOppfyltÅrsak, ikkeOppfyltÅrsakFritekstbeskrivelse, kilde, kildeFritekst);
            case BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto(
                LocalDate fom,
                AndreLivsoppholdsytelserIkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                String ikkeOppfyltÅrsakFritekstbeskrivelse,
                AndreLivsoppholdsytelserAvklaringKildeType kilde,
                String kildeFritekst,
                String _
            ) -> new BekreftAndreLivsoppholdsytelserOppgaveDataEntitet(fom, null, ikkeOppfyltÅrsak, ikkeOppfyltÅrsakFritekstbeskrivelse, kilde, kildeFritekst);
            default -> throw new IllegalArgumentException("Ugyldig data type: " + data.getClass());
        };
    }
}
