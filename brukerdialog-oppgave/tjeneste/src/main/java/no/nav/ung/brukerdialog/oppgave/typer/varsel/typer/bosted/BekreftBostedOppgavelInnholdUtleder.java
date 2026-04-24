package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bosted;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.OppgavelInnholdUtleder;

@OppgaveTypeRef(OppgaveType.BEKREFT_BOSTED)
@ApplicationScoped
public class BekreftBostedOppgavelInnholdUtleder implements OppgavelInnholdUtleder {

    private String aktivitetspengerInnsynBaseUrl;

    @Inject
    public BekreftBostedOppgavelInnholdUtleder(
        @KonfigVerdi(value = "AKTIVITETSPENGER_INNSYN_BASE_URL") String aktivitetspengerInnsynBaseUrl
    ) {
        this.aktivitetspengerInnsynBaseUrl = aktivitetspengerInnsynBaseUrl;
    }

    public BekreftBostedOppgavelInnholdUtleder() {
    }

    @Override
    public String utledVarselTekst(BrukerdialogOppgaveEntitet oppgave) {
        return "Du har fått en oppgave om å bekrefte bosted for aktivitetspenger";
    }

    @Override
    public String utledVarselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return aktivitetspengerInnsynBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
    }
}
