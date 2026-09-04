package no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.OppgaveTekster;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.pdf.NorskDatoFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Kilde: {@code sif-brukerdialog/.../oppgavepaneler/sok-ytelse/i18n/nb.ts} og
 * {@code SokYtelseOppgavetekst.tsx}. Kilden har ingen brødtekst for uløst tilstand her (kun
 * tittel + kort info-setning + lenkeknapp til forsiden) - erstattet med skriftlig henvisning til
 * Min side, siden et PDF-brev/varsel ikke kan ha en lenkeknapp.
 * <p>
 * <b>Bevisst avvik, videreført:</b> {@link #varselLenke} peker på selve base-URL-en (uten
 * {@code /oppgave}-suffiks), i motsetning til alle andre typer her - dette speiler opprinnelig
 * oppførsel og er ikke endret som del av denne refaktoreringen.
 */
@OppgaveTypeRef(OppgaveType.SØK_YTELSE)
@ApplicationScoped
public class SøkYtelseOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;

    SøkYtelseOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public SøkYtelseOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl
    ) {
        this.mappere = mappere;
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
    }

    @Override
    public String tittel(BrukerdialogOppgaveEntitet oppgave) {
        return switch (oppgave.getYtelsetype()) {
            case UNGDOMSYTELSE -> "Søknad for ungdomsprogramytelsen";
            case AKTIVITETSPENGER -> "Søknad om aktivitetspenger";
        };
    }

    @Override
    public List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave) {
        SøkYtelseOppgavetypeDataDto dto = hentDto(oppgave);

        List<OppgaveTekst> tekster = new ArrayList<>();
        tekster.add(new OppgaveAvsnitt(infotekst(oppgave.getYtelsetype())));
        tekster.add(new OppgaveAvsnitt("Startdato: " + NorskDatoFormat.datoLang(dto.fomDato()), true));
        tekster.add(new OppgaveAvsnitt("Du finner søknaden på Min side på nav.no."));
        OppgaveTekster.leggTilSvarfrist(tekster, oppgave.getFristTid(), "søke", null);
        return tekster;
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return ungdomsprogramytelsenDeltakerBaseUrl;
    }

    private static String infotekst(OppgaveYtelsetype ytelsetype) {
        return switch (ytelsetype) {
            case UNGDOMSYTELSE -> "Du er meldt inn i ungdomsprogrammet. Nå kan du søke om ungdomsprogramytelsen.";
            case AKTIVITETSPENGER -> "Du har søkt om aktivitetspenger.";
        };
    }

    private SøkYtelseOppgavetypeDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (SøkYtelseOppgavetypeDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
