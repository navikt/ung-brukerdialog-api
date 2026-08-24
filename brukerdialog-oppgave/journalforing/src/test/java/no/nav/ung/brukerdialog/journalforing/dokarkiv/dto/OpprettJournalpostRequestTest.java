package no.nav.ung.brukerdialog.journalforing.dokarkiv.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class OpprettJournalpostRequestTest {

    private static final OpprettJournalpostRequest.AvsenderMottaker AVSENDER_MOTTAKER =
        new OpprettJournalpostRequest.AvsenderMottaker("11111111111", IdType.FNR);
    private static final OpprettJournalpostRequest.Bruker BRUKER =
        new OpprettJournalpostRequest.Bruker("11111111111", IdType.FNR);
    private static final List<OpprettJournalpostRequest.Dokument> DOKUMENTER = List.of(
        new OpprettJournalpostRequest.Dokument(
            "En tittel",
            "FVL 04-16.0",
            List.of(OpprettJournalpostRequest.DokumentVariant.arkivPdf(new byte[]{1, 2, 3}))
        )
    );

    @Test
    void skal_ikke_inneholde_bruker_avsenderMottaker_eller_dokumenter_i_toString() {
        var request = new OpprettJournalpostRequest(
            JournalpostType.UTGAAENDE,
            AVSENDER_MOTTAKER,
            BRUKER,
            "UNG",
            "En tittel",
            "9999",
            "test-ekstern-referanse-id",
            List.of(new OpprettJournalpostRequest.Tilleggsopplysning("ung.oppgave.eRef", "test-ekstern-referanse-id")),
            OpprettJournalpostRequest.Sak.forFagsak("ABC123", "UNG_SAK"),
            DOKUMENTER
        );

        String toString = request.toString();

        assertThat(toString)
            .contains("UTGAAENDE", "UNG", "9999", "test-ekstern-referanse-id", "FAGSAK", "ABC123", "UNG_SAK")
            // fnr - dukker kun opp i bruker/avsenderMottaker
            .doesNotContain("11111111111")
            // felt som kun finnes på Dokument/DokumentVariant - dukker kun opp om dokumenter var med
            .doesNotContain("brevkode", "dokumentvarianter", "fysiskDokument");
    }

    @Test
    void skal_feile_ved_manglende_paakrevde_felt() {
        assertThatThrownBy(() -> new OpprettJournalpostRequest(
            null, AVSENDER_MOTTAKER, BRUKER, "UNG", "tittel", "9999", "eRef",
            List.of(), OpprettJournalpostRequest.Sak.GENERELL_SAK, DOKUMENTER
        )).isInstanceOf(NullPointerException.class).hasMessageContaining("journalpostType");

        assertThatThrownBy(() -> new OpprettJournalpostRequest(
            JournalpostType.UTGAAENDE, AVSENDER_MOTTAKER, BRUKER, "UNG", "tittel", "9999", "eRef",
            List.of(), OpprettJournalpostRequest.Sak.GENERELL_SAK, null
        )).isInstanceOf(NullPointerException.class).hasMessageContaining("dokumenter");
    }

    @Test
    void generell_sak_har_ingen_fagsakId_eller_fagsaksystem() {
        var sak = OpprettJournalpostRequest.Sak.GENERELL_SAK;

        assertThat(sak.sakstype()).isEqualTo("GENERELL_SAK");
        assertThat(sak.fagsakId()).isNull();
        assertThat(sak.fagsaksystem()).isNull();
    }

    @Test
    void fagsak_har_sakstype_fagsak_og_gitt_fagsakId_og_fagsaksystem() {
        var sak = OpprettJournalpostRequest.Sak.forFagsak("ABC123", "UNG_SAK");

        assertThat(sak.sakstype()).isEqualTo("FAGSAK");
        assertThat(sak.fagsakId()).isEqualTo("ABC123");
        assertThat(sak.fagsaksystem()).isEqualTo("UNG_SAK");
    }

    @Test
    void dokumentvariant_arkivPdf_og_originalJson_har_forventet_filtype_og_variantformat() {
        var pdf = OpprettJournalpostRequest.DokumentVariant.arkivPdf(new byte[]{1});
        var json = OpprettJournalpostRequest.DokumentVariant.originalJson(new byte[]{2});

        assertThat(pdf.filtype()).isEqualTo("PDFA");
        assertThat(pdf.variantformat()).isEqualTo("ARKIV");
        assertThat(json.filtype()).isEqualTo("JSON");
        assertThat(json.variantformat()).isEqualTo("ORIGINAL");
    }
}
