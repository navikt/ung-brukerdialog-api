package no.nav.ung.brukerdialog.web.server.metrics;


import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import no.nav.k9.felles.log.metrics.MetricsUtil;
import no.nav.ung.brukerdialog.oppgave.journalforing.JournalføringMetrikker;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveJournalføringRepository;

/**
 * Eksponerer Prometheus-metrikker. {@code @Transactional} fordi
 * {@code ung_brukerdialog_journalforing_etterslep}-gaugen leser fra databasen første gang
 * Micrometer skraper verdien, dvs. inne i {@link #prometheus()}.
 */
@Path("/metrics")
@Produces(TEXT_PLAIN)
@ApplicationScoped
@Transactional
public class PrometheusRestService {

    public PrometheusRestService() {
        // CDI proxy
    }

    @Inject
    public PrometheusRestService(OppgaveJournalføringRepository journalføringRepository) {
        JournalføringMetrikker.registrerEtterslepGauge(journalføringRepository);
    }

    @GET
    @Operation(hidden = true)
    @Path("/prometheus")
    public String prometheus()  {
        return MetricsUtil.REGISTRY.scrape();
    }
}

