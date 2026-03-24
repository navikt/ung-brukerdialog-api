package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.ung.brukerdialog.abac.StandardAbacAttributt;
import no.nav.ung.brukerdialog.typer.AktørId;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO for migrering av enkelt oppgave.
 */
public record MigrerOppgaveDto(
    @JsonProperty(value = "oppgaveReferanse", required = true)
    @NotNull
    @Valid
    UUID oppgaveReferanse,

    @JsonProperty(value = "aktørId", required = true)
    @NotNull
    @Valid
    AktørId aktørId,

    @JsonProperty(value = "oppgavetype", required = true)
    @NotNull
    OppgaveType oppgavetype,

    @JsonProperty(value = "oppgavetypeData", required = true)
    @NotNull
    @Valid
    OppgavetypeDataDto oppgavetypeData,

    @JsonProperty(value = "respons")
    @Valid
    OppgaveResponsDto respons,

    @JsonProperty(value = "status", required = true)
    @NotNull
    OppgaveStatus status,

    @JsonProperty(value = "opprettetDato", required = true)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    ZonedDateTime opprettetDato,

    @JsonProperty(value = "løstDato")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    ZonedDateTime løstDato,

    @JsonProperty(value = "frist")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    ZonedDateTime frist
) {

    @StandardAbacAttributt(value = StandardAbacAttributtType.AKTØR_ID)
    public String getAktørIdAsString() {
        return aktørId.getId();
    }

}

