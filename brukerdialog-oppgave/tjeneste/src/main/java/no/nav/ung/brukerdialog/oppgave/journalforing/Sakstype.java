package no.nav.ung.brukerdialog.oppgave.journalforing;

/**
 * Dokarkiv-sakstype. Intern type i journalføringsdomenet - ikke en del av API-kontrakten.
 * <p>
 * {@code FAGSAK} når oppgaven har en {@code saksnummer} ved opprettelse, ellers
 * {@code GENERELL_SAK} (f.eks. {@code SØK_YTELSE}, som ikke har noen fagsak å knytte seg til).
 */
public enum Sakstype {
    FAGSAK,
    GENERELL_SAK
}
