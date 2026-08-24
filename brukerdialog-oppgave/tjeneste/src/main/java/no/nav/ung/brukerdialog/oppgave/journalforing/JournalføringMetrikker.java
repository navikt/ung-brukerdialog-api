package no.nav.ung.brukerdialog.oppgave.journalforing;

import java.time.Duration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import no.nav.k9.felles.log.metrics.MetricsUtil;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;

/**
 * Metrikker for journalføring, samlet ett sted for konsistente navn og tagger.
 * Registrerer mot {@link MetricsUtil#REGISTRY}, som allerede eksponeres på
 * {@code /internal/metrics/prometheus}.
 * <p>
 * {@code Counter}/{@code Timer}/{@code Gauge}-registrering er trygt å gjenta - Micrometer
 * dedupliserer på navn+tagger, så gjentatte {@code .register(...)}-kall returnerer samme
 * instans i stedet for å opprette en ny.
 * <p>
 * Klassen er {@code public} fordi {@code registrerEtterslepGauge} kalles fra {@code web}-modulen
 * ({@code PrometheusRestService}) - selve metrikk-definisjonen skal likevel holdes samlet her,
 * ikke spres til web-laget.
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
     * Registrerer etterslep-gaugen mot repositoryet. Selve databasekallet skjer
     * først når Micrometer skraper verdien - altså fra {@code PrometheusRestService} i
     * web-modulen, som derfor må være {@code @Transactional} på samme måte som andre lesende
     * REST-endepunkt i denne kodebasen (jf. {@code DiagnostikkBrukerdialogOppgaverRestTjeneste}
     * og {@code no.nav.k9.felles.jpa.TransactionInterceptor}, som er globalt aktivert via
     * {@code @Priority}). {@code register(...)} her krever ingen transaksjon - det kobler bare
     * til en verdi-leverandør som kalles senere.
     * <p>
     * {@code Gauge.builder(name, repository, ...)} holder kun en {@code WeakReference} til
     * repositoryet, men det er trygt her: repositoryet er en {@code @ApplicationScoped}
     * CDI-bean som beholdes av containeren så lenge applikasjonen kjører.
     */
    public static void registrerEtterslepGauge(OppgaveJournalføringRepository repository) {
        Gauge.builder(NAVN_ETTERSLEP, repository, repo -> repo.tellEtterslepEldreEnn(ETTERSLEP_GRENSE))
            .description("Antall journalføringsrader som har ventet på journalføring i mer enn 1 time")
            .register(MetricsUtil.REGISTRY);
    }
}
