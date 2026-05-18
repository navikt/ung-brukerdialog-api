package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.ung.brukerdialog.abac.StandardAbacAttributt;
import no.nav.ung.brukerdialog.typer.AktørId;

import java.time.LocalDate;

/**
 * DTO for å sette en oppgave av en gitt type til utløpt eller avbrutt.
 * For periode-baserte oppgavetyper (f.eks. RAPPORTER_INNTEKT) er fomDato og tomDato påkrevd.
 * For ikke-periode-baserte oppgavetyper (f.eks. SØK_YTELSE) kan fomDato og tomDato utelates.
 * Se {@link OppgaveType#kreverPeriode()} og {@link GyldigEndreOppgaveStatus}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@GyldigEndreOppgaveStatus
public record EndreOppgaveStatusDto(

    @JsonProperty(value = "aktørId", required = true)
    @NotNull
    @Valid
    AktørId aktørId,

    @JsonProperty(value = "oppgavetype", required = true)
    @NotNull
    OppgaveType oppgavetype,

    @JsonProperty(value = "fomDato")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fomDato,

    @JsonProperty(value = "tomDato")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate tomDato
) {

    @StandardAbacAttributt(value = StandardAbacAttributtType.AKTØR_ID)
    public String getAktørIdAsString() {
        return aktørId.getId();
    }


}

