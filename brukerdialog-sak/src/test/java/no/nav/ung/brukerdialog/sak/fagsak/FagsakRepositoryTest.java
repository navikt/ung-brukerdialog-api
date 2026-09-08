package no.nav.ung.brukerdialog.sak.fagsak;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakPeriodeDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.FagsakYtelseType;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Periode;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

@ExtendWith(CdiAwareExtension.class)
@ExtendWith(JpaExtension.class)
class FagsakRepositoryTest {

    private static final FagsakYtelseType YTELSE = FagsakYtelseType.AKTIVITETSPENGER;
    private static final Saksnummer SAKSNUMMER = new Saksnummer("SAK1234");
    private static final LocalDate FOM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2025, 12, 31);

    @Inject
    private EntityManager entityManager;

    @Inject
    private FagsakRepository fagsakRepository;

    @Test
    void samtidig_lagring_på_sak_uten_perioder_skal_feile_med_optimistic_lock() {
        // Sak uten perioder: to samtidige meldinger ville begge bare satt inn nye rader, så det
        // finnes ingen delt periode-rad som kunne fanget konflikten. Bare fagsaken er felles.
        var aktørId = AktørId.dummy();
        fagsakRepository.lagreFagsak(aktørId, YTELSE, SAKSNUMMER, List.of());

        // Transaksjon 1 leser saken
        var kopi1 = fagsakRepository.hentForSaksnummer(SAKSNUMMER).orElseThrow();
        entityManager.detach(kopi1);

        // Transaksjon 2 rekker å legge inn sine perioder og bumper versjonen
        fagsakRepository.lagreFagsak(aktørId, YTELSE, SAKSNUMMER, List.of(innvilget(FOM, TOM)));
        entityManager.clear();

        // Transaksjon 1 forsøker å skrive sin utdaterte versjon
        assertThatThrownBy(() -> {
            entityManager.merge(kopi1);
            entityManager.flush();
        }).isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void samme_perioder_i_annen_rekkefølge_skal_ikke_lagre_nye_perioder() {
        var aktørId = AktørId.dummy();
        var første = innvilget(FOM, TOM);
        var andre = innvilget(TOM.plusDays(1), TOM.plusMonths(6));

        fagsakRepository.lagreFagsak(aktørId, YTELSE, SAKSNUMMER, List.of(første, andre));
        flushOgTøm();
        var periodeIderFørst = periodeIder(SAKSNUMMER);
        var versjonFørst = versjon(SAKSNUMMER);

        fagsakRepository.lagreFagsak(aktørId, YTELSE, SAKSNUMMER, List.of(andre, første));
        flushOgTøm();

        assertThat(periodeIder(SAKSNUMMER)).containsExactlyInAnyOrderElementsOf(periodeIderFørst);
        assertThat(versjon(SAKSNUMMER)).isEqualTo(versjonFørst);
    }

    @Test
    void endrede_perioder_skal_deaktivere_forrige_generasjon() {
        var aktørId = AktørId.dummy();

        fagsakRepository.lagreFagsak(aktørId, YTELSE, SAKSNUMMER, List.of(innvilget(FOM, TOM)));
        flushOgTøm();
        var periodeIderFørst = periodeIder(SAKSNUMMER);
        var fagsakIdFørst = fagsakRepository.hentForSaksnummer(SAKSNUMMER).orElseThrow().getId();

        fagsakRepository.lagreFagsak(aktørId, YTELSE, SAKSNUMMER, List.of(innvilget(FOM, TOM.plusMonths(1))));
        flushOgTøm();

        assertThat(periodeIder(SAKSNUMMER)).containsAll(periodeIderFørst).hasSize(2);

        var fagsak = fagsakRepository.hentForSaksnummer(SAKSNUMMER).orElseThrow();
        assertThat(fagsak.getId()).isEqualTo(fagsakIdFørst);
        assertThat(fagsak.getAktivePerioder())
            .singleElement()
            .extracting(it -> it.getPeriode().getTomDato())
            .isEqualTo(TOM.plusMonths(1));
        assertThat(inaktivePerioder(SAKSNUMMER))
            .singleElement()
            .extracting(it -> it.getPeriode().getTomDato())
            .isEqualTo(TOM);
    }

    @Test
    void fagsak_uten_perioder_skal_lagres() {
        fagsakRepository.lagreFagsak(AktørId.dummy(), YTELSE, SAKSNUMMER, List.of());
        flushOgTøm();

        assertThat(fagsakRepository.hentForSaksnummer(SAKSNUMMER)).get()
            .extracting(FagsakEntitet::getAktivePerioder).asInstanceOf(LIST)
            .isEmpty();
    }

    @Test
    void flere_saker_på_samme_deltaker_skal_lagres_hver_for_seg() {
        var aktørId = AktørId.dummy();

        fagsakRepository.lagreFagsak(aktørId, YTELSE, SAKSNUMMER, List.of(innvilget(FOM, TOM)));
        fagsakRepository.lagreFagsak(aktørId, YTELSE, new Saksnummer("SAK5678"),
            List.of(innvilget(FOM.plusYears(2), TOM.plusYears(2))));
        flushOgTøm();

        assertThat(fagsakRepository.hentForAktørOgYtelse(aktørId, YTELSE)).hasSize(2);
    }

    @Test
    void sak_som_tilhører_en_annen_deltaker_skal_avvises() {
        fagsakRepository.lagreFagsak(AktørId.dummy(), YTELSE, SAKSNUMMER, List.of(innvilget(FOM, TOM)));
        flushOgTøm();

        var annenAktør = AktørId.dummy();
        assertThatThrownBy(() -> fagsakRepository.lagreFagsak(annenAktør, YTELSE, SAKSNUMMER, List.of()))
            .isInstanceOf(IllegalStateException.class);
    }

    private List<Long> periodeIder(Saksnummer saksnummer) {
        return entityManager.createQuery(
                "SELECT p.id FROM VedtakPeriode p WHERE p.fagsak.saksnummer = :saksnummer", Long.class)
            .setParameter("saksnummer", saksnummer)
            .getResultList();
    }

    private List<VedtakPeriodeEntitet> inaktivePerioder(Saksnummer saksnummer) {
        return entityManager.createQuery(
                "SELECT p FROM VedtakPeriode p WHERE p.fagsak.saksnummer = :saksnummer AND p.aktiv = false",
                VedtakPeriodeEntitet.class)
            .setParameter("saksnummer", saksnummer)
            .getResultList();
    }

    private long versjon(Saksnummer saksnummer) {
        return entityManager.createQuery(
                "SELECT f.versjon FROM Fagsak f WHERE f.saksnummer = :saksnummer", Long.class)
            .setParameter("saksnummer", saksnummer)
            .getSingleResult();
    }

    private void flushOgTøm() {
        entityManager.flush();
        entityManager.clear();
    }

    private static VedtakPeriodeDto innvilget(LocalDate fom, LocalDate tom) {
        return new VedtakPeriodeDto(new Periode(fom, tom), VedtakResultatType.INNVILGET);
    }
}
