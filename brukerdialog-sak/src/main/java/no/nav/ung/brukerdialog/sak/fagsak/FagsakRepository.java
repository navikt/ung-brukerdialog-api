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
        Optional<FagSakEntitet> eksisterendeOpt = hentForSaksnummer(fagsak.getSaksnummer());
        if (eksisterendeOpt.isPresent()) {
            FagSakEntitet eksisterendeSak = eksisterendeOpt.get();
            eksisterendeSak.deaktiver();
            entityManager.persist(eksisterendeSak);
        }
        entityManager.persist(fagsak);
        entityManager.flush();
    }

    public Optional<FagSakEntitet> hentForSaksnummer(Saksnummer saksnummer) {
        TypedQuery<FagSakEntitet> query = entityManager.createQuery(
            "SELECT v FROM Fagsak v WHERE v.saksnummer = :saksnummer",
            FagSakEntitet.class
        );
        query.setParameter("saksnummer", saksnummer);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<FagSakEntitet> hentForAktørOgYtelse(AktørId aktørId, FagsakYtelseType ytelseType) {
        TypedQuery<FagSakEntitet> query = entityManager.createQuery(
            "SELECT v FROM Fagsak v WHERE v.aktørId = :aktoerId AND v.ytelseType = :ytelseType ORDER BY v.opprettetTidspunkt DESC",
            FagSakEntitet.class
        );
        query.setParameter("aktoerId", aktørId);
        query.setParameter("ytelseType", ytelseType);
        return query.getResultList();
    }
}
