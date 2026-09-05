package no.nav.ung.brukerdialog.sak.fagsak;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakPeriodeDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.soknad.FagsakYtelseType;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Periode;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        fagsakRepository.lagre(new FagSakEntitet(AktørId.dummy(), YTELSE, SAKSNUMMER));

        // Transaksjon 1 leser saken
        var kopi1 = fagsakRepository.hentForSaksnummer(SAKSNUMMER).orElseThrow();
        entityManager.detach(kopi1);

        // Transaksjon 2 leser samme sak og rekker å legge inn sine perioder
        var kopi2 = fagsakRepository.hentForSaksnummer(SAKSNUMMER).orElseThrow();
        kopi2.erstattPerioder(List.of(new VedtakPeriodeDto(new Periode(FOM, TOM), VedtakResultatType.INNVILGET)));
        fagsakRepository.lagre(kopi2);
        entityManager.detach(kopi2);

        // Transaksjon 1 forsøker å skrive sin utdaterte versjon
        assertThatThrownBy(() -> {
            entityManager.merge(kopi1);
            entityManager.flush();
        }).isInstanceOf(OptimisticLockException.class);
    }
}
