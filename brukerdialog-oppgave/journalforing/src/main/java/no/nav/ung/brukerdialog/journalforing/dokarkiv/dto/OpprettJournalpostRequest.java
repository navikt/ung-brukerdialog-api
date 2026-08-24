package no.nav.ung.brukerdialog.journalforing.dokarkiv.dto;

import java.util.List;
import java.util.Objects;

/**
 * Payload for Dokarkivs {@code opprettJournalpost}. Generisk mot Dokarkivs
 * wire-format - kjenner bevisst ikke til denne applikasjonens egne enums ({@code Tema},
 * {@code Fagsaksystem}, {@code Sakstype}), som lever i {@code brukerdialog-oppgave-tjeneste}.
 * Kalleren oversetter til de rå verdiene Dokarkiv forventer.
 *
 * <p>{@code kanal} og {@code behandlingstema} er bevisst utelatt: {@code kanal} er kun påkrevd
 * for {@link JournalpostType#INNGAAENDE}, som denne applikasjonen aldri sender.
 */
public record OpprettJournalpostRequest(
    JournalpostType journalpostType,
    AvsenderMottaker avsenderMottaker,
    Bruker bruker,
    String tema,
    String tittel,
    String journalfoerendeEnhet,
    String eksternReferanseId,
    List<Tilleggsopplysning> tilleggsopplysninger,
    Sak sak,
    List<Dokument> dokumenter
) {
    public OpprettJournalpostRequest {
        Objects.requireNonNull(journalpostType, "journalpostType");
        Objects.requireNonNull(avsenderMottaker, "avsenderMottaker");
        Objects.requireNonNull(bruker, "bruker");
        Objects.requireNonNull(tema, "tema");
        Objects.requireNonNull(sak, "sak");
        Objects.requireNonNull(dokumenter, "dokumenter");
    }

    /**
     * PII-fri. Utelater bevisst {@code bruker}, {@code avsenderMottaker} og {@code dokumenter}
     * (inneholder fødselsnummer, navn og PDF-/JSON-bytes). Denne
     * representasjonen skal aldri utvides til å inkludere de feltene.
     */
    @Override
    public String toString() {
        return "OpprettJournalpostRequest{" +
            "journalpostType=" + journalpostType +
            ", tema='" + tema + '\'' +
            ", journalfoerendeEnhet='" + journalfoerendeEnhet + '\'' +
            ", eksternReferanseId='" + eksternReferanseId + '\'' +
            ", sak=" + sak +
            ", tilleggsopplysninger=" + tilleggsopplysninger +
            '}';
    }

    /**
     * Mottaker av det utgående dokumentet. {@code navn} er bevisst ikke et felt her -
     * Dokarkiv slår selv opp navn i PDL ved journalføring.
     */
    public record AvsenderMottaker(String id, IdType idType) {
        public AvsenderMottaker {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(idType, "idType");
        }
    }

    public record Bruker(String id, IdType idType) {
        public Bruker {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(idType, "idType");
        }
    }

    /**
     * Sakstilknytning. Enten en fagsak (via {@link #forFagsak}) eller
     * {@link #GENERELL_SAK} når oppgaven ikke har noen fagsaktilknytning ved opprettelse
     * (sakstype {@code SØK_YTELSE}).
     */
    public record Sak(String sakstype, String fagsakId, String fagsaksystem) {
        public static final Sak GENERELL_SAK = new Sak("GENERELL_SAK", null, null);

        public static Sak forFagsak(String fagsakId, String fagsaksystem) {
            Objects.requireNonNull(fagsakId, "fagsakId");
            Objects.requireNonNull(fagsaksystem, "fagsaksystem");
            return new Sak("FAGSAK", fagsakId, fagsaksystem);
        }
    }

    public record Tilleggsopplysning(String nokkel, String verdi) {
        public Tilleggsopplysning {
            Objects.requireNonNull(nokkel, "nokkel");
            Objects.requireNonNull(verdi, "verdi");
        }
    }

    public record Dokument(String tittel, String brevkode, List<DokumentVariant> dokumentvarianter) {
        public Dokument {
            Objects.requireNonNull(tittel, "tittel");
            Objects.requireNonNull(brevkode, "brevkode");
            Objects.requireNonNull(dokumentvarianter, "dokumentvarianter");
        }
    }

    /**
     * {@code fysiskDokument} er dokumentbytes (PDF eller JSON). Utelates bevisst fra
     * {@code toString()} på {@link Dokument} sitt omsluttende {@link OpprettJournalpostRequest}
     * - se klassekommentaren.
     */
    public record DokumentVariant(String filtype, String variantformat, byte[] fysiskDokument) {
        public DokumentVariant {
            Objects.requireNonNull(filtype, "filtype");
            Objects.requireNonNull(variantformat, "variantformat");
            Objects.requireNonNull(fysiskDokument, "fysiskDokument");
        }

        public static DokumentVariant arkivPdf(byte[] pdf) {
            return new DokumentVariant("PDFA", "ARKIV", pdf);
        }

        public static DokumentVariant originalJson(byte[] json) {
            return new DokumentVariant("JSON", "ORIGINAL", json);
        }
    }
}
