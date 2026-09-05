package no.nav.ung.brukerdialog.sak.diagnostikk;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakPeriodeDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.fagsak.FagSakEntitet;
import no.nav.ung.brukerdialog.sak.fagsak.FagsakRepository;
import no.nav.ung.brukerdialog.sak.soknad.FagsakYtelseType;
import no.nav.ung.brukerdialog.sak.soknad.SøknadHendelseEntitet;
import no.nav.ung.brukerdialog.sak.soknad.SøknadHendelseRepository;
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
class DiagnostikkSakTjenesteTest {

    private static final FagsakYtelseType YTELSE = FagsakYtelseType.AKTIVITETSPENGER;
    private static final LocalDate FOM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2025, 6, 30);

    @Inject
    private EntityManager entityManager;

    @Inject
    private DiagnostikkSakTjeneste diagnostikkSakTjeneste;

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private SøknadHendelseRepository søknadHendelseRepository;

    @Test
    void dumper_også_deaktiverte_vedtaksperioder() {
        var aktørId = AktørId.dummy();
        var fagsak = new FagSakEntitet(aktørId, YTELSE, nyttSaksnummer());
        fagsak.erstattPerioder(List.of(new VedtakPeriodeDto(new Periode(FOM, TOM), VedtakResultatType.INNVILGET)));
        fagsakRepository.lagre(fagsak);

        var oppdatert = fagsakRepository.hentForSaksnummer(fagsak.getSaksnummer()).orElseThrow();
        oppdatert.erstattPerioder(List.of(new VedtakPeriodeDto(new Periode(FOM, TOM.plusMonths(1)), VedtakResultatType.AVSLÅTT)));
        fagsakRepository.lagre(oppdatert);
        entityManager.flush();
        entityManager.clear();

        var perioder = diagnostikkSakTjeneste.dump(aktørId).fagsaker().getFirst().perioder();

        assertThat(perioder).hasSize(2);
        assertThat(perioder).extracting("aktiv").containsExactlyInAnyOrder(false, true);
    }

    @Test
    void dumper_også_deaktiverte_søknadhendelser() {
        var aktørId = AktørId.dummy();
        var hendelse = new SøknadHendelseEntitet(UUID.randomUUID(), aktørId, YTELSE, LocalDateTime.now());
        hendelse.deaktiver();
        søknadHendelseRepository.lagre(hendelse);
        entityManager.flush();
        entityManager.clear();

        var hendelser = diagnostikkSakTjeneste.dump(aktørId).søknadHendelser();

        assertThat(hendelser).hasSize(1);
        assertThat(hendelser.getFirst().aktiv()).isFalse();
    }

    @Test
    void dumper_søknad_som_ikke_er_mottatt_i_fagsak() {
        var aktørId = AktørId.dummy();
        søknadHendelseRepository.lagre(new SøknadHendelseEntitet(UUID.randomUUID(), aktørId, YTELSE, LocalDateTime.now()));
        entityManager.flush();
        entityManager.clear();

        var dump = diagnostikkSakTjeneste.dump(aktørId);

        assertThat(dump.fagsaker()).isEmpty();
        assertThat(dump.søknadHendelser()).hasSize(1);
        assertThat(dump.søknadHendelser().getFirst().mottattISaksnummer()).isNull();
    }

    @Test
    void finner_aktør_for_saksnummer() {
        var aktørId = AktørId.dummy();
        var saksnummer = nyttSaksnummer();
        fagsakRepository.lagre(new FagSakEntitet(aktørId, YTELSE, saksnummer));
        entityManager.flush();
        entityManager.clear();

        assertThat(diagnostikkSakTjeneste.finnAktørForSaksnummer(saksnummer)).contains(aktørId);
    }

    private static Saksnummer nyttSaksnummer() {
        return new Saksnummer("SAK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
    }
}
