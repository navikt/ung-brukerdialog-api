package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.automatiskopphor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.OppgavelInnholdUtleder;

@OppgaveTypeRef(OppgaveType.BEKREFT_AUTOMATISK_OPPHOR)
@ApplicationScoped
public class BekreftAutomatiskOpphorOppgavelInnholdUtleder implements OppgavelInnholdUtleder {

    private String ungdomsprogramytelsenDeltakerBaseUrl;

    @Inject
    public BekreftAutomatiskOpphorOppgavelInnholdUtleder(
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl
    ) {
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
    }

    public BekreftAutomatiskOpphorOppgavelInnholdUtleder() {
    }

    @Override
    public String utledVarselTekst(BrukerdialogOppgaveEntitet oppgave) {
        return "Din ungdomsprogramytelse opph\u00f8rer automatisk. Du kan gi oss en kommentar innen 14 dager.";
    }

    @Override
    public String utledVarselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return ungdomsprogramytelsenDeltakerBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
    }
}
