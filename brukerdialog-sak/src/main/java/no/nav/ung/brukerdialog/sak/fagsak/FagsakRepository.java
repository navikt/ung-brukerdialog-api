package no.nav.ung.brukerdialog.sak.fagsak;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import no.nav.k9.felles.jpa.HibernateVerktøy;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakPeriodeDto;
import no.nav.ung.brukerdialog.sak.FagsakYtelseType;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FagsakRepository {

    private static final Logger log = LoggerFactory.getLogger(FagsakRepository.class);
    private EntityManager entityManager;

    public FagsakRepository() {
        // CDI proxy
    }

    @Inject
    public FagsakRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public FagsakEntitet lagreFagsak(AktørId aktørId, FagsakYtelseType fagsakYtelseType, Saksnummer saksnummer, List<VedtakPeriodeDto> perioder) {
        Optional<FagsakEntitet> eksisterendeFagsakOpt = hentForSaksnummer(saksnummer);
        if (eksisterendeFagsakOpt.isPresent() && !eksisterendeFagsakOpt.get().getAktørId().equals(aktørId)) {
            throw new IllegalStateException("Saken tilhører en annen bruker. Saksnummer " + eksisterendeFagsakOpt.get().getSaksnummer());
        }

        if (eksisterendeFagsakOpt.isPresent()) {
            return erstattEksisterendePerioderHvisEndret(perioder, eksisterendeFagsakOpt.get());
        }

        FagsakEntitet fagsak = new FagsakEntitet(aktørId, fagsakYtelseType, saksnummer, perioder);
        entityManager.persist(fagsak);
        entityManager.flush();
        return fagsak;

    }

    private FagsakEntitet erstattEksisterendePerioderHvisEndret(List<VedtakPeriodeDto> perioder, FagsakEntitet eksisterendeFagsak) {
        if (sortert(somDto(eksisterendeFagsak.getAktivePerioder())).equals(sortert(perioder))) {
            log.info("Ingen endring av fagsak. ");
            return eksisterendeFagsak;
        }
        // Hibernate bumper ikke versjonen når bare den inverse periodesamlingen endres (VedtakPeriode),
        // så to samtidige meldinger på samme sak ville begge gått gjennom uten dette.
        // Version på VedtakPeriode ville ikke fanget det hvis lista opprinnelig var tom
        entityManager.lock(eksisterendeFagsak, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        eksisterendeFagsak.erstattPerioder(perioder);
        entityManager.flush();
        return eksisterendeFagsak;
    }

    private static List<VedtakPeriodeDto> sortert(List<VedtakPeriodeDto> perioder) {
        return perioder.stream()
            .sorted(Comparator.comparing(VedtakPeriodeDto::periode)
                .thenComparing(VedtakPeriodeDto::vedtakResultatType))
            .toList();
    }

    private static List<VedtakPeriodeDto> somDto(List<VedtakPeriodeEntitet> aktivePerioder) {
        return aktivePerioder.stream()
            .map(it -> new VedtakPeriodeDto(it.getPeriode().tilPeriode(), it.getResultat()))
            .toList();
    }

    public Optional<FagsakEntitet> hentForSaksnummer(Saksnummer saksnummer) {
        TypedQuery<FagsakEntitet> query = entityManager.createQuery(
            "SELECT f FROM Fagsak f WHERE f.saksnummer = :saksnummer",
            FagsakEntitet.class
        );
        query.setParameter("saksnummer", saksnummer);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<FagsakEntitet> hentForAktørOgYtelse(AktørId aktørId, FagsakYtelseType ytelseType) {
        TypedQuery<FagsakEntitet> query = entityManager.createQuery(
            "SELECT f FROM Fagsak f WHERE f.aktørId = :aktoerId AND f.ytelseType = :ytelseType ORDER BY f.opprettetTidspunkt DESC",
            FagsakEntitet.class
        );
        query.setParameter("aktoerId", aktørId);
        query.setParameter("ytelseType", ytelseType);
        return query.getResultList();
    }
}
