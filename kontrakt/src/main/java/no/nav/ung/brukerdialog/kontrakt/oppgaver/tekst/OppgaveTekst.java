package no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Typesikker tekstblokk til bruk i PDF-brevet og min-side-varselet - konsumeres via
 * {@code OppgaveInnholdUtleder#tekster}. Det FØRSTE elementet er alltid en {@link OppgaveAvsnitt}
 * (ren tekst, uten overskrift), siden dette elementet også brukes som selve varselteksten på
 * Min Side og som {@code varseltekst} på {@link no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto}.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OppgaveAvsnitt.class, name = "AVSNITT"),
    @JsonSubTypes.Type(value = OppgavePunktliste.class, name = "PUNKT_LISTE"),
    @JsonSubTypes.Type(value = OppgaveTabell.class, name = "TABELL")
})
public sealed interface OppgaveTekst permits OppgaveAvsnitt, OppgavePunktliste, OppgaveTabell {

    /**
     * Valgfri overskrift for denne blokken alene. {@code null} når blokken ikke skal ha egen
     * overskrift (det vanlige tilfellet - de fleste avsnitt i et oppgavebrev har ingen egen
     * tittel).
     */
    default String tittel() {
        return null;
    }

    default boolean fet() {
        return false;
    }
}
