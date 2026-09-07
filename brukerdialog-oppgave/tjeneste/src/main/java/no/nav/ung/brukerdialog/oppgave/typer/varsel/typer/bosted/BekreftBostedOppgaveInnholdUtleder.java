package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bosted;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Kilde: fagside-levert tekst-/formateringstabell (se plansporet), erstatter tidligere
 * frontend-avledet tekst (ingen forelegg i {@code sif-brukerdialog}). Ingen ytelseskvalifikator -
 * gjelder alltid aktivitetspenger.
 * <p>
 * To DTO-varianter fra vår mapper: bundet periode ({@link BekreftBostedOppgavetypeDataDto},
 * {@code fom}-{@code tom}) og åpen/opphørt periode ({@link BekreftBostedOpphørOppgavetypeDataDto},
 * kun {@code fom}) - se {@link OppgaveTekster#bostedVarselTekst}.
 */
@OppgaveTypeRef(OppgaveType.BEKREFT_BOSTED)
@ApplicationScoped
public class BekreftBostedOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String aktivitetspengerInnsynBaseUrl;

    BekreftBostedOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public BekreftBostedOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "AKTIVITETSPENGER_INNSYN_BASE_URL") String aktivitetspengerInnsynBaseUrl
    ) {
        this.mappere = mappere;
        this.aktivitetspengerInnsynBaseUrl = aktivitetspengerInnsynBaseUrl;
    }

    @Override
    public String undertittel(BrukerdialogOppgaveEntitet oppgave) {
        return "Bostedsadresse";
    }

    @Override
    public List<OppgaveTekst> egneTekster(BrukerdialogOppgaveEntitet oppgave) {
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

        List<OppgaveTekst> tekster = new ArrayList<>();
        tekster.add(new OppgaveAvsnitt(OppgaveTekster.bostedVarselTekst(årsak, fom, tom)));
        String annetFritekst = OppgaveTekster.bostedAnnetFritekst(årsak, fritekst);
        if (annetFritekst != null) {
            tekster.add(new OppgaveAvsnitt(annetFritekst));
        }
        tekster.add(OppgaveTekster.bostedKildeAvsnitt(kilde, kildeFritekst));
        OppgaveTekster.leggTilSvarfrist(tekster, oppgave.getFristTid(), "svare",
            "Hvis vi ikke hører fra deg innen svarfristen har gått ut, legger vi de registrerte opplysningene til grunn når vi behandler saken din.");
        return tekster;
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return aktivitetspengerInnsynBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
    }
}
