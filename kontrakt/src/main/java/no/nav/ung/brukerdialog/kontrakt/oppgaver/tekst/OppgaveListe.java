package no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst;

import java.util.List;

/** En punktliste, f.eks. et {@code <ul>}-element i PDF-brevet. */
public record OppgaveListe(
    String tittel,
    List<String> punkter,
    boolean fet
) implements OppgaveTekst {

    public OppgaveListe(List<String> punkter) {
        this(null, punkter, false);
    }

    /** Ingen egen tittel, men hele listen skal ev. være fet. */
    public OppgaveListe(List<String> punkter, boolean fet) {
        this(null, punkter, fet);
    }
}
