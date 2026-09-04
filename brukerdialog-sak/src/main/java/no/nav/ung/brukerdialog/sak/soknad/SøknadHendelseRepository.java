package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.ung.brukerdialog.typer.AktørId;

import java.util.List;

@Dependent
public class SøknadHendelseRepository {

    private final EntityManager entityManager;


    @Inject
    public SøknadHendelseRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(SøknadHendelseEntitet søknadHendelse) {
        entityManager.persist(søknadHendelse);
        entityManager.flush();
    }

    public List<SøknadHendelseEntitet> hentForAktørOgYtelse(AktørId aktørId, FagsakYtelseType ytelseType) {
        TypedQuery<SøknadHendelseEntitet> query = entityManager.createQuery(
            "SELECT s FROM SøknadHendelse s WHERE s.aktørId = :aktørId AND s.ytelseType = :ytelseType AND s.aktiv = true ORDER BY s.mottatt DESC",
            SøknadHendelseEntitet.class
        );
        query.setParameter("aktørId", aktørId);
        query.setParameter("ytelseType", ytelseType);
        return query.getResultList();
    }
}
