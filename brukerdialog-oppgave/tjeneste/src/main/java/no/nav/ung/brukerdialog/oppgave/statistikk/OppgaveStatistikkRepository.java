package no.nav.ung.brukerdialog.oppgave.statistikk;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class OppgaveStatistikkRepository {

    private EntityManager entityManager;

    OppgaveStatistikkRepository() {
        // CDI proxy
    }

    @Inject
    public OppgaveStatistikkRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Henter oppgaver som har fått svar (løst eller avbrutt) eller er eldre enn 14 dager.
     * Brukes til svartidstatistikk.
     */
    public List<BrukerdialogOppgaveEntitet> finnOppgaverForSvartidStatistikk() {
        return entityManager.createQuery(
                "SELECT o FROM BrukerdialogOppgave o " +
                "WHERE o.løstDato IS NOT NULL " +
                "OR o.status = :avbrutt " +
                "OR o.opprettetTidspunkt < :cutoff",
                BrukerdialogOppgaveEntitet.class)
            .setParameter("avbrutt", OppgaveStatus.AVBRUTT)
            .setParameter("cutoff", LocalDateTime.now().minusDays(14))
            .getResultList();
    }

    /**
     * Henter RAPPORTER_INNTEKT-oppgaver som er opprettet eller endret etter angitt tidspunkt.
     */
    public List<BrukerdialogOppgaveEntitet> finnRapporterInntektOppgaverEndretEtter(LocalDateTime sisteKjøring) {
        return entityManager.createQuery(
                "SELECT o FROM BrukerdialogOppgave o " +
                "WHERE o.oppgaveType = :type " +
                "AND (o.opprettetTidspunkt >= :sisteKjøring OR o.endretTidspunkt >= :sisteKjøring)",
                BrukerdialogOppgaveEntitet.class)
            .setParameter("type", OppgaveType.RAPPORTER_INNTEKT)
            .setParameter("sisteKjøring", sisteKjøring)
            .getResultList();
    }

    /**
     * Henter BEKREFT_AVVIK_REGISTERINNTEKT-oppgaver som er opprettet eller endret etter angitt tidspunkt.
     */
    public List<BrukerdialogOppgaveEntitet> finnBekreftAvvikOppgaverEndretEtter(LocalDateTime sisteKjøring) {
        return entityManager.createQuery(
                "SELECT o FROM BrukerdialogOppgave o " +
                "WHERE o.oppgaveType = :type " +
                "AND (o.opprettetTidspunkt >= :sisteKjøring OR o.endretTidspunkt >= :sisteKjøring)",
                BrukerdialogOppgaveEntitet.class)
            .setParameter("type", OppgaveType.BEKREFT_AVVIK_REGISTERINNTEKT)
            .setParameter("sisteKjøring", sisteKjøring)
            .getResultList();
    }
}
