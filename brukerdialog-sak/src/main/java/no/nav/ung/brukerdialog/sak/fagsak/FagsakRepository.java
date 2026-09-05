package no.nav.ung.brukerdialog.sak.fagsak;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.k9.felles.jpa.HibernateVerktøy;
import no.nav.ung.brukerdialog.sak.soknad.FagsakYtelseType;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FagsakRepository {

    private EntityManager entityManager;

    public FagsakRepository() {
        // CDI proxy
    }

    @Inject
    public FagsakRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(FagSakEntitet fagsak) {
        if (fagsak.getId() == null) {
            entityManager.persist(fagsak);
        }
        fagsak.getAktivePerioder().forEach(entityManager::persist);
        entityManager.flush();
    }

    public Optional<FagSakEntitet> hentForSaksnummer(Saksnummer saksnummer) {
        TypedQuery<FagSakEntitet> query = entityManager.createQuery(
            "SELECT f FROM Fagsak f WHERE f.saksnummer = :saksnummer",
            FagSakEntitet.class
        );
        query.setParameter("saksnummer", saksnummer);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<FagSakEntitet> hentForAktørOgYtelse(AktørId aktørId, FagsakYtelseType ytelseType) {
        TypedQuery<FagSakEntitet> query = entityManager.createQuery(
            "SELECT f FROM Fagsak f WHERE f.aktørId = :aktoerId AND f.ytelseType = :ytelseType ORDER BY f.opprettetTidspunkt DESC",
            FagSakEntitet.class
        );
        query.setParameter("aktoerId", aktørId);
        query.setParameter("ytelseType", ytelseType);
        return query.getResultList();
    }
}
