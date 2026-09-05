package no.nav.ung.brukerdialog.kontrakt.sak.diagnostikk;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.ung.brukerdialog.abac.StandardAbacAttributt;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;

import java.util.Optional;

public record DiagnostikkSakRequestDto(

    @Valid
    AktørId aktørId,

    @Valid
    Saksnummer saksnummer,

    @NotNull
    @Size(max = 4000)
    @Pattern(regexp = "^[\\p{Graph}\\p{IsWhite_Space}\\p{Sc}\\p{L}\\p{M}\\p{N}§]+$")
    String begrunnelse
) {

    @AssertTrue(message = "Nøyaktig én av aktørId og saksnummer må være satt")
    public boolean isEksaktEnAvAktørIdOgSaksnummerSatt() {
        return (aktørId() == null) == (saksnummer() != null);
    }

    @StandardAbacAttributt(value = StandardAbacAttributtType.AKTØR_ID)
    public String getAktørIdAsString() {
        return Optional.ofNullable(aktørId).map(AktørId::getId).orElse(null);
    }

    @StandardAbacAttributt(value = StandardAbacAttributtType.SAKSNUMMER)
    public String getSaksnummerAsString() {
        return Optional.ofNullable(saksnummer).map(Saksnummer::getVerdi).orElse(null);
    }
}
