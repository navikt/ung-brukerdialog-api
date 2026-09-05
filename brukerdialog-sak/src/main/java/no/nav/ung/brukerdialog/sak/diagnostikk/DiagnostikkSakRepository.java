package no.nav.ung.brukerdialog.sak.diagnostikk;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.ung.brukerdialog.sak.fagsak.FagSakEntitet;
import no.nav.ung.brukerdialog.sak.fagsak.VedtakPeriodeEntitet;
import no.nav.ung.brukerdialog.sak.soknad.SøknadHendelseEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class DiagnostikkSakRepository {

    private EntityManager entityManager;

    public DiagnostikkSakRepository() {
        // CDI proxy
    }

    @Inject
    public DiagnostikkSakRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<FagSakEntitet> hentAlleFagsaker(AktørId aktørId) {
        return entityManager.createQuery(
                "SELECT f FROM Fagsak f WHERE f.aktørId = :aktørId ORDER BY f.id",
                FagSakEntitet.class)
            .setParameter("aktørId", aktørId)
            .getResultList();
    }

    public Map<Saksnummer, List<VedtakPeriodeEntitet>> hentAllePerioderPerSaksnummer(List<Saksnummer> saksnumre) {
        if (saksnumre.isEmpty()) {
            return Map.of();
        }
        return entityManager.createQuery(
                "SELECT p.fagsak.saksnummer, p FROM VedtakPeriode p WHERE p.fagsak.saksnummer IN :saksnumre ORDER BY p.id",
                Object[].class)
            .setParameter("saksnumre", saksnumre)
            .getResultList()
            .stream()
            .collect(Collectors.groupingBy(
                rad -> (Saksnummer) rad[0],
                Collectors.mapping(rad -> (VedtakPeriodeEntitet) rad[1], Collectors.toList())));
    }

    public List<SøknadHendelseEntitet> hentAlleSøknadHendelser(AktørId aktørId) {
        return entityManager.createQuery(
                "SELECT s FROM SøknadHendelse s WHERE s.aktørId = :aktørId ORDER BY s.mottatt DESC",
                SøknadHendelseEntitet.class)
            .setParameter("aktørId", aktørId)
            .getResultList();
    }
}
