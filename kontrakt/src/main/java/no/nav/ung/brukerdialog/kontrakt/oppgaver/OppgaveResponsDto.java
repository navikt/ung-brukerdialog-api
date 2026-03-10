package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.RapportertInntektDto;

/**
 * Interface for respons-data.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SvarPåVarselDto.class, name = "VARSEL_SVAR"),
    @JsonSubTypes.Type(value = RapportertInntektDto.class, name = "RAPPORTERT_INNTEKT")
})
public class OppgaveResponsDto { }

