package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.livsopphold;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserAvklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.OppgaveTekster;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.pdf.OppgaveTekstfragmentRenderer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@OppgaveTypeRef(OppgaveType.BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER)
@ApplicationScoped
public class BekreftAndreLivsoppholdsytelserOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String aktivitetspengerInnsynBaseUrl;
    private OppgaveTekstfragmentRenderer renderer;

    BekreftAndreLivsoppholdsytelserOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public BekreftAndreLivsoppholdsytelserOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "AKTIVITETSPENGER_INNSYN_BASE_URL") String aktivitetspengerInnsynBaseUrl,
        OppgaveTekstfragmentRenderer renderer
    ) {
        this.mappere = mappere;
        this.aktivitetspengerInnsynBaseUrl = aktivitetspengerInnsynBaseUrl;
        this.renderer = renderer;
    }

    @Override
    public String undertittel(BrukerdialogOppgaveEntitet oppgave) {
        return "Andre ytelser til livsopphold";
    }

    @Override
    public List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave) {
        validerYtelsetype(oppgave.getYtelsetype());

        OppgavetypeDataDto dto = OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());

        LocalDate fom;
        LocalDate tom;
        AndreLivsoppholdsytelserIkkeOppfyltÅrsak årsak;
        String fritekst;
        AndreLivsoppholdsytelserAvklaringKildeType kilde;
        String kildeFritekst;

        switch (dto) {
            case BekreftAndreLivsoppholdsytelserOppgavetypeDataDto bundet -> {
                fom = bundet.fom();
                tom = bundet.tom();
                årsak = bundet.ikkeOppfyltÅrsak();
                fritekst = bundet.ikkeOppfyltÅrsakFritekstbeskrivelse();
                kilde = bundet.kilde();
                kildeFritekst = bundet.kildeFritekst();
            }
            case BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto opphør -> {
                fom = opphør.fom();
                tom = null;
                årsak = opphør.ikkeOppfyltÅrsak();
                fritekst = opphør.ikkeOppfyltÅrsakFritekstbeskrivelse();
                kilde = opphør.kilde();
                kildeFritekst = opphør.kildeFritekst();
            }
            default -> throw new IllegalArgumentException(
                "Ikke støttet oppgavedata for " + OppgaveType.BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER + ": " + dto.getClass().getName());
        }

        return renderer.rendre("tekstfragmenter/livsopphold/bekreft_andre_livsoppholdsytelser",
            byggData(årsak, fom, tom, fritekst, kilde, kildeFritekst, oppgave.getFristTid()));
    }

    private static Map<String, Object> byggData(
        AndreLivsoppholdsytelserIkkeOppfyltÅrsak årsak, LocalDate fom, LocalDate tom, String fritekst,
        AndreLivsoppholdsytelserAvklaringKildeType kilde, String kildeFritekst, LocalDateTime fristTid
    ) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("årsak", årsak.name());
        data.put("erPeriode", tom != null);
        data.put("fom", fom.toString());
        data.put("tom", tom != null ? tom.toString() : null);
        data.put("årsakFritekst", fritekst != null && !fritekst.isBlank() ? fritekst : null);
        data.put("kilde", kilde.name());
        data.put("kildeFritekst", kildeFritekst);
        data.put("fristDato", OppgaveTekster.fristDato(fristTid));
        return data;
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return aktivitetspengerInnsynBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
    }

    private static void validerYtelsetype(OppgaveYtelsetype ytelsetype) {
        if (ytelsetype != OppgaveYtelsetype.AKTIVITETSPENGER) {
            throw new IllegalStateException(
                "BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER støtter kun AKTIVITETSPENGER, fikk ytelsetype=%s".formatted(ytelsetype));
        }
    }
}
