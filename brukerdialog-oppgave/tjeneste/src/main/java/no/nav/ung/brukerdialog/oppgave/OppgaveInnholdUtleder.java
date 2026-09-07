package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.inject.Instance;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;

import java.util.ArrayList;
import java.util.List;

/**
 * SPI for å utlede tittel, undertittel, brevtekst og varsel-lenke for en oppgave - én
 * implementasjon per {@link OppgaveType}, koblet via {@link OppgaveTypeRef}. Konsoliderer det som
 * tidligere var to separate SPI-er ({@code OppgaveDokumentUtleder} for PDF-journalføring og
 * {@code OppgavelInnholdUtleder} for min-side-varsel), slik at PDF, varsel og
 * {@code BrukerdialogOppgaveDto} alltid viser samme tekst.
 * <p>
 * Brukes av:
 * <ul>
 *     <li>PDF-journalføring ({@code JournalførOppgaveTask}) - {@link #tittel} + {@link #undertittel}
 *     + {@link #tekster}</li>
 *     <li>Min side-varsel ({@code OppgaveLivssyklusTjeneste}) - det FØRSTE elementet fra
 *     {@link #tekster} (kontrakt: alltid en {@link OppgaveAvsnitt}) + {@link #varselLenke}</li>
 *     <li>{@code BrukerdialogOppgaveDto} ({@code BrukerdialogOppgaveMapper}) - hele {@link #tekster}
 *     + {@link #undertittel}</li>
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
     * {@code <title>}/{@code <h1>}). Lik for alle oppgavetyper - se
     * {@link OppgaveTekster#VARSEL_OM_NYE_OPPLYSNINGER_TITTEL}. Journalpostens egen tittel utledes
     * separat, se {@code JournalføringParametre#journalposttittel}.
     */
    default String tittel(BrukerdialogOppgaveEntitet oppgave) {
        return OppgaveTekster.VARSEL_OM_NYE_OPPLYSNINGER_TITTEL;
    }

    /**
     * Kort, oppgavetype-spesifikk undertittel/tema (f.eks. «Bostedsadresse»), vist rett under
     * {@link #tittel} i PDF-en. Erstatter det den tidligere per-type {@code tittel()} dekket.
     */
    String undertittel(BrukerdialogOppgaveEntitet oppgave);

    /**
     * Oppgavetype-spesifikk brevtekst, i visningsrekkefølge - IKKE inkludert den delte
     * «Om «Varsel om nye opplysninger»»-seksjonen (se {@link #tekster}). Kontrakt: det FØRSTE
     * elementet skal alltid være en {@link OppgaveAvsnitt} - dette elementets
     * {@link OppgaveAvsnitt#innhold()} brukes direkte som varselteksten på Min Side, som krever
     * ren tekst.
     */
    List<OppgaveTekst> egneTekster(BrukerdialogOppgaveEntitet oppgave);

    /**
     * Sant dersom den delte «Om «Varsel om nye opplysninger»»-seksjonen skal legges til etter
     * {@link #egneTekster}. Default sann - {@code SØK_YTELSE} og {@code RAPPORTER_INNTEKT} slår
     * den av, siden de ikke er varsel om caseworker-satte opplysninger til motsigelse.
     */
    default boolean omVarselSeksjonAktivert() {
        return true;
    }

    /**
     * Fullstendig brevtekst, i visningsrekkefølge: {@link #egneTekster} pluss (normalt) den delte
     * «Om «Varsel om nye opplysninger»»-seksjonen, se {@link #omVarselSeksjonAktivert}.
     */
    default List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave) {
        List<OppgaveTekst> tekster = new ArrayList<>(egneTekster(oppgave));
        if (omVarselSeksjonAktivert()) {
            tekster.addAll(OppgaveTekster.omVarselSeksjon());
        }
        return tekster;
    }

    /** Lenke til varselet på Min Side (typisk til den relevante innsynsløsningen for ytelsen). */
    String varselLenke(BrukerdialogOppgaveEntitet oppgave);
}
