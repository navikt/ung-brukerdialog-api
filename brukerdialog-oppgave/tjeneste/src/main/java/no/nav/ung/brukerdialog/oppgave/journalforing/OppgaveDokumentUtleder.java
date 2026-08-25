package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.enterprise.inject.Instance;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;

import java.util.Map;

/**
 * SPI for å utlede tittel, PDF-mal og PDF-innhold for journalføring, én implementasjon per
 * {@link OppgaveType} koblet via {@link OppgaveTypeRef}.
 * <p>
 * Skal ALDRI returnere navn eller fødselsnummer - kalleren ({@code JournalførOppgaveTask})
 * fletter inn personopplysningene selv, ikke implementasjonene her.
 */
public interface OppgaveDokumentUtleder {

    static OppgaveDokumentUtleder finnUtleder(Instance<OppgaveDokumentUtleder> utledere, OppgaveType oppgaveType) {
        return OppgaveTypeRef.Lookup.find(utledere, oppgaveType)
            .orElseThrow(() -> new IllegalArgumentException("Finner ingen dokumentutleder for oppgavetype: " + oppgaveType));
    }

    /**
     * Tittel på journalposten og på dokumentet i journalposten (vises i Gosys/SAF).
     */
    String utledTittel(BrukerdialogOppgaveEntitet oppgave);

    /**
     * Handlebars-malnavn (uten filendelse, under {@code handlebars/typer/}). Fast per
     * implementasjon - forgrening skjer via data fra {@link #utledInnholdsdata}, ikke flere maler.
     */
    String malnavn();

    /**
     * Oppgavetype-spesifikke data til PDF-malen, under nøkkelen {@code "oppgave"}. Skal ALDRI
     * inneholde navn eller fødselsnummer - se klasse-javadoc.
     */
    Map<String, Object> utledInnholdsdata(BrukerdialogOppgaveEntitet oppgave);
}
