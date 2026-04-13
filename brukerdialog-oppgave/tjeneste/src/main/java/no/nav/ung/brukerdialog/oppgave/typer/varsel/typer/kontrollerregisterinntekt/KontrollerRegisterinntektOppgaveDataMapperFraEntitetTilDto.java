package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.kontrollerregisterinntekt;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.ArbeidOgFrilansRegisterInntektDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.RegisterinntektDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseRegisterInntektDTO;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.util.List;

@ApplicationScoped
@OppgaveTypeRef(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT)
public class KontrollerRegisterinntektOppgaveDataMapperFraEntitetTilDto implements OppgaveDataMapperFraEntitetTilDto {

    protected KontrollerRegisterinntektOppgaveDataMapperFraEntitetTilDto() {
        // CDI proxy
    }

    @Override
    public OppgavetypeDataDto tilDto(OppgaveDataEntitet entitet) {
        var e = (KontrollerRegisterinntektOppgaveDataEntitet) entitet;

        List<ArbeidOgFrilansRegisterInntektDTO> arbeidOgFrilans = e.getArbeidOgFrilansInntekter().stream()
            .map(i -> new ArbeidOgFrilansRegisterInntektDTO(i.getInntekt(), i.getArbeidsgiverIdentifikator(), i.getArbeidsgivernavn()))
            .toList();

        List<YtelseRegisterInntektDTO> ytelse = e.getYtelseInntekter().stream()
            .map(i -> new YtelseRegisterInntektDTO(i.getInntekt(), i.getYtelsetype()))
            .toList();

        var registerinntekt = new RegisterinntektDTO(
            arbeidOgFrilans,
            ytelse,
            e.getTotalInntektArbeidFrilans(),
            e.getTotalInntektYtelse(),
            e.getTotalInntekt()
        );

        return new KontrollerRegisterinntektOppgavetypeDataDto(
            e.getFraOgMed(),
            e.getTilOgMed(),
            registerinntekt,
            e.isGjelderDelerAvMåned()
        );
    }

}

