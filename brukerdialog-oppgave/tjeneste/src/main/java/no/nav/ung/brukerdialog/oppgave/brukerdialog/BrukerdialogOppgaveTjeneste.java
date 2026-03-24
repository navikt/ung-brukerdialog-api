package no.nav.ung.brukerdialog.oppgave.brukerdialog;


import no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveResponsDto;
import no.nav.ung.brukerdialog.typer.AktørId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface som definerer handlinger på oppgaver som er tilgjengelig for bruker.
 */
public interface BrukerdialogOppgaveTjeneste {

    /**
     * Henter alle oppgaver for en gitt aktør.
     *
     * @param aktørId aktørId for bruker
     * @param ytelsetype valgfritt filter på ytelsetype
     * @return liste med oppgaver
     */
    List<BrukerdialogOppgaveDto> hentAlleOppgaverForAktør(AktørId aktørId, OppgaveYtelsetype ytelsetype);

    /**
     * Henter en spesifikk oppgave basert på oppgaveReferanse og aktørId.
     *
     * @param oppgavereferanse unik referanse til oppgaven
     * @param aktørId aktørId for bruker
     * @return oppgaven som DTO
     * @throws IllegalArgumentException hvis oppgaven ikke finnes
     */
    BrukerdialogOppgaveDto hentOppgaveForOppgavereferanse(UUID oppgavereferanse, AktørId aktørId);

    /**
     * Løser en oppgave basert på oppgaveReferanse.
     *
     * @param oppgavereferanse unik referanse til oppgaven
     * @param aktørId aktørId for bruker
     * @return den løste oppgaven som DTO
     * @throws IllegalArgumentException hvis oppgaven ikke finnes
     */
    BrukerdialogOppgaveDto løsOppgave(UUID oppgavereferanse, AktørId aktørId, Optional<OppgaveResponsDto> oppgaveResponsDto);
}
