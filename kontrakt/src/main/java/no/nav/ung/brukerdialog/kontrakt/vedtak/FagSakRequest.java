package no.nav.ung.brukerdialog.kontrakt.vedtak;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.ung.brukerdialog.abac.StandardAbacAttributt;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;

import java.util.List;

public record FagSakRequest(

    @NotNull
    @Valid
    AktørId aktørId,

    @StandardAbacAttributt(value = StandardAbacAttributtType.SAKSNUMMER)
    @NotNull
    @Valid
    Saksnummer saksnummer,

    @NotNull
    @Size(max = 100)
    List<@Valid VedtakPeriodeDto> vedtakPerioder,

    @NotNull
    @Size(max = 100)
    List<@Valid MottattSøknadDto> mottatteSøknader
) {

    @StandardAbacAttributt(value = StandardAbacAttributtType.AKTØR_ID)
    public String getAktørIdAsString() {
        return aktørId.getId();
    }

}
