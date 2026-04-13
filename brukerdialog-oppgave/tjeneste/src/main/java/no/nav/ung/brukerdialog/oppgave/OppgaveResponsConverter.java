package no.nav.ung.brukerdialog.oppgave;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.ung.brukerdialog.JsonObjectMapper;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveResponsDto;

/**
 * Converter for å serialisere BekreftelseDTO til/fra JSONB.
 */
@Converter
public class OppgaveResponsConverter implements AttributeConverter<OppgaveResponsDto, String> {

    private static final ObjectMapper MAPPER = JsonObjectMapper.getMapper();

    @Override
    public String convertToDatabaseColumn(OppgaveResponsDto attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Kunne ikke serialisere BekreftelseDTO til JSON", e);
        }
    }

    @Override
    public OppgaveResponsDto convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(dbData, OppgaveResponsDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Kunne ikke deserialisere BekreftelseDTO fra JSON", e);
        }
    }
}

