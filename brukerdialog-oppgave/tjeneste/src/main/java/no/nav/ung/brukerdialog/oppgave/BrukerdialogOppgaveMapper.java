package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@ApplicationScoped
public class BrukerdialogOppgaveMapper {

    private static final Logger log = LoggerFactory.getLogger(BrukerdialogOppgaveMapper.class);

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private Instance<OppgaveInnholdUtleder> innholdUtledere;

    public BrukerdialogOppgaveMapper() {
        // CDI proxy
    }

    @Inject
    public BrukerdialogOppgaveMapper(@Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
                                      @Any Instance<OppgaveInnholdUtleder> innholdUtledere) {
        this.mappere = mappere;
        this.innholdUtledere = innholdUtledere;
    }

    public BrukerdialogOppgaveDto tilDto(BrukerdialogOppgaveEntitet oppgave) {
        var oppgavetypeData = OppgaveDataMapperFraEntitetTilDto.finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
        OppgaveInnhold innhold = innhold(oppgave);

        return new BrukerdialogOppgaveDto(
            oppgave.getOppgavereferanse(),
            oppgave.getOppgaveType(),
            oppgavetypeData,
            oppgave.getYtelsetype(),
            oppgave.getRespons(),
            oppgave.getStatus(),
            toZonedDateTime(oppgave.getOpprettetTidspunkt()),
            toZonedDateTime(oppgave.getLøstDato()),
            toZonedDateTime(oppgave.getFristTid()),
            innhold.tekster(),
            innhold.undertittel()
        );
    }

    private record OppgaveInnhold(List<OppgaveTekst> tekster, String undertittel) {
    }

    /**
     * Degraderer til tom tekstliste/{@code null}-undertittel ved feil, i stedet for å la hele
     * {@code GET /oppgave/hent/alle} feile pga. én oppgave med f.eks. korrupt oppgavedata - se
     * Fase 3-review i plansporet for denne endringen. Loggmeldingen inneholder bevisst verken
     * fnr/navn eller annet oppgaveinnhold, kun oppgavetype og -referanse.
     */
    private OppgaveInnhold innhold(BrukerdialogOppgaveEntitet oppgave) {
        try {
            OppgaveInnholdUtleder utleder = OppgaveInnholdUtleder.finnUtleder(innholdUtledere, oppgave.getOppgaveType());
            return new OppgaveInnhold(utleder.tekster(oppgave), utleder.undertittel(oppgave));
        } catch (RuntimeException e) {
            log.warn("Klarte ikke å utlede tekster/undertittel for oppgave (oppgaveType={}, oppgaveReferanse={}) - returnerer tom liste/null",
                oppgave.getOppgaveType(), oppgave.getOppgavereferanse(), e);
            return new OppgaveInnhold(List.of(), null);
        }
    }

    private ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault());
    }
}

