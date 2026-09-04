package no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Typesikker tekstblokk til bruk i oppgavebrev - delt datamodell mellom PDF-dokumentet
 * ({@code brukerdialog-oppgave/pdf}), min-side-varselet og {@code tekster}-feltet på
 * {@link no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto}, slik at teksten som
 * vises de tre stedene aldri kan drifte fra hverandre.
 * <p>
 * En oppgave sin fullstendige tekst er en {@code List<OppgaveTekst>} som konsumenten itererer
 * over i rekkefølge - ikke et generisk {@code Map<String, Object>}. Kontrakten er at det FØRSTE
 * elementet i listen alltid er en {@link OppgaveAvsnitt} (ren tekst, uten overskrift), siden dette
 * elementet også brukes som selve varselteksten på Min Side.
 * <p>
 * {@code fet} gjelder hele blokken (ikke enkeltord/delsetninger inni et avsnitt) - se
 * Fase 1-beslutning i plansporet for denne modellen.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OppgaveAvsnitt.class, name = "AVSNITT"),
    @JsonSubTypes.Type(value = OppgaveListe.class, name = "LISTE"),
    @JsonSubTypes.Type(value = OppgaveTabell.class, name = "TABELL")
})
public sealed interface OppgaveTekst permits OppgaveAvsnitt, OppgaveListe, OppgaveTabell {

    /**
     * Valgfri overskrift for denne blokken alene. {@code null} når blokken ikke skal ha egen
     * overskrift (det vanlige tilfellet - de fleste avsnitt i et oppgavebrev har ingen egen
     * tittel).
     */
    String tittel();

    /** Sant dersom hele blokkens innhold skal vises i fet skrift. */
    boolean fet();
}
