package no.nav.ung.brukerdialog.kontrakt.oppgaver.journalforing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import no.nav.ung.brukerdialog.typer.Saksnummer;

/**
 * Journalføringsrelaterte felter som kan oppgis ved opprettelse av en brukerdialogoppgave.
 * <p>
 * Tema og fagsaksystem sendes <b>ikke</b> inn her - de utledes fra oppgavens
 * {@code ytelsetype} (se {@code JournalføringParametre.utled(...)} i tjeneste-modulen).
 * Kalleren oppgir kun {@code fagsakId}, og kun når oppgavetypen krever det
 * (se {@link GyldigJournalføring}).
 * <p>
 * Beholdt som egen wrapper selv om den i dag kun har ett felt: støtte for K9 som
 * fagsaksystem er utsatt, ikke avlyst, og kan senere kreve flere felter her uten at
 * {@code OpprettOppgaveDto} må endres.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JournalføringDto(

    @JsonProperty(value = "fagsakId")
    @Valid
    Saksnummer fagsakId
) {
}
