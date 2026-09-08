package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.ung.brukerdialog.sak.FagsakYtelseType;
import no.nav.ung.brukerdialog.sak.fagsak.FagsakEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Dependent
public class SøknadHendelseRepository {

    private static final Logger log = LoggerFactory.getLogger(SøknadHendelseRepository.class);

    private final EntityManager entityManager;


    @Inject
    public SøknadHendelseRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(SøknadHendelseEntitet søknadHendelse) {
        entityManager.persist(søknadHendelse);
        entityManager.flush();
    }

    public void markerMottattIFagsak(AktørId aktørId, FagsakYtelseType ytelseType, FagsakEntitet fagsak, Set<UUID> mottattSøknadIder) {
        if (mottattSøknadIder.isEmpty()) {
            return;
        }

        hentAktiveSøknaderForAktørOgYtelse(aktørId, ytelseType).stream()
            .filter(søknad -> søknad.getMottattIFagsak() == null)
            .filter(søknad -> mottattSøknadIder.contains(søknad.getSøknadId()))
            .forEach(søknad -> {
                søknad.markerMottattIFagsak(fagsak);
                log.info("Markert søknad med id {} mottatt {} som mottatt av fagsak {}",
                    søknad.getId(), søknad.getMottatt(), fagsak.getSaksnummer().getVerdi());
            });
        entityManager.flush();
    }

    public List<SøknadHendelseEntitet> hentAktiveSøknaderForAktørOgYtelse(AktørId aktørId, FagsakYtelseType ytelseType) {
        TypedQuery<SøknadHendelseEntitet> query = entityManager.createQuery(
            "SELECT s FROM SøknadHendelse s WHERE s.aktørId = :aktørId AND s.ytelseType = :ytelseType AND s.aktiv = true ORDER BY s.mottatt DESC",
            SøknadHendelseEntitet.class
        );
        query.setParameter("aktørId", aktørId);
        query.setParameter("ytelseType", ytelseType);
        return query.getResultList();
    }
}
