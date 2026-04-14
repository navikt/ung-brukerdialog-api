package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static no.nav.k9.felles.validering.InputValideringRegex.FRITEKST;

/**
 * DTO for uttalelse fra bruker på et varsel.
 */
public class SvarPåVarselDto extends OppgaveResponsDto {
    @JsonProperty(value = "harUttalelse", required = true)
    @NotNull
    private Boolean harUttalelse;

    @JsonProperty(value = "uttalelseFraBruker")
    @Size(max = 4000)
    private String uttalelseFraBruker;

    public SvarPåVarselDto() {
    }

    public SvarPåVarselDto(Boolean harUttalelse, String uttalelseFraBruker) {
        this.harUttalelse = harUttalelse;
        this.uttalelseFraBruker = uttalelseFraBruker;
    }

    public Boolean getHarUttalelse() {
        return harUttalelse;
    }

    public String getUttalelseFraBruker() {
        return uttalelseFraBruker;
    }
}

