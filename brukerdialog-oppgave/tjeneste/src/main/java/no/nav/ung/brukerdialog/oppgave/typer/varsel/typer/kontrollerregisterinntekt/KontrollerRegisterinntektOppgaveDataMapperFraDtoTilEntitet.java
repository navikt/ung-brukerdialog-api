package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.kontrollerregisterinntekt;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraDtoTilEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT)
public class KontrollerRegisterinntektOppgaveDataMapperFraDtoTilEntitet implements OppgaveDataMapperFraDtoTilEntitet {

    protected KontrollerRegisterinntektOppgaveDataMapperFraDtoTilEntitet() {
        // CDI proxy
    }

    @Override
    public OppgaveDataEntitet map(OppgavetypeDataDto data) {
        var dto = (KontrollerRegisterinntektOppgavetypeDataDto) data;
        var registerinntekt = dto.registerinntekt();
        var entitet = new KontrollerRegisterinntektOppgaveDataEntitet(
            dto.fraOgMed(),
            dto.tilOgMed(),
            dto.gjelderDelerAvMåned(),
            registerinntekt.totalInntektArbeidOgFrilans(),
            registerinntekt.totalInntektYtelse(),
            registerinntekt.totalInntekt()
        );
        if (registerinntekt.arbeidOgFrilansInntekter() != null) {
            registerinntekt.arbeidOgFrilansInntekter()
                .forEach(i -> entitet.leggTilArbeidOgFrilansInntekt(i.arbeidsgiverIdentifikator(), i.arbeidsgiverNavn(), i.inntekt()));
        }
        if (registerinntekt.ytelseInntekter() != null) {
            registerinntekt.ytelseInntekter()
                .forEach(i -> entitet.leggTilYtelseInntekt(i.ytelsetype(), i.inntekt()));
        }
        return entitet;
    }
}
