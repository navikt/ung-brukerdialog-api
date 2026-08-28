package no.nav.ung.brukerdialog.oppgave.journalforing.typer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentTekster;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentUtleder;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kilde: {@code sif-brukerdialog/.../oppgavepaneler/bostedsvilkar/i18n/nb.ts}. Ingen
 * ytelseskvalifikator - gjelder alltid aktivitetspenger.
 * <p>
 * To DTO-varianter fra vår mapper: bundet periode ({@link BekreftBostedOppgavetypeDataDto}) og
 * åpen/opphørt periode ({@link BekreftBostedOpphørOppgavetypeDataDto}, uten {@code tom}).
 * Frontend har ingen tekst for opphør-varianten - «Dette gjelder fra og med ...»-formen under er
 * nytt formuleringsarbeid, ikke hentet fra kilden.
 */
@OppgaveTypeRef(OppgaveType.BEKREFT_BOSTED)
@ApplicationScoped
public class BekreftBostedOppgaveDokumentUtleder implements OppgaveDokumentUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;

    BekreftBostedOppgaveDokumentUtleder() {
        // for CDI proxy
    }

    @Inject
    public BekreftBostedOppgaveDokumentUtleder(@Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere) {
        this.mappere = mappere;
    }

    @Override
    public String utledTittel(BrukerdialogOppgaveEntitet oppgave) {
        return "Bekrefte bosted for aktivitetspenger";
    }

    @Override
    public String malnavn() {
        return "typer/bekreft-bosted";
    }

    @Override
    public Map<String, Object> utledInnholdsdata(BrukerdialogOppgaveEntitet oppgave) {
        OppgavetypeDataDto dto = OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());

        LocalDate fom;
        LocalDate tom;
        boolean erBosattITrondheim;
        String fritekst;
        BostedsvilkårIkkeOppfyltÅrsak årsak;

        if (dto instanceof BekreftBostedOppgavetypeDataDto bundet) {
            fom = bundet.fom();
            tom = bundet.tom();
            erBosattITrondheim = bundet.erBosattITrondheim();
            fritekst = bundet.ikkeOppfyltÅrsakFritekstbeskrivelse();
            årsak = bundet.ikkeOppfyltÅrsak();
        } else {
            var opphør = (BekreftBostedOpphørOppgavetypeDataDto) dto;
            fom = opphør.fom();
            tom = null;
            erBosattITrondheim = opphør.erBosattITrondheim();
            fritekst = opphør.ikkeOppfyltÅrsakFritekstbeskrivelse();
            årsak = opphør.ikkeOppfyltÅrsak();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fom", fom.toString());
        if (tom != null) {
            data.put("tom", tom.toString());
        }
        data.put("erBosattITrondheim", erBosattITrondheim ? "Ja" : "Nei");
        String forklaring = OppgaveDokumentTekster.bostedIkkeOppfyltForklaring(årsak, fritekst);
        if (forklaring != null) {
            data.put("ikkeOppfyltForklaring", forklaring);
        }
        OppgaveDokumentTekster.leggTilSvarfrist(data, oppgave.getFristTid());
        return data;
    }
}
