package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bosted;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak;
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

@OppgaveTypeRef(OppgaveType.BEKREFT_BOSTED)
@ApplicationScoped
public class BekreftBostedOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String aktivitetspengerInnsynBaseUrl;
    private OppgaveTekstfragmentRenderer renderer;

    BekreftBostedOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public BekreftBostedOppgaveInnholdUtleder(
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
        return "Bostedsadresse";
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
        validerYtelsetype(oppgave.getYtelsetype());

        OppgavetypeDataDto dto = OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());

        LocalDate fom;
        LocalDate tom;
        String fritekst;
        BostedsvilkårIkkeOppfyltÅrsak årsak;
        BostedsavklaringKildeType kilde;
        String kildeFritekst;

        switch (dto) {
            case BekreftBostedOppgavetypeDataDto bundet -> {
                fom = bundet.fom();
                tom = bundet.tom();
                fritekst = bundet.ikkeOppfyltÅrsakFritekstbeskrivelse();
                årsak = bundet.ikkeOppfyltÅrsak();
                kilde = bundet.kilde();
                kildeFritekst = bundet.kildeFritekst();
            }
            case BekreftBostedOpphørOppgavetypeDataDto opphør -> {
                fom = opphør.fom();
                tom = null;
                fritekst = opphør.ikkeOppfyltÅrsakFritekstbeskrivelse();
                årsak = opphør.ikkeOppfyltÅrsak();
                kilde = opphør.kilde();
                kildeFritekst = opphør.kildeFritekst();
            }
            default -> throw new IllegalArgumentException(
                "Ikke støttet oppgavedata for " + OppgaveType.BEKREFT_BOSTED + ": " + dto.getClass().getName());
        }

        return renderer.rendre("tekstfragmenter/bosted/bekreft_bosted",
            byggData(årsak, fom, tom, fritekst, kilde, kildeFritekst, oppgave.getFristTid()));
    }

    private static Map<String, Object> byggData(
        BostedsvilkårIkkeOppfyltÅrsak årsak, LocalDate fom, LocalDate tom, String fritekst,
        BostedsavklaringKildeType kilde, String kildeFritekst, LocalDateTime fristTid
    ) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("årsak", årsak.name());
        data.put("erPeriode", tom != null);
        data.put("fom", fom.toString());
        data.put("tom", tom != null ? tom.toString() : null);
        data.put("annetFritekst", årsak == BostedsvilkårIkkeOppfyltÅrsak.ANNET
            ? (fritekst != null && !fritekst.isBlank() ? fritekst : "Annet.")
            : null);
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
                "BEKREFT_BOSTED støtter kun AKTIVITETSPENGER, fikk ytelsetype=%s".formatted(ytelsetype));
        }
    }
}
