package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OppgaveJournalføringRepository {

    private EntityManager entityManager;

    public OppgaveJournalføringRepository() {
        // CDI proxy
    }

    @Inject
    public OppgaveJournalføringRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(OppgaveJournalføringEntitet journalføring) {
        entityManager.persist(journalføring);
        entityManager.flush();
    }

    public OppgaveJournalføringEntitet oppdater(OppgaveJournalføringEntitet journalføring) {
        OppgaveJournalføringEntitet merget = entityManager.merge(journalføring);
        entityManager.flush();
        return merget;
    }

    public Optional<OppgaveJournalføringEntitet> hentForOppgaveReferanse(UUID oppgavereferanse) {
        TypedQuery<OppgaveJournalføringEntitet> query = entityManager.createQuery(
            "SELECT j FROM OppgaveJournalføring j WHERE j.oppgave.oppgavereferanse = :oppgavereferanse",
            OppgaveJournalføringEntitet.class
        );
        query.setParameter("oppgavereferanse", oppgavereferanse);
        return query.getResultList().stream().findFirst();
    }

    /**
     * Rader som venter på journalføring, eldste først - tilsvarer indeksen
     * {@code idx_bd_oppgave_journalforing_etterslep}.
     */
    public List<OppgaveJournalføringEntitet> hentEtterslep() {
        TypedQuery<OppgaveJournalføringEntitet> query = entityManager.createQuery(
            "SELECT j FROM OppgaveJournalføring j WHERE j.status = :status ORDER BY j.opprettetTidspunkt",
            OppgaveJournalføringEntitet.class
        );
        query.setParameter("status", JournalføringStatus.PLANLAGT);
        return query.getResultList();
    }

    /**
     * Grunnlag for etterslep-gauge {@code ung_brukerdialog_journalforing_etterslep}. Egen,
     * snevrere spørring enn {@link #hentEtterslep()}, som må returnere ALLE planlagte rader
     * uavhengig av alder (brukes også til en eventuell opphentingsjobb).
     */
    public long tellEtterslepEldreEnn(Duration eldreEnn) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(j) FROM OppgaveJournalføring j WHERE j.status = :status AND j.opprettetTidspunkt < :grense",
            Long.class
        );
        query.setParameter("status", JournalføringStatus.PLANLAGT);
        query.setParameter("grense", LocalDateTime.now().minus(eldreEnn));
        return query.getSingleResult();
    }
}
