package no.nav.ung.brukerdialog.kontrakt.oppgaver.diagnostikk;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.ung.brukerdialog.abac.AppAbacAttributt;
import no.nav.ung.brukerdialog.abac.AppAbacAttributtType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveResponsDto;

import java.time.LocalDateTime;
import java.util.UUID;

import static no.nav.k9.felles.validering.InputValideringRegex.FRITEKST;

public record ForvaltningSettLøstDto(
    @JsonProperty("oppgaveReferanse")
    @NotNull
    @Valid
    UUID oppgaveReferanse,

    @JsonProperty("respons")
    @NotNull
    @Valid
    OppgaveResponsDto respons,

    @JsonProperty("begrunnelse")
    @NotNull
    @Size(max = 4000)
    @Pattern(regexp = FRITEKST)
    String begrunnelse
) {
    @AppAbacAttributt(value = AppAbacAttributtType.OPPGAVE_EKSTERN_REFERANSE)
    public String getOppgaveReferanseAsString() {
        return oppgaveReferanse.toString();
    }
}
