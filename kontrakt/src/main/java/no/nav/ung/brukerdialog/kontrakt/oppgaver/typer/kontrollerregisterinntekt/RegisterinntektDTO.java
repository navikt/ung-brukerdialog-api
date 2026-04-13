package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for registerinntekt med totalsummer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterinntektDTO(
    @JsonProperty(value = "arbeidOgFrilansInntekter")
    @Size(max = 100)
    List<@Valid ArbeidOgFrilansRegisterInntektDTO> arbeidOgFrilansInntekter,

    @JsonProperty(value = "ytelseInntekter")
    @Size(max = 100)
    List<@Valid YtelseRegisterInntektDTO> ytelseInntekter,

    @JsonProperty(value = "totalInntektArbeidOgFrilans", required = true)
    @NotNull
    Integer totalInntektArbeidOgFrilans,

    @JsonProperty(value = "totalInntektYtelse", required = true)
    @NotNull
    Integer totalInntektYtelse,

    @JsonProperty(value = "totalInntekt", required = true)
    @NotNull
    Integer totalInntekt
) {
    /**
     * Constructor som beregner totalsummer automatisk.
     */
    public RegisterinntektDTO(
        List<ArbeidOgFrilansRegisterInntektDTO> arbeidOgFrilansInntekter,
        List<YtelseRegisterInntektDTO> ytelseInntekter
    ) {
        this(
            arbeidOgFrilansInntekter,
            ytelseInntekter,
            arbeidOgFrilansInntekter.stream().mapToInt(ArbeidOgFrilansRegisterInntektDTO::inntekt).sum(),
            ytelseInntekter.stream().mapToInt(YtelseRegisterInntektDTO::inntekt).sum(),
            arbeidOgFrilansInntekter.stream().mapToInt(ArbeidOgFrilansRegisterInntektDTO::inntekt).sum() +
                ytelseInntekter.stream().mapToInt(YtelseRegisterInntektDTO::inntekt).sum()
        );
    }
}

