package no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.OppgaveTekster;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.pdf.OppgaveTekstfragmentRenderer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@OppgaveTypeRef(OppgaveType.SØK_YTELSE)
@ApplicationScoped
public class SøkYtelseOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;
    private OppgaveTekstfragmentRenderer renderer;

    SøkYtelseOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public SøkYtelseOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl,
        OppgaveTekstfragmentRenderer renderer
    ) {
        this.mappere = mappere;
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
        this.renderer = renderer;
    }

    @Override
    public String undertittel(BrukerdialogOppgaveEntitet oppgave) {
        return "Søknad";
    }

    @Override
    public List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave) {
        return rendre(oppgave).alle();
    }

    @Override
    public List<OppgaveTekst> varselInnhold(BrukerdialogOppgaveEntitet oppgave) {
        return rendre(oppgave).varselInnhold();
    }

    private OppgaveTekstfragmentRenderer.Resultat rendre(BrukerdialogOppgaveEntitet oppgave) {
        SøkYtelseOppgavetypeDataDto dto = hentDto(oppgave);
        validerYtelsetype(oppgave.getYtelsetype());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fomDato", dto.fomDato().toString());
        data.put("fristDato", OppgaveTekster.fristDato(oppgave.getFristTid()));

        return renderer.rendre("tekstfragmenter/sok_ytelse/sok_ytelse", data);
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return ungdomsprogramytelsenDeltakerBaseUrl;
    }

    private static void validerYtelsetype(OppgaveYtelsetype ytelsetype) {
        if (ytelsetype != OppgaveYtelsetype.UNGDOMSYTELSE) {
            throw new IllegalStateException(
                "SØK_YTELSE støtter kun UNGDOMSYTELSE, fikk ytelsetype=%s".formatted(ytelsetype));
        }
    }

    private SøkYtelseOppgavetypeDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (SøkYtelseOppgavetypeDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
