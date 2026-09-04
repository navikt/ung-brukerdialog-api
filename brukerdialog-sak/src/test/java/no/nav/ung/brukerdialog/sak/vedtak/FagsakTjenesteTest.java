package no.nav.ung.brukerdialog.sak.vedtak;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.vedtak.FagSakRequest;
import no.nav.ung.brukerdialog.kontrakt.vedtak.MottattSøknadDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakPeriodeDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.fagsak.FagsakRepository;
import no.nav.ung.brukerdialog.sak.fagsak.FagsakTjeneste;
import no.nav.ung.brukerdialog.sak.fagsak.VedtakPeriodeEntitet;
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
    private static final LocalDateTime VEDTAKSTIDSPUNKT = LocalDateTime.of(2025, 2, 1, 12, 0);
    private static final LocalDate FOM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2025, 12, 31);

    @Inject
    private EntityManager entityManager;

    @Inject
    private FagsakTjeneste tjeneste;

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private SøknadHendelseRepository søknadHendelseRepository;

    @Test
    void skal_lagre_vedtaksstatus_med_perioder_og_finne_den_igjen_på_aktør() {
        var aktørId = AktørId.dummy();
        var saksnummer = saksnummer();

        tjeneste.motta(YTELSE, request(aktørId, saksnummer, List.of(new Periode(FOM, TOM)), List.of()));
        flushOgTøm();

        var lagret = fagsakRepository.hentForAktørOgYtelse(aktørId, YTELSE);
        assertThat(lagret).hasSize(1);
        assertThat(lagret.getFirst().getSaksnummer()).isEqualTo(saksnummer);
        List<VedtakPeriodeEntitet> perioder = lagret.getFirst().getPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.getFirst().getPeriode()).isEqualTo(DatoIntervallEntitet.fra(FOM, TOM));
    }

    @Test
    void flere_saker_på_samme_deltaker_skal_lagres_hver_for_seg() {
        var aktørId = AktørId.dummy();

        tjeneste.motta(YTELSE, request(aktørId, saksnummer(), List.of(new Periode(FOM, TOM)), List.of()));
        tjeneste.motta(YTELSE, request(aktørId, saksnummer(), List.of(new Periode(FOM.plusYears(2), TOM.plusYears(2))), List.of()));
        flushOgTøm();

        assertThat(fagsakRepository.hentForAktørOgYtelse(aktørId, YTELSE)).hasSize(2);
    }

    @Test
    void skal_markere_behandlede_søknader_med_vedtakstidspunktet() {
        var aktørId = AktørId.dummy();
        var søknadId = UUID.randomUUID();
        søknadHendelseRepository.lagre(new SøknadHendelseEntitet(søknadId, aktørId, YTELSE, LocalDateTime.of(2025, 1, 2, 10, 30)));
        flushOgTøm();

        tjeneste.motta(YTELSE, request(aktørId, saksnummer(), List.of(new Periode(FOM, TOM)), List.of(behandletSøknad(søknadId))));
        flushOgTøm();

        assertThat(søknadHendelseRepository.hentAktiveSøknadForAktørOgYtelse(aktørId, YTELSE)).hasSize(1);
    }

    private void flushOgTøm() {
        entityManager.flush();
        entityManager.clear();
    }

    private static Saksnummer saksnummer() {
        return new Saksnummer(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    private static MottattSøknadDto behandletSøknad(UUID søknadId) {
        return new MottattSøknadDto(søknadId, LocalDate.of(2025, 1, 2));
    }

    private static FagSakRequest request(AktørId aktørId, Saksnummer saksnummer,
                                         List<Periode> perioder, List<MottattSøknadDto> behandledeSøknader) {
        return new FagSakRequest(aktørId, saksnummer, perioder.stream().map(it -> new VedtakPeriodeDto(it, VedtakResultatType.INNVILGET)).toList(), behandledeSøknader);
    }
}
