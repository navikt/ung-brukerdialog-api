package no.nav.ung.brukerdialog.kontrakt.soknad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpprettSøknadHendelseRequest(
    @JsonProperty(value = "søknadId", required = true)
    @NotNull
    UUID søknadId,

    @JsonProperty(value = "mottatt", required = true)
    @NotNull
    LocalDateTime mottatt
) {
}
