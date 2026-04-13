package no.nav.ung.brukerdialog.kontrakt.oppgaver.diagnostikk;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.*;

import java.time.ZonedDateTime;
import java.util.UUID;

public record DiagnostikkBrukerdialogOppgaveDto(

    @JsonProperty(value = "oppgaveReferanse", required = true)
    @NotNull
    @Valid
    UUID oppgaveReferanse,

    @JsonProperty(value = "oppgavetype", required = true)
    @NotNull
    OppgaveType oppgavetype,

    @JsonProperty(value = "oppgavetypeData", required = true)
    @NotNull
    OppgavetypeDataDto oppgavetypeData,

    @JsonProperty(value = "ytelsetype", required = true)
    @NotNull
    OppgaveYtelsetype ytelsetype,

    @JsonProperty(value = "respons")
    DiagnostikkOppgaveResponsDto respons,

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

){}
