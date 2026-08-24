package no.nav.ung.brukerdialog.journalforing.dokarkiv;

import java.net.URI;
import java.net.URISyntaxException;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.k9.felles.integrasjon.rest.OidcRestClient;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.journalforing.dokarkiv.dto.OpprettJournalpostRequest;
import no.nav.ung.brukerdialog.journalforing.dokarkiv.dto.OpprettJournalpostResponse;

/**
 * Auth mot Dokarkiv er Azure AD {@code client_credentials} - journalføringen skjer
 * fra en {@code ProsessTask} uten brukerkontekst.
 */
@Dependent
@ScopedRestIntegration(scopeKey = "DOKARKIV_SCOPE", defaultScope = "api://prod-fss.teamdokumenthandtering.dokarkiv/.default")
public class DokArkivKlientImpl implements DokArkivKlient {

    private final OidcRestClient restClient;
    private final URI forsøkFerdigstillUrl;
    private final URI opprettJournalpostUrl;

    @Inject
    public DokArkivKlientImpl(
        OidcRestClient restClient,
        @KonfigVerdi(value = "DOKARKIV_URL") String dokarkivUrl) {
        this.restClient = restClient;

        URI journalpostUrl = tilUri(dokarkivUrl, "journalpost");
        this.forsøkFerdigstillUrl = UriBuilder.fromUri(journalpostUrl)
            .queryParam("forsoekFerdigstill", true)
            .build();
        this.opprettJournalpostUrl = UriBuilder.fromUri(journalpostUrl)
            .queryParam("forsoekFerdigstill", false)
            .build();
    }

    @Override
    public OpprettJournalpostResponse opprettJournalpostOgFerdigstill(OpprettJournalpostRequest request) {
        return restClient.post(forsøkFerdigstillUrl, request, OpprettJournalpostResponse.class);
    }

    @Override
    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request) {
        return restClient.post(opprettJournalpostUrl, request, OpprettJournalpostResponse.class);
    }

    private static URI tilUri(String baseUrl, String path) {
        try {
            return new URI(baseUrl + "/" + path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Ugyldig konfigurasjon for DOKARKIV_URL", e);
        }
    }
}
