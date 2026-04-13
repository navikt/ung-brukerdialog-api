package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;

import java.time.LocalDate;

/**
 * Data for oppgave om å kontrollere registerinntekt.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KontrollerRegisterinntektOppgavetypeDataDto(
    @JsonProperty(value = "fraOgMed", required = true)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fraOgMed,

    @JsonProperty(value = "tilOgMed", required = true)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate tilOgMed,

    @JsonProperty(value = "registerinntekt", required = true)
    @Valid
    @NotNull
    RegisterinntektDTO registerinntekt,

    @JsonProperty(value = "gjelderDelerAvMåned", required = true)
    @NotNull
    Boolean gjelderDelerAvMåned
) implements OppgavetypeDataDto {
    @Override
    public OppgaveType oppgavetype() {
        return OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT;
    }
}
