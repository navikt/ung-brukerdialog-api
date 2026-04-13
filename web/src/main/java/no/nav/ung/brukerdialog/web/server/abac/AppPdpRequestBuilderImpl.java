package no.nav.ung.brukerdialog.web.server.abac;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.env.Cluster;
import no.nav.k9.felles.konfigurasjon.env.Environment;
import no.nav.k9.felles.sikkerhet.abac.*;
import no.nav.ung.brukerdialog.abac.AppAbacAttributtType;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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

    private final BrukerdialogOppgaveRepository oppgaveRepository;

    public AppPdpRequestBuilderImpl() {
        this(null);
    }

    @Inject
    public AppPdpRequestBuilderImpl(BrukerdialogOppgaveRepository oppgaveRepository) {
        this.oppgaveRepository = oppgaveRepository;
    }


    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        PdpRequestMedBerørtePersonerForAuditlogg pdpRequest = new PdpRequestMedBerørtePersonerForAuditlogg();

        pdpRequest.setActionType(attributter.getActionType());
        pdpRequest.setResourceType(attributter.getResourceType());
        pdpRequest.setFagsakYtelseTyper(attributter.getVerdier(AppAbacAttributtType.YTELSETYPE));
        Set<String> aktørIder = new HashSet<>(attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID));

        mapAktørIdFraEksternReferanser(attributter, aktørIder);

        Set<String> fødselsnumre = attributter.getVerdier(StandardAbacAttributtType.FNR);
        pdpRequest.setAktørIderStr(aktørIder);
        pdpRequest.setFødselsnumreStr(fødselsnumre);
        pdpRequest.setBerørtePersonerForAuditlogg(new BerørtePersonerForAuditlogg(
            fødselsnumre.stream().map(Fnr::new).collect(Collectors.toSet()),
            aktørIder.stream().map(AktørId::new).collect(Collectors.toSet())));
        return pdpRequest;
    }

    private void mapAktørIdFraEksternReferanser(AbacAttributtSamling attributter, Set<String> aktørIder) {
        Set<String> eksternReferanser = attributter.getVerdier(AppAbacAttributtType.OPPGAVE_EKSTERN_REFERANSE);
        if (!eksternReferanser.isEmpty()) {
            eksternReferanser.stream().map(UUID::fromString).map(oppgaveRepository::hentOppgaveForOppgavereferanse)
                .flatMap(Optional::stream)
                .map(BrukerdialogOppgaveEntitet::getAktørId)
                .map(no.nav.ung.brukerdialog.typer.AktørId::getId)
                .forEach(aktørIder::add);
        }
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
