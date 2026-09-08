package no.nav.ung.brukerdialog.kontrakt.soknad;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record OpprettSøknadHendelseRequest(
    @NotNull
    UUID søknadId,

    @NotNull
    LocalDateTime mottatt
) {
}
