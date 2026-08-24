package no.nav.ung.brukerdialog.journalforing.dokarkiv.dto;

import java.util.List;

/**
 * Respons fra Dokarkivs {@code opprettJournalpost}. {@code journalpostferdigstilt} er
 * {@code false} når journalposten er opprettet, men ferdigstilling ikke lot seg gjøre til
 * tross for {@code forsoekFerdigstill=true}.
 */
public record OpprettJournalpostResponse(
    String journalpostId,
    List<Dokument> dokumenter,
    boolean journalpostferdigstilt,
    String melding
) {
    public record Dokument(String dokumentInfoId) {}
}
