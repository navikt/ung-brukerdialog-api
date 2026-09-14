package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.aktivitet;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.AktivitetsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.AktivitetsvilkåretIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.BekreftAktivitetOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.BekreftAktivitetOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraDtoTilEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.time.LocalDate;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_AKTIVITET)
public class BekreftAktivitetOppgaveDataMapperFraDtoTilEntitet implements OppgaveDataMapperFraDtoTilEntitet {

    protected BekreftAktivitetOppgaveDataMapperFraDtoTilEntitet() {
        // CDI proxy
    }

    @Override
    public OppgaveDataEntitet map(OppgavetypeDataDto data) {
        return switch (data) {
            case BekreftAktivitetOppgavetypeDataDto(
                LocalDate fom,
                LocalDate tom,
                AktivitetsvilkåretIkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                String ikkeOppfyltÅrsakFritekstbeskrivelse,
                AktivitetsavklaringKildeType kilde,
                String kildeFritekst
            ) -> new BekreftAktivitetOppgaveDataEntitet(fom, tom, ikkeOppfyltÅrsak, ikkeOppfyltÅrsakFritekstbeskrivelse, kilde, kildeFritekst);
            case BekreftAktivitetOpphørOppgavetypeDataDto(
                LocalDate fom,
                AktivitetsvilkåretIkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                String ikkeOppfyltÅrsakFritekstbeskrivelse,
                AktivitetsavklaringKildeType kilde,
                String kildeFritekst
            ) -> new BekreftAktivitetOppgaveDataEntitet(fom, null, ikkeOppfyltÅrsak, ikkeOppfyltÅrsakFritekstbeskrivelse, kilde, kildeFritekst);
            default -> throw new IllegalArgumentException("Ugyldig data type: " + data.getClass());
        };
    }
}
