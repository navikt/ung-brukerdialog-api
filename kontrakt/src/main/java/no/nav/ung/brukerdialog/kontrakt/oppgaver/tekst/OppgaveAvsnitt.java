package no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst;

/**
 * Et enkelt tekstavsnitt, f.eks. ett {@code <p>}-element i PDF-brevet. Det vanligste og enkleste
 * elementet i en {@link OppgaveTekst}-liste.
 * <p>
 * {@code innhold} kan inneholde bokstavelige {@code <b>}/{@code </b>}-tagger rundt deler av
 * teksten som skal vises fet (f.eks. en dato).
 */
public record OppgaveAvsnitt(
    String tittel,
    String innhold
) implements OppgaveTekst {

    public OppgaveAvsnitt(String innhold) {
        this(null, innhold);
    }
}
