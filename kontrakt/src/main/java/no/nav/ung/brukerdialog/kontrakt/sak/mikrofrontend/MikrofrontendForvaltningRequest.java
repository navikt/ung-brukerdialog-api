package no.nav.ung.brukerdialog.kontrakt.sak.mikrofrontend;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.ung.brukerdialog.abac.StandardAbacAttributt;
import no.nav.ung.brukerdialog.typer.PersonIdent;

public record MikrofrontendForvaltningRequest(

    @NotNull
    @Valid
    PersonIdent fnr,

    @NotNull
    @Size(min = 3, max = 4000)
    @Pattern(regexp = "^[\\p{Graph}\\p{IsWhite_Space}\\p{Sc}\\p{L}\\p{M}\\p{N}§]+$")
    String begrunnelse
) {

    @AssertTrue(message = "fnr må være et gyldig fødselsnummer eller D-nummer")
    public boolean isGyldigFnr() {
        return fnr == null || fnr.erNorskIdent();
    }

    @StandardAbacAttributt(value = StandardAbacAttributtType.FNR)
    public String getFnrAsString() {
        return fnr == null ? null : fnr.getIdent();
    }

    @Override
    public String toString() {
        return "MikrofrontendForvaltningRequest<fnr=***, begrunnelse=" + begrunnelse + ">";
    }
}
