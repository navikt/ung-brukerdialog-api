package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.opphorvedmaksdato;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;

import java.time.LocalDate;

/**
 * Data for oppgave om automatisk opphør av ungdomsprogramytelsen.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BekreftOpphorVedMaksdatoOppgavetypeDataDto(
    @JsonProperty(value = "sluttdato", required = true)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate sluttdato,

    @JsonProperty(value = "maxDato", required = true)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate maxDato
) implements OppgavetypeDataDto {
    @Override
    public OppgaveType oppgavetype() {
        return OppgaveType.BEKREFT_OPPHOR_VED_MAKSDATO;
    }
}
