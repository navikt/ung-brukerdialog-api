package no.nav.ung.brukerdialog.oppgave.journalforing.typer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.opphorvedmaksdato.BekreftOpphorVedMaksdatoOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentTekster;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentUtleder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kilde: {@code sif-brukerdialog/.../oppgavepaneler/opphor-ved-maksdato/i18n/nb.ts} og
 * {@code OpphorVedMaksdatoOppgavetekst.tsx}. {@code maxDato} er bevisst utelatt - kilden bruker
 * kun {@code sluttdato}/«sisteDag».
 * <p>
 * Malen bruker {@link OppgaveDokumentTekster#ytelseNavn} direkte («med ungdomsprogramytelsen»/
 * «med aktivitetspenger» passer begge grammatisk) - ingen ytelseskvalifikator-metode trengs her.
 */
@OppgaveTypeRef(OppgaveType.BEKREFT_OPPHOR_VED_MAKSDATO)
@ApplicationScoped
public class BekreftOpphorVedMaksdatoOppgaveDokumentUtleder implements OppgaveDokumentUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;

    BekreftOpphorVedMaksdatoOppgaveDokumentUtleder() {
        // for CDI proxy
    }

    @Inject
    public BekreftOpphorVedMaksdatoOppgaveDokumentUtleder(@Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere) {
        this.mappere = mappere;
    }

    @Override
    public String utledTittel(BrukerdialogOppgaveEntitet oppgave) {
        return "Tilbakemelding på sluttdato " + OppgaveDokumentTekster.ytelsePreposisjonsfrase(oppgave.getYtelsetype());
    }

    @Override
    public String malnavn() {
        return "typer/bekreft-opphor-ved-maksdato";
    }

    @Override
    public Map<String, Object> utledInnholdsdata(BrukerdialogOppgaveEntitet oppgave) {
        BekreftOpphorVedMaksdatoOppgavetypeDataDto dto = hentDto(oppgave);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sluttdato", dto.sluttdato().toString());
        data.put("ytelseNavn", OppgaveDokumentTekster.ytelseNavn(oppgave.getYtelsetype()));
        OppgaveDokumentTekster.leggTilSvarfrist(data, oppgave.getFristTid());
        return data;
    }

    private BekreftOpphorVedMaksdatoOppgavetypeDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (BekreftOpphorVedMaksdatoOppgavetypeDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
