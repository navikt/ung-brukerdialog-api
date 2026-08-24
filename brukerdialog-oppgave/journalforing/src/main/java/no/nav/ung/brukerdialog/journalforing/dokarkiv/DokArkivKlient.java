package no.nav.ung.brukerdialog.journalforing.dokarkiv;

import no.nav.ung.brukerdialog.journalforing.dokarkiv.dto.OpprettJournalpostRequest;
import no.nav.ung.brukerdialog.journalforing.dokarkiv.dto.OpprettJournalpostResponse;

/**
 * Klient mot Dokarkivs {@code journalpostapi}. Se
 * <a href="https://confluence.adeo.no/display/BOA/opprettJournalpost">opprettJournalpost</a>.
 */
public interface DokArkivKlient {

    /**
     * Oppretter journalpost og forsøker å ferdigstille den i samme kall
     * ({@code forsoekFerdigstill=true}). Dette er metoden som brukes av
     * {@code JournalførOppgaveTask}.
     */
    OpprettJournalpostResponse opprettJournalpostOgFerdigstill(OpprettJournalpostRequest request);

    /**
     * Oppretter journalpost uten forsøk på ferdigstilling ({@code forsoekFerdigstill=false}).
     * Ikke i bruk i dagens flyt, men en del av Dokarkivs API og tatt med for et komplett klient-
     * grensesnitt.
     */
    OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request);
}
