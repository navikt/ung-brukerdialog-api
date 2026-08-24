package no.nav.ung.brukerdialog.oppgave.journalforing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import no.nav.k9.felles.exception.HttpStatuskodeException;
import no.nav.k9.felles.integrasjon.dokarkiv.DokarkivKlient;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.AvsluttSakRequest;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.GjenaapneSakRequest;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.OpprettJournalpostRequest;
import no.nav.k9.felles.integrasjon.dokarkiv.dto.OpprettJournalpostResponse;

/**
 * In-memory fake for {@link DokarkivKlient}, til bruk i tester av {@code JournalførOppgaveTask}.
 * Registrerer alle mottatte requester slik at en test kan verifisere
 * hva som faktisk ble sendt (tema, sak, dokumentvarianter osv.) uten en ekte HTTP-server, og lar
 * testen styre om svaret skal være suksess, 409 eller et transient 5xx.
 */
public class DokArkivKlientFake implements DokarkivKlient {

    private final List<OpprettJournalpostRequest> mottatteRequester = new ArrayList<>();
    private Function<OpprettJournalpostRequest, OpprettJournalpostResponse> svarfunksjon = req -> {
        throw new IllegalStateException("DokArkivKlientFake er ikke konfigurert med noe svar - se svarMed*(...)");
    };

    @Override
    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request) {
        mottatteRequester.add(request);
        return svarfunksjon.apply(request);
    }

    @Override
    public void avsluttSak(AvsluttSakRequest request) {
        throw new UnsupportedOperationException("DokArkivKlientFake støtter ikke avsluttSak - ikke brukt av JournalførOppgaveTask");
    }

    @Override
    public void gjenaapneSak(GjenaapneSakRequest request) {
        throw new UnsupportedOperationException("DokArkivKlientFake støtter ikke gjenaapneSak - ikke brukt av JournalførOppgaveTask");
    }

    /** OK-respons: journalpost opprettet og ferdigstilt med gitt journalpostId. */
    public void svarMedOk(String journalpostId) {
        svarfunksjon = req -> new OpprettJournalpostResponse(journalpostId, List.of(), true, null);
    }

    /** Journalpost opprettet, men IKKE ferdigstilt til tross for {@code forsoekFerdigstill=true}. */
    public void svarMedOpprettetIkkeFerdigstilt(String journalpostId, String melding) {
        svarfunksjon = req -> new OpprettJournalpostResponse(journalpostId, List.of(), false, melding);
    }

    /**
     * Simulerer 409 (duplikat på {@code eksternReferanseId}).
     * Responsbody kan i praksis ikke leses av dagens {@code OidcRestClient} - faken kaster derfor
     * samme eksakte unntakstype som produksjonskoden må håndtere, uten body.
     */
    public void svarMed409() {
        svarfunksjon = req -> {
            throw new HttpStatuskodeException("409", "Conflict", 409);
        };
    }

    /** Simulerer et transient serverfeil (5xx) - skal propagere slik at prosesstasken retryer. */
    public void svarMedFeil(int httpStatuskode) {
        svarfunksjon = req -> {
            throw new HttpStatuskodeException(Integer.toString(httpStatuskode), "Feil", httpStatuskode);
        };
    }

    /** Full kontroll, f.eks. for å verifisere requesten før svaret bestemmes. */
    public void svar(Function<OpprettJournalpostRequest, OpprettJournalpostResponse> svarfunksjon) {
        this.svarfunksjon = svarfunksjon;
    }

    public List<OpprettJournalpostRequest> getMottatteRequester() {
        return List.copyOf(mottatteRequester);
    }

    public OpprettJournalpostRequest getSisteRequest() {
        if (mottatteRequester.isEmpty()) {
            throw new IllegalStateException("Ingen requester er mottatt ennå");
        }
        return mottatteRequester.get(mottatteRequester.size() - 1);
    }
}
