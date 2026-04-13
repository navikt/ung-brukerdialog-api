package no.nav.ung.brukerdialog.web.server.abac;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.k9.felles.integrasjon.rest.OidcRestClient;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.sif.abac.kontrakt.abac.dto.OperasjonDto;
import no.nav.sif.abac.kontrakt.abac.dto.PersonerOperasjonDto;
import no.nav.sif.abac.kontrakt.abac.resultat.Tilgangsbeslutning;

import java.net.URI;
import java.net.URISyntaxException;

@Dependent
@ScopedRestIntegration(scopeKey = "sif.abac.pdp.scope", defaultScope = "api://prod-gcp.k9saksbehandling.sif-abac-pdp/.default")
public class SifAbacPdpRestKlient {

    private OidcRestClient restClient;
    private URI uriTilgangskontrollPersoner;
    private URI uriTilgangskontrollOperasjon;

    SifAbacPdpRestKlient() {
        // for CDI proxy
    }

    @Inject
    public SifAbacPdpRestKlient(OidcRestClient restClient,
                                @KonfigVerdi(value = "sif.abac.pdp.url", defaultVerdi = "http://sif-abac-pdp/sif/sif-abac-pdp/api/tilgangskontroll/v2/ung") String urlSifAbacPdp) {
        this.restClient = restClient;
        this.uriTilgangskontrollPersoner = tilUri(urlSifAbacPdp, "personer");
        this.uriTilgangskontrollOperasjon = tilUri(urlSifAbacPdp, "operasjon");

    }

    public Tilgangsbeslutning sjekkTilgangForInnloggetBruker(PersonerOperasjonDto input) {
        return restClient.post(uriTilgangskontrollPersoner, input, Tilgangsbeslutning.class);
    }

    public Tilgangsbeslutning sjekkTilgangTilOperasjon(OperasjonDto input) {
        return restClient.post(uriTilgangskontrollOperasjon, input, Tilgangsbeslutning.class);
    }


    private static URI tilUri(String baseUrl, String path) {
        try {
            return new URI(baseUrl + "/" + path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Ugyldig konfigurasjon for sif.abac.pdp.url", e);
        }
    }

}
