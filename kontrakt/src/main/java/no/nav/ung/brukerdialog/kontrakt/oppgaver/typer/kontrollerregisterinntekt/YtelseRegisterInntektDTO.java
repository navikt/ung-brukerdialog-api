package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for ytelse registerinntekt.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record YtelseRegisterInntektDTO(
    @JsonProperty(value = "inntekt", required = true)
    @NotNull
    Integer inntekt,

    @JsonProperty(value = "ytelsetype", required = true)
    @NotNull
    YtelseType ytelsetype
) {
}

