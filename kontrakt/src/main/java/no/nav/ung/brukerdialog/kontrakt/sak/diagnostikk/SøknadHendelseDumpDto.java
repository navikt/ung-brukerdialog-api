package no.nav.ung.brukerdialog.kontrakt.sak.diagnostikk;

import java.time.LocalDateTime;
import java.util.UUID;

public record SøknadHendelseDumpDto(
    Long id,
    UUID søknadId,
    String ytelseType,
    LocalDateTime mottatt,
    boolean aktiv,
    String mottattISaksnummer,
    String opprettetAv,
    LocalDateTime opprettetTidspunkt,
    String endretAv,
    LocalDateTime endretTidspunkt
) {
}
