package no.nav.ung.brukerdialog.web.server.abac;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import no.nav.k9.felles.konfigurasjon.env.Cluster;
import no.nav.k9.felles.konfigurasjon.env.Environment;
import no.nav.k9.felles.sikkerhet.abac.*;
import no.nav.ung.brukerdialog.abac.AppAbacAttributtType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Dependent
@Alternative
@Priority(2)
public class AppPdpRequestBuilderImpl implements PdpRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(AppPdpRequestBuilderImpl.class);
    private static final Cluster CLUSTER = Environment.current().getCluster();
    private static final List<String> INTERNAL_CLUSTER_NAMESPACE = List.of(
            CLUSTER.clusterName() + ":k9saksbehandling",
            CLUSTER.DEV_GCP.clusterName() + ":dusseldorf",
            CLUSTER.PROD_GCP.clusterName() + ":dusseldorf"
    );

    public AppPdpRequestBuilderImpl() {
    }


    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        PdpRequestMedBerørtePersonerForAuditlogg pdpRequest = new PdpRequestMedBerørtePersonerForAuditlogg();

        pdpRequest.setActionType(attributter.getActionType());
        pdpRequest.setResourceType(attributter.getResourceType());
        pdpRequest.setFagsakYtelseTyper(attributter.getVerdier(AppAbacAttributtType.YTELSETYPE));

        Set<String> aktørIder = attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID);
        Set<String> fødselsnumre = attributter.getVerdier(StandardAbacAttributtType.FNR);
        pdpRequest.setAktørIderStr(aktørIder);
        pdpRequest.setFødselsnumreStr(fødselsnumre);
        pdpRequest.setBerørtePersonerForAuditlogg(new BerørtePersonerForAuditlogg(
            fødselsnumre.stream().map(Fnr::new).collect(Collectors.toSet()),
            aktørIder.stream().map(AktørId::new).collect(Collectors.toSet())));
        return pdpRequest;
    }


    @Override
    public boolean internAzureConsumer(String azpName) {
        var match = INTERNAL_CLUSTER_NAMESPACE.stream().anyMatch(azpName::startsWith);
        if (!match) {
            LOG.warn("App fra ikke-godkjent namespace har etterspurt tilgang: " + azpName);
        }
        return match;
    }

}
