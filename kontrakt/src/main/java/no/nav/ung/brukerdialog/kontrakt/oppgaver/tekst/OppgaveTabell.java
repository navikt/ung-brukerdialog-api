package no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * En tabell, f.eks. inntektsoversikten i {@code BEKREFT_AVVIK_REGISTERINNTEKT}-brevet.
 * <p>
 * {@code rader} er posisjonsbasert (rad-per-liste, celle-per-indeks, parret med
 * {@code kolonneOverskrifter} via indeks) - en bevisst forenkling siden det per i dag kun finnes
 * ett tabell-brukstilfelle. Vurder en dedikert rad-record hvis flere, ulikt formede tabeller
 * kommer til senere.
 */
public record OppgaveTabell(
    @JsonProperty("tittel") String tittel,
    @JsonProperty(value = "kolonneOverskrifter", required = true) List<String> kolonneOverskrifter,
    @JsonProperty(value = "rader", required = true) List<List<String>> rader,
    @JsonProperty(value = "fet", required = true) boolean fet
) implements OppgaveTekst {

    /** Vanlig tilfelle: verken egen tittel eller fet skrift. */
    public OppgaveTabell(List<String> kolonneOverskrifter, List<List<String>> rader) {
        this(null, kolonneOverskrifter, rader, false);
    }
}
