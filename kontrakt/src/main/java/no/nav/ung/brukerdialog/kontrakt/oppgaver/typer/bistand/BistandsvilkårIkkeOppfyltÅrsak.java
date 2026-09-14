package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand;

/**
 * Kontraktens egen kopi av ung-sak sitt kodeverk for hvorfor bistandsvilkåret ikke er oppfylt.
 * AVKORTET finnes i ung-sak, men er bevisst utelatt her: den brukes kun når saksbehandler
 * innvilger en kortere periode enn systemet tillater, og skal aldri varsles om.
 */
public enum BistandsvilkårIkkeOppfyltÅrsak {
    IKKE_14A_VEDTAK,
    KOMMET_I_UTDANNING,
    KOMMET_I_ARBEID,
    ANNET,
    UDEFINERT
}
