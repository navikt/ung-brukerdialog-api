package no.nav.ung.brukerdialog.oppgave.journalforing;

/**
 * Fagsaksystem i dokarkiv-forstand. Utledes fra oppgavens ytelsetype - kan ikke sendes inn av
 * kalleren (se {@link JournalføringParametre#utled}).
 * <p>
 * Inneholder kun faktiske fagsaksystemer. {@code GENERELL_SAK} er en dokarkiv-<b>sakstype</b>,
 * ikke et fagsaksystem, og er modellert som {@link Sakstype#GENERELL_SAK} i stedet.
 */
public enum Fagsaksystem {

    UNG_SAK;
    // TODO: K9 når K9-ytelser skal støttes. Se JournalføringParametre.utled(...).
}
