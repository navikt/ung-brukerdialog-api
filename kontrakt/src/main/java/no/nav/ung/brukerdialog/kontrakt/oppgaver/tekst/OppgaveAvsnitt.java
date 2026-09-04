package no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Et enkelt tekstavsnitt, f.eks. ett {@code <p>}-element i PDF-brevet. Det vanligste og enkleste
 * elementet i en {@link OppgaveTekst}-liste.
 */
public record OppgaveAvsnitt(
    @JsonProperty("tittel") String tittel,
    @JsonProperty(value = "innhold", required = true) String innhold,
    @JsonProperty(value = "fet", required = true) boolean fet
) implements OppgaveTekst {

    /** Vanlig tilfelle: verken egen tittel eller fet skrift. */
    public OppgaveAvsnitt(String innhold) {
        this(null, innhold, false);
    }

    /** Avsnitt uten egen tittel, men med eksplisitt fet-verdi (f.eks. den innledende svarfrist-setningen). */
    public OppgaveAvsnitt(String innhold, boolean fet) {
        this(null, innhold, fet);
    }
}
