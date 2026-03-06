package no.nav.ung.brukerdialog.web.server.abac;

import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionType;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursResourceType;
import no.nav.k9.felles.sikkerhet.abac.PdpRequest;
import no.nav.sif.abac.kontrakt.abac.BeskyttetRessursActionAttributt;
import no.nav.sif.abac.kontrakt.abac.ResourceType;
import no.nav.sif.abac.kontrakt.abac.dto.OperasjonDto;
import no.nav.sif.abac.kontrakt.abac.dto.PersonerOperasjonDto;
import no.nav.sif.abac.kontrakt.person.AktørId;
import no.nav.sif.abac.kontrakt.person.PersonIdent;

import java.util.Collections;

public class PdpRequestMapper {

    public static PersonerOperasjonDto map(PdpRequest pdpRequest) {
        return new PersonerOperasjonDto(
                pdpRequest.getAktørIder().stream().map(it -> new AktørId(it.aktørId())).toList(),
                pdpRequest.getFødselsnumre().stream().map(it -> new PersonIdent(it.fnr())).toList(),
                operasjon(pdpRequest)
        );
    }

    public static OperasjonDto operasjon(PdpRequest pdpRequest) {
        ResourceType resource = resourceTypeFraKode(pdpRequest.getResourceType());
        return new OperasjonDto(resource, mapAction(pdpRequest.getActionType()), Collections.emptySet());
    }

    static ResourceType resourceTypeFraKode(BeskyttetRessursResourceType kode) {
        return switch (kode) {
            case APPLIKASJON -> ResourceType.APPLIKASJON;
            case FAGSAK -> ResourceType.FAGSAK;
            case DRIFT -> ResourceType.DRIFT;
            case VENTEFRIST -> ResourceType.VENTEFRIST;
            case UNGDOMSPROGRAM -> ResourceType.UNGDOMSPROGRAM;
            case OPPGAVE -> ResourceType.OPPGAVE;
            default -> throw new IllegalArgumentException("Ikke-støttet verdi: " + kode);
        };
    }

    static BeskyttetRessursActionAttributt mapAction(BeskyttetRessursActionType kode) {
        return switch (kode) {
            case READ -> BeskyttetRessursActionAttributt.READ;
            case UPDATE -> BeskyttetRessursActionAttributt.UPDATE;
            case CREATE -> BeskyttetRessursActionAttributt.CREATE;
            default -> throw new IllegalArgumentException("Ikke-styttet verdi: " + kode);
        };
    }

}
