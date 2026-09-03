package no.nav.ung.brukerdialog.kontrakt.soknad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TilgjengeligSøknadDto(
    @JsonProperty(value = "type", required = true)
    TilgjengeligSøknadType type,

    @JsonProperty(value = "harUbehandletSøknad", required = true)
    boolean harUbehandletSøknad,

    @JsonProperty(value = "harInnsyn", required = true)
    boolean harInnsyn
) {
}
