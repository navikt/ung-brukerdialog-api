package no.nav.ung.brukerdialog.kontrakt.sak.diagnostikk;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VedtakPeriodeDumpDto(
    Long id,
    LocalDate fom,
    LocalDate tom,
    String resultat,
    boolean aktiv,
    String opprettetAv,
    LocalDateTime opprettetTidspunkt,
    String endretAv,
    LocalDateTime endretTidspunkt
) {
}
