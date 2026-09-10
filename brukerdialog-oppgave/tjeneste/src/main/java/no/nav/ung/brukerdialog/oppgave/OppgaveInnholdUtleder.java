package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.inject.Instance;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
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

    List<OppgaveTekst> varselInnhold(BrukerdialogOppgaveEntitet oppgave);

    String varselLenke(BrukerdialogOppgaveEntitet oppgave);
}
