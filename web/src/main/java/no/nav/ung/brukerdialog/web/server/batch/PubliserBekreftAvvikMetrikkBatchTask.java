package no.nav.ung.brukerdialog.web.server.batch;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.integrasjon.bigquery.klient.BigQueryKlient;
import no.nav.k9.prosesstask.api.BatchProsessTaskHandler;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.impl.cron.CronExpression;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.statistikk.OppgaveStatistikkRepository;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgaveDataEntitet;
import no.nav.ung.brukerdialog.web.server.batch.bigquery.BekreftAvvikOppgaveTabellDefinisjon;
import no.nav.ung.brukerdialog.web.server.batch.bigquery.BekreftAvvikRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@ProsessTask(value = PubliserBekreftAvvikMetrikkBatchTask.TASKTYPE)
public class PubliserBekreftAvvikMetrikkBatchTask implements BatchProsessTaskHandler {

    public static final String TASKTYPE = "batch.metrikk.bekreftAvvik";
    private static final String DATASET = "ung_deltakelse_opplyser_statistikk_dataset";
    private static final Logger log = LoggerFactory.getLogger(PubliserBekreftAvvikMetrikkBatchTask.class);

    private OppgaveStatistikkRepository statistikkRepository;
    private BigQueryKlient bigQueryKlient;

    PubliserBekreftAvvikMetrikkBatchTask() {
        // CDI proxy
    }

    @Inject
    public PubliserBekreftAvvikMetrikkBatchTask(OppgaveStatistikkRepository statistikkRepository,
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
        var oppgaver = statistikkRepository.finnAlleBekreftAvvikOppgaver();
        Instant now = Instant.now();
        var rows = oppgaver.stream()
            .map(this::tilRecord)
            .filter(Objects::nonNull)
            .map(r -> BekreftAvvikOppgaveTabellDefinisjon.INSTANCE.getRowMapper(now).apply(r))
            .collect(Collectors.toList());

        bigQueryKlient.tømOgPubliserAtomisk(DATASET, BekreftAvvikOppgaveTabellDefinisjon.INSTANCE, rows);
        log.info("Publiserte {} rader til BigQuery tabell {}", rows.size(), BekreftAvvikOppgaveTabellDefinisjon.TABELL_NAVN);
    }

    private BekreftAvvikRecord tilRecord(BrukerdialogOppgaveEntitet oppgave) {
        var data = oppgave.getOppgaveData();
        if (!(data instanceof KontrollerRegisterinntektOppgaveDataEntitet kontrollerData)) {
            return null;
        }
        LocalDateTime sistEndret = finnSistEndret(oppgave);
        return new BekreftAvvikRecord(
            oppgave.getOppgavereferanse(),
            oppgave.getStatus().name(),
            kontrollerData.getFraOgMed(),
            kontrollerData.getTilOgMed(),
            kontrollerData.isGjelderDelerAvMåned(),
            harRegisterInntekt(kontrollerData),
            sistEndret
        );
    }

    private boolean harRegisterInntekt(KontrollerRegisterinntektOppgaveDataEntitet data) {
        for (var inntekt : data.getArbeidOgFrilansInntekter()) {
            if (inntekt.getInntekt() > 0) {
                return true;
            }
        }
        for (var inntekt : data.getYtelseInntekter()) {
            if (inntekt.getInntekt() > 0) {
                return true;
            }
        }
        return false;
    }

    private LocalDateTime finnSistEndret(BrukerdialogOppgaveEntitet oppgave) {
        return Stream.of(oppgave.getOpprettetTidspunkt(), oppgave.getLøstDato(), oppgave.getEndretTidspunkt())
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(oppgave.getOpprettetTidspunkt());
    }
}
