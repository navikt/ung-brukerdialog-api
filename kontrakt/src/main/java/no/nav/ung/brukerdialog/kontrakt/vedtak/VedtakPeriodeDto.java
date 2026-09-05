package no.nav.ung.brukerdialog.kontrakt.vedtak;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.ung.brukerdialog.typer.Periode;

public record VedtakPeriodeDto(
    @NotNull
    @Valid
    Periode periode,

    @NotNull
    VedtakResultatType vedtakResultatType
){}
