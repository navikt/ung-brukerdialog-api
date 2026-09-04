package no.nav.ung.brukerdialog.kontrakt.soknad;

public record TilgjengeligSøknadResponse(
    TilgjengeligSøknadType type,

    boolean harUbehandletSøknad,

    boolean harInnsyn
) {
}
