package no.nav.ung.brukerdialog.kontrakt.oppgaver.diagnostikk;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.ung.brukerdialog.abac.AppAbacAttributt;
import no.nav.ung.brukerdialog.abac.AppAbacAttributtType;

import java.util.UUID;

public record DiagnostikkOppgaveRequestDto(
    @JsonProperty("oppgaveReferanse")
    @NotNull
    @Valid
    UUID oppgaveReferanse,

    @JsonProperty("begrunnelse")
    @NotNull
    @Size(max = 4000)
    String begrunnelse
) {
    @AppAbacAttributt(value = AppAbacAttributtType.OPPGAVE_EKSTERN_REFERANSE)
    public String getOppgaveReferanseAsString() {
        return oppgaveReferanse.toString();
    }
}
