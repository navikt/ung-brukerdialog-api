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
 * {@link EndretPeriodeDataDto#endringer()} avgjør hvilket av fire narrativ som vises, gjenskapt
 * fra frontendens egen forgrening i {@code sif-api/src/api/parse-utils/parseOppgaverElement.ts}:
 * <ul>
 *   <li>{@code {ENDRET_STARTDATO}} - samme tekst som {@link EndretStartdatoOppgaveDokumentUtleder},
 *       hentet fra samme sted ({@link OppgaveDokumentTekster}) slik at teksten ikke kan drifte.</li>
 *   <li>{@code {ENDRET_SLUTTDATO}} - samme tekst som {@link EndretSluttdatoOppgaveDokumentUtleder}
 *       (inkl. «meldt ut»-varianten når forrige tomdato manglet).</li>
 *   <li>{@code {FJERNET_PERIODE}} - «stans»-narrativ, ny tekst kuratert fra
 *       {@code oppgavepaneler/fjernet-periode/i18n/nb.ts}.</li>
 *   <li>{@code {ENDRET_STARTDATO, ENDRET_SLUTTDATO}} - «ny periode»-narrativ, kuratert fra
 *       {@code oppgavepaneler/endret-start-og-sluttdato/i18n/nb.ts}.</li>
 * </ul>
 * <b>Fallback (bevisst avvik fra frontend):</b> enhver annen/uventet kombinasjon - blant annet
 * {@code ANDRE_ENDRINGER}, som frontend ikke har tekst for og som får
 * {@code parseOppgaverElement.ts} til å kaste et vanlig unntak. Det er greit for en
 * frontend-rendering (oppgaven vises da bare ikke), men journalføring MÅ likevel produsere et
 * gyldig, arkiverbart dokument - se {@link #bestemGren} og {@code typer/endret-periode.hbs}.
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
     * Se klasse-javadoc. Krever ikke bare riktig {@code endringer}-kombinasjon, men også at de
     * tilhørende datoene faktisk finnes - en gjenkjent kombinasjon med manglende datoer havner i
     * fallback-grenen i stedet for å risikere en {@code NullPointerException} eller en
     * misvisende, delvis utfylt setning.
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
