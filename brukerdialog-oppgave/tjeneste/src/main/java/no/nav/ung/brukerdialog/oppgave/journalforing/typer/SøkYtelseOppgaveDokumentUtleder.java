package no.nav.ung.brukerdialog.oppgave.journalforing.typer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentTekster;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentUtleder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kilde: {@code sif-brukerdialog/.../oppgavepaneler/sok-ytelse/i18n/nb.ts} og
 * {@code SokYtelseOppgavetekst.tsx}. Kilden har ingen brødtekst for uløst tilstand her (kun
 * tittel + kort info-setning + lenkeknapp til forsiden) - erstattet med skriftlig henvisning til
 * Min side, siden et PDF-brev ikke kan ha en lenkeknapp.
 * <p>
 * {@code fomDato} vises med samme etikett («Startdato») som frontendens
 * {@code oppsummering.startdato} bruker etter at søknaden er løst - vist allerede her siden
 * PDF-en genereres ved opprettelse, før den tilstanden finnes.
 */
@OppgaveTypeRef(OppgaveType.SØK_YTELSE)
@ApplicationScoped
public class SøkYtelseOppgaveDokumentUtleder implements OppgaveDokumentUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;

    SøkYtelseOppgaveDokumentUtleder() {
        // for CDI proxy
    }

    @Inject
    public SøkYtelseOppgaveDokumentUtleder(@Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere) {
        this.mappere = mappere;
    }

    @Override
    public String utledTittel(BrukerdialogOppgaveEntitet oppgave) {
        return switch (oppgave.getYtelsetype()) {
            case UNGDOMSYTELSE -> "Søknad for ungdomsprogramytelsen";
            case AKTIVITETSPENGER -> "Søknad om aktivitetspenger";
        };
    }

    @Override
    public String malnavn() {
        return "typer/sok-ytelse";
    }

    @Override
    public Map<String, Object> utledInnholdsdata(BrukerdialogOppgaveEntitet oppgave) {
        SøkYtelseOppgavetypeDataDto dto = hentDto(oppgave);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("infotekst", infotekst(oppgave.getYtelsetype()));
        data.put("fomDato", dto.fomDato().toString());
        OppgaveDokumentTekster.leggTilSvarfrist(data, oppgave.getFristTid());
        return data;
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
