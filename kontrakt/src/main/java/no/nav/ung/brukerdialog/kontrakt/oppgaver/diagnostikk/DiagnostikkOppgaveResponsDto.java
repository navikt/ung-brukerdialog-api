package no.nav.ung.brukerdialog.kontrakt.oppgaver.diagnostikk;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.SvarPåVarselDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.RapportertInntektDto;

/**
 * Basisklasse for respons-data brukt ved polymorf serialisering og deserialisering.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DiagnostikkSvarPåVarselDto.class, name = "VARSEL_SVAR"),
    @JsonSubTypes.Type(value = DiagnostikkRapportertInntektDto.class, name = "RAPPORTERT_INNTEKT")
})
public class DiagnostikkOppgaveResponsDto { }

