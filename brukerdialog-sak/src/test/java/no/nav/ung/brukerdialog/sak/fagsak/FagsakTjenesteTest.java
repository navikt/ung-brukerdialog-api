package no.nav.ung.brukerdialog.sak.fagsak;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.vedtak.FagSakRequest;
import no.nav.ung.brukerdialog.kontrakt.vedtak.MottattSøknadDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakPeriodeDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.soknad.FagsakYtelseType;
import no.nav.ung.brukerdialog.sak.soknad.SøknadHendelseEntitet;
import no.nav.ung.brukerdialog.sak.soknad.SøknadHendelseRepository;
import no.nav.ung.brukerdialog.tid.DatoIntervallEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Periode;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(CdiAwareExtension.class)
@ExtendWith(JpaExtension.class)
class FagsakTjenesteTest {

    private static final FagsakYtelseType YTELSE = FagsakYtelseType.AKTIVITETSPENGER;
    private static final LocalDate FOM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2025, 12, 31);
    public static final Saksnummer SAKSNUMMER = new Saksnummer("SAK1234");

    @Inject
    private EntityManager entityManager;

    @Inject
    private FagsakTjeneste tjeneste;

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private SøknadHendelseRepository søknadHendelseRepository;

    @Test
    void skal_lagre_fagsak_med_perioder_og_finne_den_igjen_på_aktør() {
        var aktørId = AktørId.dummy();
        var saksnummer = SAKSNUMMER;

        tjeneste.motta(YTELSE, request(aktørId, saksnummer, List.of(innvilget(FOM, TOM)), List.of()));
        flushOgTøm();

        var lagret = fagsakRepository.hentForAktørOgYtelse(aktørId, YTELSE);
        assertThat(lagret).hasSize(1);
        assertThat(lagret.getFirst().getSaksnummer()).isEqualTo(saksnummer);

        var perioder = lagret.getFirst().getAktivePerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.getFirst().getPeriode()).isEqualTo(DatoIntervallEntitet.fra(FOM, TOM));
        assertThat(perioder.getFirst().getResultat()).isEqualTo(VedtakResultatType.INNVILGET);
    }

    @Test
    void skal_lagre_både_innvilgede_og_avslåtte_perioder() {
        var aktørId = AktørId.dummy();

        tjeneste.motta(YTELSE, request(aktørId, SAKSNUMMER, List.of(
            innvilget(FOM, TOM),
            new VedtakPeriodeDto(new Periode(TOM.plusDays(1), TOM.plusMonths(3)), VedtakResultatType.AVSLÅTT)
        ), List.of()));
        flushOgTøm();

        var perioder = fagsakRepository.hentForAktørOgYtelse(aktørId, YTELSE).getFirst().getAktivePerioder();
        assertThat(perioder).extracting(VedtakPeriodeEntitet::getResultat)
            .containsExactlyInAnyOrder(VedtakResultatType.INNVILGET, VedtakResultatType.AVSLÅTT);
    }

    @Test
    void ny_melding_på_samme_sak_skal_oppdatere_raden_og_deaktivere_forrige_perioder() {
        var aktørId = AktørId.dummy();
        var saksnummer = SAKSNUMMER;

        tjeneste.motta(YTELSE, request(aktørId, saksnummer, List.of(innvilget(FOM, TOM)), List.of()));
        flushOgTøm();
        var førsteId = fagsakRepository.hentForSaksnummer(saksnummer).orElseThrow().getId();

        var nyTom = TOM.plusMonths(6);
        tjeneste.motta(YTELSE, request(aktørId, saksnummer, List.of(innvilget(FOM, nyTom)), List.of()));
        flushOgTøm();

        assertThat(fagsakRepository.hentForAktørOgYtelse(aktørId, YTELSE)).hasSize(1);

        var fagsak = fagsakRepository.hentForSaksnummer(saksnummer).orElseThrow();
        assertThat(fagsak.getId()).isEqualTo(førsteId);
        assertThat(fagsak.getAktivePerioder())
            .singleElement()
            .extracting(p -> p.getPeriode().getTomDato())
            .isEqualTo(nyTom);
        assertThat(allePerioder(fagsak)).hasSize(2);
        assertThat(inaktivePerioder(fagsak))
            .singleElement()
            .extracting(p -> p.getPeriode().getTomDato())
            .isEqualTo(TOM);
    }

    @Test
    void melding_uten_perioder_skal_gi_fagsak_uten_aktive_perioder() {
        var aktørId = AktørId.dummy();

        tjeneste.motta(YTELSE, request(aktørId, SAKSNUMMER, List.of(), List.of()));
        flushOgTøm();

        assertThat(fagsakRepository.hentForAktørOgYtelse(aktørId, YTELSE))
            .singleElement()
            .extracting(FagSakEntitet::getAktivePerioder)
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.LIST)
            .isEmpty();
    }

    @Test
    void flere_saker_på_samme_deltaker_skal_lagres_hver_for_seg() {
        var aktørId = AktørId.dummy();

        tjeneste.motta(YTELSE, request(aktørId, SAKSNUMMER, List.of(innvilget(FOM, TOM)), List.of()));
        tjeneste.motta(YTELSE, request(aktørId, SAKSNUMMER,
            List.of(innvilget(FOM.plusYears(2), TOM.plusYears(2))), List.of()));
        flushOgTøm();

        assertThat(fagsakRepository.hentForAktørOgYtelse(aktørId, YTELSE)).hasSize(2);
    }

    @Test
    void skal_koble_mottatte_søknader_til_fagsaken() {
        var aktørId = AktørId.dummy();
        var søknadId = UUID.randomUUID();
        søknadHendelseRepository.lagre(new SøknadHendelseEntitet(søknadId, aktørId, YTELSE, LocalDateTime.of(2025, 1, 2, 10, 30)));
        flushOgTøm();

        var saksnummer = SAKSNUMMER;
        tjeneste.motta(YTELSE, request(aktørId, saksnummer, List.of(innvilget(FOM, TOM)), List.of(mottattSøknad(søknadId))));
        flushOgTøm();

        var søknader = søknadHendelseRepository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE);
        assertThat(søknader).singleElement()
            .extracting(s -> s.getMottattIFagsak().getSaksnummer())
            .isEqualTo(saksnummer);
    }

    @Test
    void skal_ikke_koble_søknader_ung_sak_ikke_har_meldt_inn() {
        var aktørId = AktørId.dummy();
        søknadHendelseRepository.lagre(new SøknadHendelseEntitet(UUID.randomUUID(), aktørId, YTELSE, LocalDateTime.of(2025, 1, 2, 10, 30)));
        flushOgTøm();

        tjeneste.motta(YTELSE, request(aktørId, SAKSNUMMER, List.of(innvilget(FOM, TOM)), List.of(mottattSøknad(UUID.randomUUID()))));
        flushOgTøm();

        assertThat(søknadHendelseRepository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE))
            .singleElement()
            .extracting(SøknadHendelseEntitet::getMottattIFagsak)
            .isNull();
    }

    private List<VedtakPeriodeEntitet> allePerioder(FagSakEntitet fagsak) {
        return entityManager.createQuery(
                "SELECT p FROM VedtakPeriode p WHERE p.fagsak = :fagsak", VedtakPeriodeEntitet.class)
            .setParameter("fagsak", fagsak)
            .getResultList();
    }

    private List<VedtakPeriodeEntitet> inaktivePerioder(FagSakEntitet fagsak) {
        return entityManager.createQuery(
                "SELECT p FROM VedtakPeriode p WHERE p.fagsak = :fagsak AND p.aktiv = false", VedtakPeriodeEntitet.class)
            .setParameter("fagsak", fagsak)
            .getResultList();
    }

    private void flushOgTøm() {
        entityManager.flush();
        entityManager.clear();
    }

    private static VedtakPeriodeDto innvilget(LocalDate fom, LocalDate tom) {
        return new VedtakPeriodeDto(new Periode(fom, tom), VedtakResultatType.INNVILGET);
    }

    private static MottattSøknadDto mottattSøknad(UUID søknadId) {
        return new MottattSøknadDto(søknadId, LocalDate.of(2025, 1, 2));
    }

    private static FagSakRequest request(AktørId aktørId, Saksnummer saksnummer,
                                         List<VedtakPeriodeDto> perioder, List<MottattSøknadDto> mottatteSøknader) {
        return new FagSakRequest(aktørId, saksnummer, perioder, mottatteSøknader);
    }
}
