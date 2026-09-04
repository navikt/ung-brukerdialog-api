package no.nav.ung.brukerdialog.kontrakt.vedtak;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.UUID;

public record MottattSøknadDto(
    @Valid
    UUID søknadId,

    LocalDate mottattDato
) {
}
