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
import no.nav.ung.brukerdialog.pdf.NorskDatoFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    public String tittel(BrukerdialogOppgaveEntitet oppgave) {
        return "Bekrefte bosted for aktivitetspenger";
    }

    @Override
    public List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave) {
        OppgavetypeDataDto dto = OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());

        LocalDate fom;
        LocalDate tom;
        boolean erBosattITrondheim;
        String fritekst;
        BostedsvilkårIkkeOppfyltÅrsak årsak;
        BostedsavklaringKildeType kilde;
        String kildeFritekst;

        switch (dto) {
            case BekreftBostedOppgavetypeDataDto bundet -> {
                fom = bundet.fom();
                tom = bundet.tom();
                erBosattITrondheim = bundet.erBosattITrondheim();
                fritekst = bundet.ikkeOppfyltÅrsakFritekstbeskrivelse();
                årsak = bundet.ikkeOppfyltÅrsak();
                kilde = bundet.kilde();
                kildeFritekst = bundet.kildeFritekst();
            }
            case BekreftBostedOpphørOppgavetypeDataDto opphør -> {
                fom = opphør.fom();
                tom = null;
                erBosattITrondheim = opphør.erBosattITrondheim();
                fritekst = opphør.ikkeOppfyltÅrsakFritekstbeskrivelse();
                årsak = opphør.ikkeOppfyltÅrsak();
                kilde = opphør.kilde();
                kildeFritekst = opphør.kildeFritekst();
            }
            default -> throw new IllegalArgumentException(
                "Ikke støttet oppgavedata for " + OppgaveType.BEKREFT_BOSTED + ": " + dto.getClass().getName());
        }

        List<OppgaveTekst> tekster = new ArrayList<>();
        tekster.add(new OppgaveAvsnitt("Du har fått en oppgave om å bekrefte bosted for aktivitetspenger."));
        tekster.add(new OppgaveAvsnitt(tom != null
            ? "Periode: %s til %s.".formatted(NorskDatoFormat.datoLang(fom), NorskDatoFormat.datoLang(tom))
            : "Dette gjelder fra og med %s.".formatted(NorskDatoFormat.datoLang(fom)), true));
        tekster.add(new OppgaveAvsnitt("Bor i Trondheim: " + (erBosattITrondheim ? "Ja" : "Nei")));
        String forklaring = OppgaveTekster.bostedIkkeOppfyltForklaring(årsak, fritekst);
        if (forklaring != null) {
            tekster.add(new OppgaveAvsnitt(forklaring));
        }
        tekster.add(new OppgaveAvsnitt(OppgaveTekster.bostedKildeForklaring(kilde, kildeFritekst)));
        tekster.add(new OppgaveAvsnitt("Du får denne meldingen slik at du kan komme med en tilbakemelding på dette. Du svarer på Min side på nav.no."));
        tekster.add(new OppgaveAvsnitt("Ingen tilbakemelding? Kryss av på \"Nei\" med en gang og send inn svaret ditt. Jo fortere du svarer, jo fortere får vi behandlet saken din."));
        tekster.add(new OppgaveAvsnitt("Har du en tilbakemelding? Ta kontakt med veilederen din først. Når dere har snakket sammen, sender du inn svaret ditt."));
        OppgaveTekster.leggTilSvarfrist(tekster, oppgave.getFristTid(), "svare",
            "Hvis vi ikke hører fra deg innen svarfristen har gått ut, legger vi de registrerte opplysningene til grunn når vi behandler saken din.");
        return tekster;
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return aktivitetspengerInnsynBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
    }
}
