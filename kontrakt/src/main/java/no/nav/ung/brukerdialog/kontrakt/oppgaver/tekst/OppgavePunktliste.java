package no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst;

import java.util.List;

/** En punktliste, f.eks. et {@code <ul>}-element i PDF-brevet. */
public record OppgavePunktliste(
    String tittel,
    List<String> punkter,
    boolean fet
) implements OppgaveTekst {

    public OppgavePunktliste(List<String> punkter) {
        this(null, punkter, false);
    }

    /** Ingen egen tittel, men hele listen skal ev. være fet. */
    public OppgavePunktliste(List<String> punkter, boolean fet) {
        this(null, punkter, fet);
    }
}
