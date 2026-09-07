package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.endretperiode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.EndretPeriodeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.PeriodeDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.PeriodeEndringType;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.OppgaveTekster;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@OppgaveTypeRef(OppgaveType.BEKREFT_ENDRET_PERIODE)
@ApplicationScoped
public class EndretPeriodeOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private enum GrenType {STARTDATO, SLUTTDATO, FJERNET, START_OG_SLUTT, FALLBACK_UKJENT_KOMBINASJON}

    private record Gren(GrenType type, boolean erMeldtUt) {
    }

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;

    EndretPeriodeOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public EndretPeriodeOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl
    ) {
        this.mappere = mappere;
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
    }

    @Override
    public String undertittel(BrukerdialogOppgaveEntitet oppgave) {
        Gren gren = bestemGren(hentDto(oppgave));
        return switch (gren.type()) {
            case STARTDATO -> OppgaveTekster.endretStartdatoTittel();
            case SLUTTDATO -> OppgaveTekster.endretSluttdatoTittel(gren.erMeldtUt());
            case FJERNET -> OppgaveTekster.fjernetPeriodeTittel();
            case START_OG_SLUTT -> OppgaveTekster.endretStartOgSluttdatoTittel();
            case FALLBACK_UKJENT_KOMBINASJON -> OppgaveTekster.ukjentPeriodeendringTittel();
        };
    }

    @Override
    public List<OppgaveTekst> egneTekster(BrukerdialogOppgaveEntitet oppgave) {
        EndretPeriodeDataDto dto = hentDto(oppgave);
        Gren gren = bestemGren(dto);
        OppgaveYtelsetype ytelsetype = oppgave.getYtelsetype();
        LocalDateTime fristTid = oppgave.getFristTid();
        PeriodeDTO ny = dto.nyPeriode();
        PeriodeDTO forrige = dto.forrigePeriode();

        return switch (gren.type()) {
            case STARTDATO -> OppgaveTekster.endretStartdatoInnhold(
                ny.getFomDato(), forrige.getFomDato(), ytelsetype, fristTid);
            case SLUTTDATO -> OppgaveTekster.endretSluttdatoInnhold(
                ny.getTomDato(), forrige != null ? forrige.getTomDato() : null, ytelsetype, fristTid);
            case FJERNET -> OppgaveTekster.fjernetPeriodeInnhold(ytelsetype, fristTid);
            case START_OG_SLUTT -> OppgaveTekster.endretStartOgSluttdatoInnhold(
                ny.getFomDato(), ny.getTomDato(), ytelsetype, fristTid);
            case FALLBACK_UKJENT_KOMBINASJON -> OppgaveTekster.ukjentPeriodeendringInnhold(
                ny != null ? ny.getFomDato() : null, ny != null ? ny.getTomDato() : null, ytelsetype, fristTid);
        };
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return ungdomsprogramytelsenDeltakerBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
    }

    private Gren bestemGren(EndretPeriodeDataDto dto) {
        Set<PeriodeEndringType> endringer = dto.endringer();
        PeriodeDTO ny = dto.nyPeriode();
        PeriodeDTO forrige = dto.forrigePeriode();

        if (ny != null && ny.getFomDato() != null && forrige != null && forrige.getFomDato() != null
            && endringer.equals(Set.of(PeriodeEndringType.ENDRET_STARTDATO))) {
            return new Gren(GrenType.STARTDATO, false);
        }
        if (ny != null && ny.getTomDato() != null
            && endringer.equals(Set.of(PeriodeEndringType.ENDRET_SLUTTDATO))) {
            boolean erMeldtUt = forrige == null || forrige.getTomDato() == null;
            return new Gren(GrenType.SLUTTDATO, erMeldtUt);
        }
        if (endringer.equals(Set.of(PeriodeEndringType.FJERNET_PERIODE))) {
            return new Gren(GrenType.FJERNET, false);
        }
        if (ny != null && ny.getFomDato() != null && ny.getTomDato() != null
            && endringer.equals(Set.of(PeriodeEndringType.ENDRET_STARTDATO, PeriodeEndringType.ENDRET_SLUTTDATO))) {
            return new Gren(GrenType.START_OG_SLUTT, false);
        }
        // Fallback: bl.a. ANDRE_ENDRINGER, en uventet/udokumentert kombinasjon, eller en
        // ellers gjenkjent kombinasjon med manglende datoer.
        return new Gren(GrenType.FALLBACK_UKJENT_KOMBINASJON, false);
    }

    private EndretPeriodeDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (EndretPeriodeDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
