package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.enterprise.inject.Instance;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;

import java.util.Map;

/**
 * SPI for å utlede tittel, PDF-mal og PDF-innhold for journalføring av en brukerdialogoppgave.
 * Én implementasjon per {@link OppgaveType}, koblet via {@link OppgaveTypeRef} - samme mønster
 * som {@code OppgavelInnholdUtleder} for Min Side-varsel.
 * <p>
 * Skal ALDRI returnere navn eller fødselsnummer - personopplysningene slås sammen med det
 * oppgavetype-spesifikke innholdet av kalleren ({@code JournalførOppgaveTask}), ikke her. Det
 * holder implementasjonene testbare uten PDL og uten ekte fødselsnumre.
 * <p>
 * Tittel og innhold er kuratert brevtekst, tilpasset fra allerede godkjent produkttekst i
 * {@code sif-brukerdialog}/{@code ung-innsyn} (Min Side / ungdomsprogram-deltaker). Se den
 * enkelte {@code typer}-klasse for begrunnelse per oppgavetype/gren.
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
     * Navnet på Handlebars-malen (uten filendelse, under {@code handlebars/typer/} i
     * journalforing-modulen) som skal brukes til å rendre PDF-en for denne oppgavetypen. Én fast
     * verdi per implementasjon - forgrening i innhold (f.eks. bosted bundet/opphør, endret
     * sluttdato/meldt ut) skjer via data-flagg fra {@link #utledInnholdsdata}, ikke ved å velge
     * mellom flere maler.
     */
    String malnavn();

    /**
     * Oppgavetype-spesifikke data til PDF-malen, under nøkkelen {@code "oppgave"}. Skal ALDRI
     * inneholde navn eller fødselsnummer - se klasse-javadoc.
     */
    Map<String, Object> utledInnholdsdata(BrukerdialogOppgaveEntitet oppgave);
}
