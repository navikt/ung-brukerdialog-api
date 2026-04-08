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
import no.nav.ung.brukerdialog.web.server.batch.bigquery.OppgaveSvartidRecord;
import no.nav.ung.brukerdialog.web.server.batch.bigquery.OppgaveSvartidTabellDefinisjon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@ProsessTask(value = PubliserOppgaveSvartidMetrikkBatchTask.TASKTYPE)
public class PubliserOppgaveSvartidMetrikkBatchTask implements BatchProsessTaskHandler {

    public static final String TASKTYPE = "batch.metrikk.oppgaveSvartid";
    private static final String DATASET = "ung_deltakelse_opplyser_statistikk_dataset";
    private static final Logger log = LoggerFactory.getLogger(PubliserOppgaveSvartidMetrikkBatchTask.class);

    private OppgaveStatistikkRepository statistikkRepository;
    private BigQueryKlient bigQueryKlient;

    PubliserOppgaveSvartidMetrikkBatchTask() {
        // CDI proxy
    }

    @Inject
    public PubliserOppgaveSvartidMetrikkBatchTask(OppgaveStatistikkRepository statistikkRepository,
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
        var oppgaver = statistikkRepository.finnOppgaverForSvartidStatistikk();
        var records = beregnSvartidRecords(oppgaver);
        Instant now = Instant.now();
        var rows = records.stream()
            .map(r -> OppgaveSvartidTabellDefinisjon.INSTANCE.getRowMapper(now).apply(r))
            .collect(Collectors.toList());

        bigQueryKlient.publiser(DATASET, OppgaveSvartidTabellDefinisjon.INSTANCE, rows);
        log.info("Publiserte {} rader til BigQuery tabell {}", rows.size(), OppgaveSvartidTabellDefinisjon.TABELL_NAVN);
    }

    private List<OppgaveSvartidRecord> beregnSvartidRecords(List<BrukerdialogOppgaveEntitet> oppgaver) {
        var records = new ArrayList<OppgaveSvartidRecord>();

        // Løste oppgaver - grupper etter (svartidDager, oppgaveType)
        oppgaver.stream()
            .filter(o -> o.getLøstDato() != null)
            .collect(Collectors.groupingBy(o -> svartidOgTypeKey(
                ChronoUnit.DAYS.between(o.getOpprettetTidspunkt(), o.getLøstDato()),
                o.getOppgaveType().name())))
            .forEach((key, gruppe) -> records.add(new OppgaveSvartidRecord(
                key.svartidDager(),
                true,
                false,
                false,
                key.oppgaveType(),
                gruppe.size()
            )));

        // Avbrutte oppgaver (tilsvarer "lukket" i ung-deltakelse-opplyser)
        oppgaver.stream()
            .filter(o -> o.getLøstDato() == null && o.getStatus() == OppgaveStatus.AVBRUTT)
            .collect(Collectors.groupingBy(o -> {
                var avbrutt = o.getEndretTidspunkt() != null ? o.getEndretTidspunkt() : o.getOpprettetTidspunkt();
                return svartidOgTypeKey(
                    ChronoUnit.DAYS.between(o.getOpprettetTidspunkt(), avbrutt),
                    o.getOppgaveType().name());
            }))
            .forEach((key, gruppe) -> records.add(new OppgaveSvartidRecord(
                key.svartidDager(),
                false,
                true,
                false,
                key.oppgaveType(),
                gruppe.size()
            )));

        // Oppgaver som verken er løst eller avbrutt og er eldre enn 14 dager
        oppgaver.stream()
            .filter(o -> o.getLøstDato() == null && o.getStatus() != OppgaveStatus.AVBRUTT)
            .collect(Collectors.groupingBy(o -> o.getOppgaveType().name()))
            .forEach((oppgaveType, gruppe) -> records.add(new OppgaveSvartidRecord(
                null,
                false,
                false,
                true,
                oppgaveType,
                gruppe.size()
            )));

        return records;
    }

    private record SvartidOgTypeKey(long svartidDager, String oppgaveType) {
    }

    private SvartidOgTypeKey svartidOgTypeKey(long svartidDager, String oppgaveType) {
        return new SvartidOgTypeKey(svartidDager, oppgaveType);
    }
}
