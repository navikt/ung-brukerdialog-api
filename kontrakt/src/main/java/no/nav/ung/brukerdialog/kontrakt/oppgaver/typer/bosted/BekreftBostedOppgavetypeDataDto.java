package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;

import java.time.LocalDate;

/**
 * Data for oppgave om bostedavklaring – bruker bekrefter om de er bosatt i Trondheim.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BekreftBostedOppgavetypeDataDto(
    @JsonProperty(value = "fraOgMed", required = true)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fraOgMed,

    @JsonProperty(value = "tilOgMed", required = true)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate tilOgMed,

    @JsonProperty(value = "erBosattITrondheim", required = true)
    @NotNull
    Boolean erBosattITrondheim
) implements OppgavetypeDataDto {
    @Override
    public OppgaveType oppgavetype() {
        return OppgaveType.BEKREFT_BOSTED;
    }
}
