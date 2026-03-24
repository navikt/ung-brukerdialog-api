package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.ung.brukerdialog.abac.StandardAbacAttributt;
import no.nav.ung.brukerdialog.typer.AktørId;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Generell DTO for opprettelse av en brukerdialogoppgave.
 * Oppgavetypen bestemmes av {@link OppgavetypeDataDto}-subtypen i {@code oppgavetypeData}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpprettOppgaveDto(

    @JsonProperty(value = "aktørId", required = true)
    @NotNull
    @Valid
    AktørId aktørId,

    @JsonProperty(value = "ytelsetype")
    @Valid
    OppgaveYtelsetype ytelsetype,

    @JsonProperty(value = "oppgaveReferanse", required = true)
    @NotNull
    UUID oppgaveReferanse,

    @JsonProperty(value = "oppgavetypeData", required = true)
    @NotNull
    @Valid
    OppgavetypeDataDto oppgavetypeData,

    @JsonProperty(value = "frist")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime frist
) {

    @StandardAbacAttributt(value = StandardAbacAttributtType.AKTØR_ID)
    public String getAktørIdAsString() {
        return aktørId.getId();
    }

}

