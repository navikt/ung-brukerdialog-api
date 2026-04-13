package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

public record LøsOppgaveRequest(
    @JsonProperty("oppgaveRespons")
    @Valid
    OppgaveResponsDto oppgaveRespons
) {
}
