package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.ung.brukerdialog.abac.StandardAbacAttributt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.journalforing.JournalføringDto;
import no.nav.ung.brukerdialog.typer.AktørId;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Generell DTO for opprettelse av en brukerdialogoppgave.
 * Oppgavetypen bestemmes av {@link OppgavetypeDataDto}-subtypen i {@code oppgavetypeData}.
 * <p>
 * {@code journalføring.saksnummer} håndheves IKKE som påkrevd her ennå - se
 * "Kjente begrensninger" i JOURNALFORING.md. En manglende {@code saksnummer} gir i mellomtiden
 * kun en {@code WARN}-logg (håndtert i {@code OppgaveLivssyklusTjeneste}), ikke en hard 400,
 * inntil `ung-sak`/nedstrøms konsumenter bekreftet sender feltet i prod. Se kommentert
 * {@code @AssertTrue}-metode under for hvordan dette reaktiveres.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpprettOppgaveDto(

    @JsonProperty(value = "aktørId", required = true)
    @NotNull
    @Valid
    AktørId aktørId,

    @JsonProperty(value = "ytelsetype", required = true)
    @NotNull
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
    LocalDateTime frist,

    @JsonProperty(value = "journalføring")
    @Valid
    JournalføringDto journalføring
) {

    @StandardAbacAttributt(value = StandardAbacAttributtType.AKTØR_ID)
    public String getAktørIdAsString() {
        return aktørId.getId();
    }

    // TODO: Aktiver når `ung-sak`/nedstrøms konsumenter er bekreftet klare til å alltid sende
    // `saksnummer` i prod (se "Kjente begrensninger" i JOURNALFORING.md). Fjern kommentartegnene
    // under for å reaktivere håndhevingen som en hard 400-feil i stedet for dagens WARN-logg
    // (OppgaveLivssyklusTjeneste).
    //
    // @AssertTrue(message = "saksnummer er påkrevd for denne oppgavetypen")
    // private boolean isSaksnummerGyldigForOppgavetype() {
    //     if (oppgavetypeData == null || oppgavetypeData.oppgavetype() == null) {
    //         return true;
    //     }
    //     if (oppgavetypeData.oppgavetype() == OppgaveType.SØK_YTELSE) {
    //         return true;
    //     }
    //     return journalføring != null && journalføring.saksnummer() != null;
    // }
}

