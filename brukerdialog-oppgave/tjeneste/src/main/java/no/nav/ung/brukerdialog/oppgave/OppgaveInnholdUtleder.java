package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.inject.Instance;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;

import java.util.List;

public interface OppgaveInnholdUtleder {

    static OppgaveInnholdUtleder finnUtleder(Instance<OppgaveInnholdUtleder> utledere, OppgaveType oppgaveType) {
        return OppgaveTypeRef.Lookup.find(utledere, oppgaveType)
            .orElseThrow(() -> new IllegalArgumentException("Finner ingen innholdsutleder for oppgavetype: " + oppgaveType));
    }

    default String tittel(BrukerdialogOppgaveEntitet oppgave) {
        return OppgaveTekster.VARSEL_OM_NYE_OPPLYSNINGER_TITTEL;
    }

    String undertittel(BrukerdialogOppgaveEntitet oppgave);

    List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave);

    // Første tekstblokk i tekster(oppgave) - brukes som varseltekst på Min Side og i API-kontrakten.
    default String varseltekst(BrukerdialogOppgaveEntitet oppgave) {
        List<OppgaveTekst> tekster = tekster(oppgave);
        if (tekster.isEmpty() || !(tekster.getFirst() instanceof OppgaveAvsnitt avsnitt)) {
            throw new IllegalStateException(
                "Første tekstblokk for oppgaveType=%s må være et OppgaveAvsnitt for å kunne brukes som varseltekst"
                    .formatted(oppgave.getOppgaveType()));
        }
        String varseltekst = avsnitt.innhold();
        OppgaveTekster.validerVarselTekstLengde(varseltekst, oppgave.getOppgaveType());
        return varseltekst;
    }

    String varselLenke(BrukerdialogOppgaveEntitet oppgave);
}
