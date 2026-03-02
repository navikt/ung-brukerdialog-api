package no.nav.ung.brukerdialog.web.server.abac;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import no.nav.k9.felles.sikkerhet.abac.*;
import no.nav.sif.abac.kontrakt.abac.dto.PersonerOperasjonDto;
import no.nav.sif.abac.kontrakt.abac.dto.SaksinformasjonOgPersonerTilgangskontrollInputDto;

import java.util.Collections;

@Dependent
@Alternative
@Priority(1)
public class AppPdpKlient implements PdpKlient {

    private final SifAbacPdpRestKlient sifAbacPdpRestKlient;

    @Inject
    public AppPdpKlient(SifAbacPdpRestKlient sifAbacPdpRestKlient) {
        this.sifAbacPdpRestKlient = sifAbacPdpRestKlient;
    }

    @Override
    public Tilgangsbeslutning forespørTilgang(PdpRequest pdpRequest) {
        PersonerOperasjonDto tilgangskontrollInput = PdpRequestMapper.map(pdpRequest);
        no.nav.sif.abac.kontrakt.abac.resultat.Tilgangsbeslutning resultat = sifAbacPdpRestKlient.sjekkTilgangForInnloggetBruker(tilgangskontrollInput);
        return new Tilgangsbeslutning(
            resultat.harTilgang(),
            pdpRequest,
            pdpRequest.getResourceType().equals(BeskyttetRessursResourceType.TOKENX_RESOURCE) ?
                TilgangType.EKSTERNBRUKER : TilgangType.INTERNBRUKER,
            new BerørtePersonerForAuditlogg(pdpRequest.getFødselsnumre(), pdpRequest.getAktørIder())
        );
    }
}

