package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

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

    public Optional<OppgaveJournalføringEntitet> hentForOppgaveReferanse(UUID oppgavereferanse) {
        TypedQuery<OppgaveJournalføringEntitet> query = entityManager.createQuery(
            "SELECT j FROM OppgaveJournalføring j WHERE j.oppgave.oppgavereferanse = :oppgavereferanse",
            OppgaveJournalføringEntitet.class
        );
        query.setParameter("oppgavereferanse", oppgavereferanse);
        return query.getResultList().stream().findFirst();
    }
}
