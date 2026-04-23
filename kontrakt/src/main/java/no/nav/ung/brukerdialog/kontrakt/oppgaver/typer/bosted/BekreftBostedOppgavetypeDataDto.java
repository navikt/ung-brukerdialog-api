package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;

import java.time.LocalDate;

/**
 * Data for oppgave om bostedavklaring – bruker bekrefter om de er bosatt i Trondheim.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BekreftBostedOppgavetypeDataDto(
    @NotNull
    LocalDate fom,

    @NotNull
    LocalDate tom,

    @NotNull
    Boolean erBosattITrondheim
) implements OppgavetypeDataDto {
    @Override
    public OppgaveType oppgavetype() {
        return OppgaveType.BEKREFT_BOSTED;
    }
}
