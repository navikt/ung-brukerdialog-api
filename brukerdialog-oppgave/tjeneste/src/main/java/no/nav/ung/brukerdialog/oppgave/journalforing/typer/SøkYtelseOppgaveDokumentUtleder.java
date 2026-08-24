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
 * Brevteksten er tilpasset fra
 * {@code sif-brukerdialog/packages/ung-innsyn/.../oppgavepaneler/sok-ytelse/i18n/nb.ts} og
 * {@code SokYtelseOppgavetekst.tsx}. Kilden har - i motsetning til de andre
 * oppgavetypene - ingen egen brødtekst («oppgavetekst») for den uløste tilstanden, kun
 * {@code oppgavetittel} og en kort {@code info}-setning pluss en lenkeknapp til forsiden; denne
 * oppgaven er en invitasjon til å søke, ikke et spørsmål å besvare. Erstatter lenkeknappen med en
 * skriftlig henvisning til Min side, og legger til {@code fomDato} som et eget faktafelt med
 * samme etikett («Startdato») som frontendens {@code oppsummering.startdato} bruker for feltet
 * etter at søknaden er løst - vi viser det bare allerede her, siden PDF-en genereres ved
 * opprettelse. Grener fullt ut på {@code ytelsetype} (egne titler/tekster for begge), så
 * ytelseskvalifikatoren i {@link OppgaveDokumentTekster} trengs ikke her.
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
