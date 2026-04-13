package no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.OppgavelInnholdUtleder;

import java.time.Month;

@OppgaveTypeRef(OppgaveType.RAPPORTER_INNTEKT)
@ApplicationScoped
public class InntektsrapporteringOppgavelInnholdUtleder implements OppgavelInnholdUtleder {

    private String ungdomsprogramytelsenDeltakerBaseUrl;
    private String aktivitetspengerInnsynBaseUrl;

    @Inject
    public InntektsrapporteringOppgavelInnholdUtleder(
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl,
        @KonfigVerdi(value = "AKTIVITETSPENGER_INNSYN_BASE_URL") String aktivitetspengerInnsynBaseUrl
    ) {
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
        this.aktivitetspengerInnsynBaseUrl = aktivitetspengerInnsynBaseUrl;
    }

    public InntektsrapporteringOppgavelInnholdUtleder() {
    }

    @Override
    public String utledVarselTekst(BrukerdialogOppgaveEntitet oppgave) {
        var oppgaveData = (InntektsrapporteringOppgaveDataEntitet) oppgave.getOppgaveData();
        String norskMånedNavn = finnNorskMånedNavn(oppgaveData.getFraOgMed().getMonth());
        return String.format("Du har fått en oppgave om å registrere inntekten din for %s dersom du har det.", norskMånedNavn);
    }

    @Override
    public String utledVarselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return switch (oppgave.getYtelsetype()) {
            case AKTIVITETSPENGER -> aktivitetspengerInnsynBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
            case UNGDOMSYTELSE -> ungdomsprogramytelsenDeltakerBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
        };
    }

    public String finnNorskMånedNavn(Month måned) {
        return switch (måned) {
            case Month.JANUARY -> "januar";
            case Month.FEBRUARY -> "februar";
            case Month.MARCH -> "mars";
            case Month.APRIL -> "april";
            case Month.MAY -> "mai";
            case Month.JUNE -> "juni";
            case Month.JULY -> "juli";
            case Month.AUGUST -> "august";
            case Month.SEPTEMBER -> "september";
            case Month.OCTOBER -> "oktober";
            case Month.NOVEMBER -> "november";
            case Month.DECEMBER -> "desember";
        };
    }

}
