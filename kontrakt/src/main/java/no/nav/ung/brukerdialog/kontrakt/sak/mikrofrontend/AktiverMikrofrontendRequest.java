package no.nav.ung.brukerdialog.kontrakt.sak.mikrofrontend;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.ung.brukerdialog.abac.StandardAbacAttributt;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;


public record AktiverMikrofrontendRequest(

    @NotNull
    @Valid
    AktørId aktørId,

    @NotNull
    @Valid
    Saksnummer saksnummer
) {

    @StandardAbacAttributt(value = StandardAbacAttributtType.AKTØR_ID)
    public String getAktørIdAsString() {
        return aktørId.getId();
    }

    @StandardAbacAttributt(value = StandardAbacAttributtType.SAKSNUMMER)
    public String getSaksnummerAsString() {
        return saksnummer.getVerdi();
    }
}
