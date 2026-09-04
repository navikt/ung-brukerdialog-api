package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.inject.Instance;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;

import java.util.List;

/**
 * SPI for å utlede tittel, brevtekst og varsel-lenke for en oppgave - én implementasjon per
 * {@link OppgaveType}, koblet via {@link OppgaveTypeRef}. Konsoliderer det som tidligere var to
 * separate SPI-er ({@code OppgaveDokumentUtleder} for PDF-journalføring og
 * {@code OppgavelInnholdUtleder} for min-side-varsel), slik at PDF, varsel og
 * {@code BrukerdialogOppgaveDto} alltid viser samme tekst.
 * <p>
 * Brukes av:
 * <ul>
 *     <li>PDF-journalføring ({@code JournalførOppgaveTask}) - {@link #tittel} + {@link #tekster}</li>
 *     <li>Min side-varsel ({@code OppgaveLivssyklusTjeneste}) - det FØRSTE elementet fra
 *     {@link #tekster} (kontrakt: alltid en {@link OppgaveAvsnitt}) + {@link #varselLenke}</li>
 *     <li>{@code BrukerdialogOppgaveDto} ({@code BrukerdialogOppgaveMapper}) - hele {@link #tekster}</li>
 * </ul>
 * <p>
 * Skal ALDRI returnere navn eller fødselsnummer - kalleren (kun aktuelt for PDF-en, se
 * {@code JournalførOppgaveTask}) henter og fletter disse inn selv, ikke implementasjonene her.
 * Andre personopplysninger (datoer, beløp, brukerskrevet fritekst m.m.) kan forekomme og skal
 * aldri logges.
 */
public interface OppgaveInnholdUtleder {

    static OppgaveInnholdUtleder finnUtleder(Instance<OppgaveInnholdUtleder> utledere, OppgaveType oppgaveType) {
        return OppgaveTypeRef.Lookup.find(utledere, oppgaveType)
            .orElseThrow(() -> new IllegalArgumentException("Finner ingen innholdsutleder for oppgavetype: " + oppgaveType));
    }

    /**
     * Tittel på PDF-dokumentet i journalposten (vises i Gosys/SAF, og som PDF-ens
     * {@code <title>}). Journalpostens egen tittel utledes separat, se
     * {@code JournalføringParametre#journalposttittel}.
     */
    String tittel(BrukerdialogOppgaveEntitet oppgave);

    /**
     * Fullstendig brevtekst, i visningsrekkefølge. Kontrakt: det FØRSTE elementet skal alltid
     * være en {@link OppgaveAvsnitt} - dette elementets {@link OppgaveAvsnitt#innhold()} brukes
     * direkte som varselteksten på Min Side, som krever ren tekst.
     */
    List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave);

    /** Lenke til varselet på Min Side (typisk til den relevante innsynsløsningen for ytelsen). */
    String varselLenke(BrukerdialogOppgaveEntitet oppgave);
}
