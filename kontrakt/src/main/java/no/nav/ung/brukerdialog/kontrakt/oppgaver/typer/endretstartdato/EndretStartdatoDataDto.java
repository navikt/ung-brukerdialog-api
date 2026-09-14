package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretstartdato;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;

import java.time.LocalDate;

/**
 * Data for oppgave om endret startdato.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EndretStartdatoDataDto(
    @JsonProperty(value = "nyStartdato", required = true)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate nyStartdato,

    @JsonProperty(value = "forrigeStartdato", required = true)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate forrigeStartdato,

    // Kun satt av backend på output (varselteksten fra PDF-brevet). Ignoreres på input.
    @JsonProperty(value = "varseltekst")
    String varseltekst
) implements OppgavetypeDataDto {

    // Konstruktør uten varseltekst - brukes der varseltekst ikke er kjent/relevant enda.
    public EndretStartdatoDataDto(LocalDate nyStartdato, LocalDate forrigeStartdato) {
        this(nyStartdato, forrigeStartdato, null);
    }

    @Override
    public OppgaveType oppgavetype() {
        return OppgaveType.BEKREFT_ENDRET_STARTDATO;
    }
}

