package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.ung.brukerdialog.abac.AppAbacAttributt;
import no.nav.ung.brukerdialog.abac.AppAbacAttributtType;
import no.nav.ung.brukerdialog.abac.StandardAbacAttributt;

import java.util.UUID;

public record OppgaveEksternReferanseDto(
    @JsonProperty("oppgaveReferanse")
    @NotNull
    @Valid
    UUID oppgaveReferanse
) {
    @AppAbacAttributt(value = AppAbacAttributtType.OPPGAVE_EKSTERN_REFERANSE)
    public String getOppgaveReferanseAsString() {
        return oppgaveReferanse.toString();
    }

}
