package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for arbeid og frilans registerinntekt.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ArbeidOgFrilansRegisterInntektDTO(
    @JsonProperty(value = "inntekt", required = true)
    @NotNull
    Integer inntekt,

    @JsonProperty(value = "arbeidsgiver")
    @Deprecated
    String arbeidsgiver,

    @JsonProperty(value = "arbeidsgiverIdentifikator", required = true)
    @NotNull
    String arbeidsgiverIdentifikator,

    @JsonProperty(value = "arbeidsgiverNavn")
    String arbeidsgiverNavn
) {
}

