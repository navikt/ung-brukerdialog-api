package no.nav.ung.brukerdialog.journalforing.dokarkiv.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class OpprettJournalpostRequestBuilderTest {

    @Test
    void skal_bygge_request_med_alle_felter_satt() {
        var avsenderMottaker = new OpprettJournalpostRequest.AvsenderMottaker("11111111111", IdType.FNR);
        var bruker = new OpprettJournalpostRequest.Bruker("11111111111", IdType.FNR);
        var tilleggsopplysninger = List.of(new OpprettJournalpostRequest.Tilleggsopplysning("ung.oppgave.eRef", "ref"));
        var sak = OpprettJournalpostRequest.Sak.forFagsak("ABC123", "UNG_SAK");
        var dokumenter = List.of(new OpprettJournalpostRequest.Dokument(
            "tittel", "FVL 04-16.0", List.of(OpprettJournalpostRequest.DokumentVariant.arkivPdf(new byte[]{1}))
        ));

        var request = new OpprettJournalpostRequestBuilder()
            .journalpostType(JournalpostType.UTGAAENDE)
            .avsenderMottaker(avsenderMottaker)
            .bruker(bruker)
            .tema("UNG")
            .tittel("En tittel")
            .journalfoerendeEnhet("9999")
            .eksternReferanseId("ref")
            .tilleggsopplysninger(tilleggsopplysninger)
            .sak(sak)
            .dokumenter(dokumenter)
            .build();

        assertThat(request.journalpostType()).isEqualTo(JournalpostType.UTGAAENDE);
        assertThat(request.avsenderMottaker()).isEqualTo(avsenderMottaker);
        assertThat(request.bruker()).isEqualTo(bruker);
        assertThat(request.tema()).isEqualTo("UNG");
        assertThat(request.tittel()).isEqualTo("En tittel");
        assertThat(request.journalfoerendeEnhet()).isEqualTo("9999");
        assertThat(request.eksternReferanseId()).isEqualTo("ref");
        assertThat(request.tilleggsopplysninger()).isEqualTo(tilleggsopplysninger);
        assertThat(request.sak()).isEqualTo(sak);
        assertThat(request.dokumenter()).isEqualTo(dokumenter);
    }
}
