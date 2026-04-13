package no.nav.ung.brukerdialog.web.server.abac;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import no.nav.k9.felles.sikkerhet.abac.*;
import no.nav.sif.abac.kontrakt.abac.ResourceType;
import no.nav.sif.abac.kontrakt.abac.dto.PersonerOperasjonDto;

import java.util.Set;

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
        Set<ResourceType> typerSomKanVæreUtenBehandlingstilknytning = Set.of(ResourceType.APPLIKASJON, ResourceType.DRIFT);
        PersonerOperasjonDto tilgangskontrollInput = PdpRequestMapper.map(pdpRequest);
        if (typerSomKanVæreUtenBehandlingstilknytning.contains(tilgangskontrollInput.operasjon().resource())) {
            var resultat = sifAbacPdpRestKlient.sjekkTilgangTilOperasjon(tilgangskontrollInput.operasjon());
            return new Tilgangsbeslutning(
                resultat.harTilgang(),
                pdpRequest,
                TilgangType.INTERNBRUKER
            );
        } else {
            var resultat = sifAbacPdpRestKlient.sjekkTilgangForInnloggetBruker(tilgangskontrollInput);
            return new Tilgangsbeslutning(
                resultat.harTilgang(),
                pdpRequest,
                pdpRequest.getResourceType().equals(BeskyttetRessursResourceType.TOKENX_RESOURCE) ?
                    TilgangType.EKSTERNBRUKER : TilgangType.INTERNBRUKER,
                new BerørtePersonerForAuditlogg(pdpRequest.getFødselsnumre(), pdpRequest.getAktørIder())
            );
        }



    }
}

