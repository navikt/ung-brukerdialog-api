package no.nav.ung.brukerdialog.oppgave.diagnostikk;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.SvarPåVarselDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.diagnostikk.DiagnostikkBrukerdialogOppgaveDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.diagnostikk.DiagnostikkOppgaveResponsDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.diagnostikk.DiagnostikkRapportertInntektDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.diagnostikk.DiagnostikkSvarPåVarselDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.RapportertInntektDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@ApplicationScoped
public class DiagnostikkBrukerdialogOppgaveMapper {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;

    public DiagnostikkBrukerdialogOppgaveMapper() {
        // CDI proxy
    }

    @Inject
    public DiagnostikkBrukerdialogOppgaveMapper(@Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere) {
        this.mappere = mappere;
    }

    public DiagnostikkBrukerdialogOppgaveDto tilDto(BrukerdialogOppgaveEntitet oppgave) {
        var oppgavetypeData = OppgaveDataMapperFraEntitetTilDto.finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());

        return new DiagnostikkBrukerdialogOppgaveDto(
            oppgave.getOppgavereferanse(),
            oppgave.getOppgaveType(),
            oppgavetypeData,
            oppgave.getYtelsetype(),
            mapRespons(oppgave),
            oppgave.getStatus(),
            toZonedDateTime(oppgave.getOpprettetTidspunkt()),
            toZonedDateTime(oppgave.getLøstDato()),
            toZonedDateTime(oppgave.getFristTid())
        );
    }

    private static DiagnostikkOppgaveResponsDto mapRespons(BrukerdialogOppgaveEntitet oppgave) {
        if (oppgave.getRespons() == null) {
            return null;
        }
        return switch (oppgave.getRespons()) {
            case SvarPåVarselDto dto ->
                new DiagnostikkSvarPåVarselDto(dto.getHarUttalelse(), dto.getUttalelseFraBruker());
            case RapportertInntektDto dto ->
                new DiagnostikkRapportertInntektDto(dto.getFraOgMed(), dto.getTilOgMed(), dto.getArbeidstakerOgFrilansInntekt());
            default -> throw new IllegalStateException("Fikk uventet respons-type: " + oppgave.getRespons().getClass().getName());
        };

    }


    private ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault());
    }
}

