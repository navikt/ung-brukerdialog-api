package no.nav.ung.brukerdialog.journalforing.dokarkiv.dto;

/**
 * Dokarkivs journalposttyper. Denne applikasjonen sender i dag kun {@link #UTGAAENDE},
 * men alle tre er modellert for å speile Dokarkivs faktiske API.
 */
public enum JournalpostType {
    INNGAAENDE,
    UTGAAENDE,
    NOTAT
}
