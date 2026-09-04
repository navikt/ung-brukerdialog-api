package no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** En punktliste, f.eks. et {@code <ul>}-element i PDF-brevet. */
public record OppgaveListe(
    @JsonProperty("tittel") String tittel,
    @JsonProperty(value = "punkter", required = true) List<String> punkter,
    @JsonProperty(value = "fet", required = true) boolean fet
) implements OppgaveTekst {

    /** Vanlig tilfelle: verken egen tittel eller fet skrift. */
    public OppgaveListe(List<String> punkter) {
        this(null, punkter, false);
    }

    /** Ingen egen tittel, men hele listen skal ev. være fet. */
    public OppgaveListe(List<String> punkter, boolean fet) {
        this(null, punkter, fet);
    }
}
