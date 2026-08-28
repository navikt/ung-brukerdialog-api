package no.nav.ung.brukerdialog.oppgave.journalforing;

/**
 * Fagsaksystem i dokarkiv-forstand, utledet fra ytelsetype (se
 * {@link JournalføringParametre#utled}) - ikke sendt inn av kalleren. {@code GENERELL_SAK} er en
 * dokarkiv-sakstype, ikke et fagsaksystem - se {@link Sakstype#GENERELL_SAK} i stedet.
 */
public enum Fagsaksystem {

    UNG_SAK;
    // TODO: K9 når K9-ytelser skal støttes. Se JournalføringParametre.utled(...).
}
