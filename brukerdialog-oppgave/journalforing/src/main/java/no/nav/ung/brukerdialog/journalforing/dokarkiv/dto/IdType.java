package no.nav.ung.brukerdialog.journalforing.dokarkiv.dto;

/**
 * Identifikatortype for {@link OpprettJournalpostRequest.Bruker} og
 * {@link OpprettJournalpostRequest.AvsenderMottaker}. Dokarkiv støtter også {@code ORGNR},
 * {@code HPRNR} og {@code UTL_ORG}, men denne applikasjonen sender kun personer identifisert
 * ved fødselsnummer og modellerer derfor kun {@link #FNR}.
 */
public enum IdType {
    FNR
}
