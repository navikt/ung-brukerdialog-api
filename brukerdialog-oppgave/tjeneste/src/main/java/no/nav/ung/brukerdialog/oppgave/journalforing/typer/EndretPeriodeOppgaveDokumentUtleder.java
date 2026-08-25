package no.nav.ung.brukerdialog.oppgave.journalforing.typer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.EndretPeriodeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.PeriodeDTO;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.PeriodeEndringType;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentTekster;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveDokumentUtleder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * {@link EndretPeriodeDataDto#endringer()} avgjør gren, gjenskapt fra frontendens forgrening i
 * {@code sif-api/src/api/parse-utils/parseOppgaverElement.ts} - se {@link #bestemGren}.
 * <p>
 * <b>Bevisst avvik fra frontend:</b> uventede kombinasjoner (bl.a. {@code ANDRE_ENDRINGER}) får
 * frontend til å kaste et unntak, siden oppgaven da bare ikke vises. Journalføring MÅ likevel
 * produsere et gyldig, arkiverbart dokument - derfor finnes {@code GrenType.UKJENT} som fallback
 * i stedet for en exception.
 */
@OppgaveTypeRef(OppgaveType.BEKREFT_ENDRET_PERIODE)
@ApplicationScoped
public class EndretPeriodeOppgaveDokumentUtleder implements OppgaveDokumentUtleder {

    private enum GrenType {STARTDATO, SLUTTDATO, FJERNET, START_OG_SLUTT, UKJENT}

    private record Gren(GrenType type, boolean erMeldtUt) {
    }

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;

    EndretPeriodeOppgaveDokumentUtleder() {
        // for CDI proxy
    }

    @Inject
    public EndretPeriodeOppgaveDokumentUtleder(@Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere) {
        this.mappere = mappere;
    }

    @Override
    public String utledTittel(BrukerdialogOppgaveEntitet oppgave) {
        Gren gren = bestemGren(hentDto(oppgave));
        OppgaveYtelsetype ytelsetype = oppgave.getYtelsetype();
        return switch (gren.type()) {
            case STARTDATO -> OppgaveDokumentTekster.endretStartdatoTittel(ytelsetype);
            case SLUTTDATO -> OppgaveDokumentTekster.endretSluttdatoTittel(ytelsetype, gren.erMeldtUt());
            case FJERNET -> OppgaveDokumentTekster.fjernetPeriodeTittel(ytelsetype);
            case START_OG_SLUTT -> OppgaveDokumentTekster.endretStartOgSluttdatoTittel(ytelsetype);
            case UKJENT -> OppgaveDokumentTekster.ukjentPeriodeendringTittel(ytelsetype);
        };
    }

    @Override
    public String malnavn() {
        return "typer/endret-periode";
    }

    @Override
    public Map<String, Object> utledInnholdsdata(BrukerdialogOppgaveEntitet oppgave) {
        EndretPeriodeDataDto dto = hentDto(oppgave);
        Gren gren = bestemGren(dto);
        OppgaveYtelsetype ytelsetype = oppgave.getYtelsetype();
        LocalDateTime fristTid = oppgave.getFristTid();
        PeriodeDTO ny = dto.nyPeriode();
        PeriodeDTO forrige = dto.forrigePeriode();

        Map<String, Object> data = switch (gren.type()) {
            case STARTDATO -> OppgaveDokumentTekster.endretStartdatoInnhold(
                ny.getFomDato(), forrige.getFomDato(), ytelsetype, fristTid);
            case SLUTTDATO -> OppgaveDokumentTekster.endretSluttdatoInnhold(
                ny.getTomDato(), forrige != null ? forrige.getTomDato() : null, ytelsetype, fristTid);
            case FJERNET -> OppgaveDokumentTekster.fjernetPeriodeInnhold(ytelsetype, fristTid);
            case START_OG_SLUTT -> OppgaveDokumentTekster.endretStartOgSluttdatoInnhold(
                ny.getFomDato(), ny.getTomDato(), ytelsetype, fristTid);
            case UKJENT -> OppgaveDokumentTekster.ukjentPeriodeendringInnhold(
                ny != null ? ny.getFomDato() : null, ny != null ? ny.getTomDato() : null, ytelsetype, fristTid);
        };
        data.put("periodeEndringType", gren.type().name());
        return data;
    }

    /**
     * Krever både riktig {@code endringer}-kombinasjon og at datoene faktisk finnes - manglende
     * datoer havner i fallback-grenen, ikke en {@code NullPointerException} eller en halvferdig setning.
     */
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
        return new Gren(GrenType.UKJENT, false);
    }

    private EndretPeriodeDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (EndretPeriodeDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
