package no.nav.ung.brukerdialog.sak.mikrofrontend;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.k9.felles.jpa.HibernateVerktøy;
import no.nav.ung.brukerdialog.typer.AktørId;

import java.util.Optional;

@ApplicationScoped
public class MinSideMikrofrontendRepository {

    private EntityManager entityManager;

    public MinSideMikrofrontendRepository() {
        // CDI proxy
    }

    @Inject
    public MinSideMikrofrontendRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<MinSideMikrofrontendEntitet> hent(AktørId aktørId, MikrofrontendId mikrofrontendId) {
        TypedQuery<MinSideMikrofrontendEntitet> query = entityManager.createQuery(
            "SELECT m FROM MinSideMikrofrontend m WHERE m.aktørId = :aktoerId AND m.mikrofrontendId = :mikrofrontendId",
            MinSideMikrofrontendEntitet.class
        );
        query.setParameter("aktoerId", aktørId);
        query.setParameter("mikrofrontendId", mikrofrontendId);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public void lagre(MinSideMikrofrontendEntitet entitet) {
        entityManager.persist(entitet);
        entityManager.flush();
    }
}
