package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;

import java.util.Set;

/**
 * Data for oppgave om endret periode.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EndretPeriodeDataDto(
    @JsonProperty(value = "nyPeriode")
    @Valid
    PeriodeDTO nyPeriode,

    @JsonProperty(value = "forrigePeriode")
    @Valid
    PeriodeDTO forrigePeriode,

    @JsonProperty(value = "endringer", required = true)
    @NotNull
    @Size(max = 4)
    Set<PeriodeEndringType> endringer
) implements OppgavetypeDataDto {
    @Override
    public OppgaveType oppgavetype() {
        return OppgaveType.BEKREFT_ENDRET_PERIODE;
    }
}
