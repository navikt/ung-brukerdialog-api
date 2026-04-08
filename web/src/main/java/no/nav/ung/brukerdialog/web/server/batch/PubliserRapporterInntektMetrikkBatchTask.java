package no.nav.ung.brukerdialog.web.server.batch;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.integrasjon.bigquery.klient.BigQueryKlient;
import no.nav.k9.prosesstask.api.BatchProsessTaskHandler;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.impl.cron.CronExpression;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.statistikk.OppgaveStatistikkRepository;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.InntektsrapporteringOppgaveDataEntitet;
import no.nav.ung.brukerdialog.web.server.batch.bigquery.RapporterInntektOppgaveTabellDefinisjon;
import no.nav.ung.brukerdialog.web.server.batch.bigquery.RapporterInntektRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@ProsessTask(value = PubliserRapporterInntektMetrikkBatchTask.TASKTYPE)
public class PubliserRapporterInntektMetrikkBatchTask implements BatchProsessTaskHandler {

    public static final String TASKTYPE = "batch.metrikk.rapporterInntekt";
    private static final String DATASET = "ung_deltakelse_opplyser_statistikk_dataset";
    private static final Logger log = LoggerFactory.getLogger(PubliserRapporterInntektMetrikkBatchTask.class);

    private OppgaveStatistikkRepository statistikkRepository;
    private BigQueryKlient bigQueryKlient;

    PubliserRapporterInntektMetrikkBatchTask() {
        // CDI proxy
    }

    @Inject
    public PubliserRapporterInntektMetrikkBatchTask(OppgaveStatistikkRepository statistikkRepository,
                                                    BigQueryKlient bigQueryKlient) {
        this.statistikkRepository = statistikkRepository;
        this.bigQueryKlient = bigQueryKlient;
    }

    @Override
    public CronExpression getCron() {
        return CronExpression.create("0 0 * * * *");
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var oppgaver = statistikkRepository.finnAlleRapporterInntektOppgaver();
        Instant now = Instant.now();
        var rows = oppgaver.stream()
            .map(this::tilRecord)
            .filter(Objects::nonNull)
            .map(r -> RapporterInntektOppgaveTabellDefinisjon.INSTANCE.getRowMapper(now).apply(r))
            .collect(Collectors.toList());

        bigQueryKlient.tømOgPubliserAtomisk(DATASET, RapporterInntektOppgaveTabellDefinisjon.INSTANCE, rows);
        log.info("Publiserte {} rader til BigQuery tabell {}", rows.size(), RapporterInntektOppgaveTabellDefinisjon.TABELL_NAVN);
    }

    private RapporterInntektRecord tilRecord(BrukerdialogOppgaveEntitet oppgave) {
        var data = oppgave.getOppgaveData();
        if (!(data instanceof InntektsrapporteringOppgaveDataEntitet inntektsdata)) {
            return null;
        }
        LocalDateTime sistEndret = finnSistEndret(oppgave);
        return new RapporterInntektRecord(
            oppgave.getOppgavereferanse(),
            oppgave.getStatus().name(),
            inntektsdata.getFraOgMed(),
            inntektsdata.getTilOgMed(),
            inntektsdata.isGjelderDelerAvMåned(),
            sistEndret
        );
    }

    private LocalDateTime finnSistEndret(BrukerdialogOppgaveEntitet oppgave) {
        LocalDateTime fristForUtløpt = oppgave.getStatus() == OppgaveStatus.UTLØPT ? oppgave.getFristTid() : null;
        return Stream.of(oppgave.getOpprettetTidspunkt(), oppgave.getLøstDato(), fristForUtløpt)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(oppgave.getOpprettetTidspunkt());
    }
}
