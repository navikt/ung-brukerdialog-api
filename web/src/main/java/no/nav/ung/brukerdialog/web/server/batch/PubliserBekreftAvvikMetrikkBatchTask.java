package no.nav.ung.brukerdialog.web.server.batch;

import com.google.cloud.bigquery.InsertAllRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.integrasjon.bigquery.klient.BigQueryKlient;
import no.nav.k9.prosesstask.api.BatchProsessTaskHandler;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;
import no.nav.k9.prosesstask.impl.cron.CronExpression;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.statistikk.OppgaveStatistikkRepository;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgaveDataEntitet;
import no.nav.ung.brukerdialog.web.server.batch.bigquery.BekreftAvvikOppgaveTabellDefinisjon;
import no.nav.ung.brukerdialog.web.server.batch.bigquery.BekreftAvvikRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@ProsessTask(value = PubliserBekreftAvvikMetrikkBatchTask.TASKTYPE)
public class PubliserBekreftAvvikMetrikkBatchTask implements BatchProsessTaskHandler {

    public static final String TASKTYPE = "batch.metrikk.bekreftAvvik";
    private static final String DATASET = "ung_deltakelse_opplyser_statistikk_dataset";
    private static final String SISTE_KJØRING_PROPERTY = "sisteKjøring";
    private static final Logger log = LoggerFactory.getLogger(PubliserBekreftAvvikMetrikkBatchTask.class);

    private OppgaveStatistikkRepository statistikkRepository;
    private BigQueryKlient bigQueryKlient;
    private ProsessTaskTjeneste prosessTaskTjeneste;

    PubliserBekreftAvvikMetrikkBatchTask() {
        // CDI proxy
    }

    @Inject
    public PubliserBekreftAvvikMetrikkBatchTask(OppgaveStatistikkRepository statistikkRepository,
                                                BigQueryKlient bigQueryKlient,
                                                ProsessTaskTjeneste prosessTaskTjeneste) {
        this.statistikkRepository = statistikkRepository;
        this.bigQueryKlient = bigQueryKlient;
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @Override
    public CronExpression getCron() {
        return CronExpression.create("0 0 * * * *");
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        LocalDateTime now = LocalDateTime.now();
        var sisteKjøring = finnSisteKjørtTidspunkt(prosessTaskData);
        log.info("Henter {} oppgaver endret siden {}", TASKTYPE, sisteKjøring);

        var oppgaver = statistikkRepository.finnOppgaverEndretEtter(OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT, sisteKjøring);
        if (oppgaver.isEmpty()) {
            log.info("Ingen endringer siden siste kjøring for tabell {}. Hopper over publisering.", BekreftAvvikOppgaveTabellDefinisjon.TABELL_NAVN);
        } else {
            Instant nowInstant = now.toInstant(java.time.ZoneOffset.UTC);
            var rows = oppgaver.stream()
                .map(this::tilRecord)
                .filter(Objects::nonNull)
                .map(r -> BekreftAvvikOppgaveTabellDefinisjon.INSTANCE.getRowMapper(nowInstant).apply(r))
                .map(InsertAllRequest.RowToInsert::of)
                .collect(Collectors.toList());

            bigQueryKlient.publiser(DATASET, BekreftAvvikOppgaveTabellDefinisjon.INSTANCE, rows);
            log.info("Publiserte {} rader til BigQuery tabell {}", rows.size(), BekreftAvvikOppgaveTabellDefinisjon.TABELL_NAVN);
        }

        prosessTaskData.setProperty(SISTE_KJØRING_PROPERTY, now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        prosessTaskTjeneste.lagre(prosessTaskData);
    }

    private LocalDateTime finnSisteKjørtTidspunkt(ProsessTaskData prosessTaskData) {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(SISTE_KJØRING_PROPERTY))
            .map(LocalDateTime::parse)
            .orElse(LocalDateTime.now().minusHours(1));
    }

    private BekreftAvvikRecord tilRecord(BrukerdialogOppgaveEntitet oppgave) {
        var data = oppgave.getOppgaveData();
        if (!(data instanceof KontrollerRegisterinntektOppgaveDataEntitet kontrollerData)) {
            return null;
        }
        LocalDateTime sisteTidspunkt = finnSisteTidspunkt(oppgave);
        return new BekreftAvvikRecord(
            oppgave.getOppgavereferanse(),
            oppgave.getStatus().name(),
            kontrollerData.getFraOgMed(),
            kontrollerData.getTilOgMed(),
            kontrollerData.isGjelderDelerAvMåned(),
            harRegisterInntekt(kontrollerData),
            sisteTidspunkt
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

    private LocalDateTime finnSisteTidspunkt(BrukerdialogOppgaveEntitet oppgave) {
        return Objects.requireNonNullElse(oppgave.getEndretTidspunkt(), oppgave.getOpprettetTidspunkt());
    }
}
