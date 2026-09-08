package no.nav.ung.brukerdialog.sak.fagsak;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.vedtak.MottaFagsakRequest;
import no.nav.ung.brukerdialog.kontrakt.vedtak.MottattSøknadDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakPeriodeDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.FagsakYtelseType;
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
import static org.assertj.core.api.Assertions.tuple;

@ExtendWith(CdiAwareExtension.class)
@ExtendWith(JpaExtension.class)
class FagsakTjenesteTest {

    private static final FagsakYtelseType YTELSE = FagsakYtelseType.AKTIVITETSPENGER;
    private static final Saksnummer SAKSNUMMER = new Saksnummer("SAK1234");
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
    void skal_lagre_fagsak_med_perioder_og_koble_mottatt_søknad() {
        var aktørId = AktørId.dummy();
        var søknadId = UUID.randomUUID();
        var avslåttFom = TOM.plusDays(1);
        var avslåttTom = TOM.plusMonths(3);
        søknadHendelseRepository.lagre(new SøknadHendelseEntitet(søknadId, aktørId, YTELSE, LocalDateTime.of(2025, 1, 2, 10, 30)));
        flushOgTøm();

        tjeneste.motta(YTELSE, new MottaFagsakRequest(aktørId, SAKSNUMMER, List.of(
            new VedtakPeriodeDto(new Periode(FOM, TOM), VedtakResultatType.INNVILGET),
            new VedtakPeriodeDto(new Periode(avslåttFom, avslåttTom), VedtakResultatType.AVSLÅTT)
        ), List.of(new MottattSøknadDto(søknadId, LocalDate.of(2025, 1, 2)))));
        flushOgTøm();

        var fagsak = fagsakRepository.hentForSaksnummer(SAKSNUMMER).orElseThrow();
        assertThat(fagsak.getAktivePerioder())
            .extracting(VedtakPeriodeEntitet::getPeriode, VedtakPeriodeEntitet::getResultat)
            .containsExactlyInAnyOrder(
                tuple(DatoIntervallEntitet.fra(FOM, TOM), VedtakResultatType.INNVILGET),
                tuple(DatoIntervallEntitet.fra(avslåttFom, avslåttTom), VedtakResultatType.AVSLÅTT));

        assertThat(søknadHendelseRepository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE))
            .singleElement()
            .extracting(it -> it.getMottattIFagsak().getId())
            .isEqualTo(fagsak.getId());
    }

    private void flushOgTøm() {
        entityManager.flush();
        entityManager.clear();
    }
}
