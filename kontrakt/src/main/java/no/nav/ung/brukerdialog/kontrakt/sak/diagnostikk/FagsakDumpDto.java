package no.nav.ung.brukerdialog.kontrakt.sak.diagnostikk;

import java.time.LocalDateTime;
import java.util.List;

public record FagsakDumpDto(
    Long id,
    String saksnummer,
    String ytelseType,
    String opprettetAv,
    LocalDateTime opprettetTidspunkt,
    String endretAv,
    LocalDateTime endretTidspunkt,
    List<VedtakPeriodeDumpDto> perioder
) {
}
