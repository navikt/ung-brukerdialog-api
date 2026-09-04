package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(CdiAwareExtension.class)
@ExtendWith(JpaExtension.class)
class SøknadHendelseRepositoryTest {

    @Inject
    private EntityManager entityManager;

    @Inject
    private SøknadHendelseRepository repository;

    @Test
    void skal_hente_aktive_søknadshendelser_for_aktør_og_ytelsetype_sortert_nyeste_først() {
        var aktørId = AktørId.dummy();
        var annenAktørId = AktørId.dummy();
        var eldsteSøknadId = UUID.randomUUID();
        var nyesteSøknadId = UUID.randomUUID();
        var eldsteMottatt = LocalDateTime.of(2025, 1, 2, 10, 30);
        var nyesteMottatt = LocalDateTime.of(2025, 6, 3, 8, 15);

        repository.lagre(new SøknadHendelseEntitet(eldsteSøknadId, aktørId, FagsakYtelseType.AKTIVITETSPENGER, eldsteMottatt));
        repository.lagre(new SøknadHendelseEntitet(nyesteSøknadId, aktørId, FagsakYtelseType.AKTIVITETSPENGER, nyesteMottatt));
        entityManager.flush();
        entityManager.clear();

        var hendelser = repository.hentAktivSøknadForAktørOgYtelse(aktørId, FagsakYtelseType.AKTIVITETSPENGER);
        assertThat(hendelser)
            .extracting(SøknadHendelseEntitet::getSøknadId)
            .containsExactly(nyesteSøknadId, eldsteSøknadId);
        assertThat(hendelser.getFirst().getAktørId()).isEqualTo(aktørId);
        assertThat(hendelser.getFirst().getYtelseType()).isEqualTo(FagsakYtelseType.AKTIVITETSPENGER);
        assertThat(hendelser.getFirst().getMottatt()).isEqualTo(nyesteMottatt);

        assertThat(repository.hentAktivSøknadForAktørOgYtelse(annenAktørId, FagsakYtelseType.AKTIVITETSPENGER)).isEmpty();

        var nyeste = hendelser.getFirst();
        nyeste.deaktiver();
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.hentAktivSøknadForAktørOgYtelse(aktørId, FagsakYtelseType.AKTIVITETSPENGER))
            .extracting(SøknadHendelseEntitet::getSøknadId)
            .containsExactly(eldsteSøknadId);
        assertThat(entityManager.find(SøknadHendelseEntitet.class, nyeste.getId())).isNotNull();
    }
}
