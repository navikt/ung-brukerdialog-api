package no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Typesikker tekstblokk til bruk i oppgavebrev - delt datamodell mellom PDF-dokumentet
 * ({@code brukerdialog-oppgave/pdf}), min-side-varselet og {@code varselInnhold}-feltet på
 * {@link no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto}, slik at teksten som
 * vises de tre stedene aldri kan drifte fra hverandre. Merk at {@code varselInnhold} kun er en
 * innsnevret undermengde av oppgavens fulle tekst (PDF-en/min-side-varselet baserer seg på hele
 * listen via {@code OppgaveInnholdUtleder#tekster}, mens {@code varselInnhold} kun inneholder
 * elementene som er merket relevante for konsumenten).
 * <p>
 * En oppgave sin fullstendige tekst er en {@code List<OppgaveTekst>} som konsumenten itererer
 * over i rekkefølge - ikke et generisk {@code Map<String, Object>}. Kontrakten er at det FØRSTE
 * elementet i den fulle listen alltid er en {@link OppgaveAvsnitt} (ren tekst, uten overskrift),
 * siden dette elementet også brukes som selve varselteksten på Min Side.
 * <p>
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
