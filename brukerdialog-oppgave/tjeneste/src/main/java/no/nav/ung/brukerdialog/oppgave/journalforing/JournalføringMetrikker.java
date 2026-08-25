package no.nav.ung.brukerdialog.oppgave.journalforing;

import java.time.Duration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import no.nav.k9.felles.log.metrics.MetricsUtil;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;

/**
 * Metrikker for journalføring, samlet ett sted for konsistente navn/tagger. Registrerer mot
 * {@link MetricsUtil#REGISTRY} ({@code /internal/metrics/prometheus}).
 * <p>
 * Trygt å kalle {@code .register(...)} gjentatte ganger - Micrometer dedupliserer på
 * navn+tagger. {@code public} fordi {@code registrerEtterslepGauge} kalles fra
 * {@code web}-modulen ({@code PrometheusRestService}).
 */
public final class JournalføringMetrikker {

    private static final String NAVN_TOTAL = "ung_brukerdialog_journalforing_total";
    private static final String NAVN_IKKE_FERDIGSTILT = "ung_brukerdialog_journalforing_ikke_ferdigstilt_total";
    private static final String NAVN_VARIGHET = "ung_brukerdialog_journalforing_varighet";
    private static final String NAVN_ETTERSLEP = "ung_brukerdialog_journalforing_etterslep";

    /** Fanger opp både «flagget er av» og «tasken kom aldri i mål». */
    private static final Duration ETTERSLEP_GRENSE = Duration.ofHours(1);

    private JournalføringMetrikker() {
    }

    /**
     * Utfall av ett journalføringsforsøk. {@code resultat}-taggen på
     * {@value #NAVN_TOTAL} - gir volum og feilrate per oppgavetype.
     */
    enum Resultat {
        /** Journalpost opprettet (og eventuelt ferdigstilt - se {@link #registrerIkkeFerdigstilt}). */
        OK,
        /** 409 fra Dokarkiv - journalposten finnes, men journalpostId kunne ikke leses. */
        DUPLIKAT_UTEN_ID,
        /** Uventet feil under forsøket (PDL, PDF-generering, dokarkiv-kallet e.l.). */
        FEILET,
        /** Tasken avsluttet uten forsøk på journalføring (idempotent eller deaktivert type). */
        HOPPET_OVER
    }

    static void registrer(OppgaveType oppgaveType, Resultat resultat) {
        Counter.builder(NAVN_TOTAL)
            .tag("oppgavetype", oppgaveType.name())
            .tag("resultat", resultat.name())
            .register(MetricsUtil.REGISTRY)
            .increment();
    }

    /** Journalpost opprettet, men IKKE ferdigstilt til tross for {@code forsoekFerdigstill=true}. */
    static void registrerIkkeFerdigstilt(OppgaveType oppgaveType) {
        Counter.builder(NAVN_IKKE_FERDIGSTILT)
            .tag("oppgavetype", oppgaveType.name())
            .register(MetricsUtil.REGISTRY)
            .increment();
    }

    static Timer.Sample startTidtaking() {
        return Timer.start(MetricsUtil.REGISTRY);
    }

    static void stoppTidtaking(Timer.Sample sample, OppgaveType oppgaveType) {
        sample.stop(Timer.builder(NAVN_VARIGHET)
            .tag("oppgavetype", oppgaveType.name())
            .register(MetricsUtil.REGISTRY));
    }

    /**
     * Databasekallet skjer først når Micrometer skraper gaugen - dvs. fra
     * {@code PrometheusRestService} i web-modulen, som derfor MÅ være {@code @Transactional}.
     * {@code register(...)} her krever selv ingen transaksjon.
     * <p>
     * {@code Gauge.builder(name, repository, ...)} holder kun en {@code WeakReference} til
     * repositoryet - trygt siden det er en {@code @ApplicationScoped} CDI-bean.
     */
    public static void registrerEtterslepGauge(OppgaveJournalføringRepository repository) {
        Gauge.builder(NAVN_ETTERSLEP, repository, repo -> repo.tellEtterslepEldreEnn(ETTERSLEP_GRENSE))
            .description("Antall journalføringsrader som har ventet på journalføring i mer enn 1 time")
            .register(MetricsUtil.REGISTRY);
    }
}
