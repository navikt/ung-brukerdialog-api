package no.nav.ung.brukerdialog.kontrakt.oppgaver.diagnostikk;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveResponsDto;

/**
 * DTO for uttalelse fra bruker på et varsel.
 */
public class DiagnostikkSvarPåVarselDto extends DiagnostikkOppgaveResponsDto {
    @JsonProperty(value = "harUttalelse", required = true)
    @NotNull
    private Boolean harUttalelse;

    @JsonProperty(value = "uttalelseFraBruker")
    @Size(max = 4000)
    private String uttalelseFraBruker;

    public DiagnostikkSvarPåVarselDto() {
    }

    public DiagnostikkSvarPåVarselDto(Boolean harUttalelse, String uttalelseFraBruker) {
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

