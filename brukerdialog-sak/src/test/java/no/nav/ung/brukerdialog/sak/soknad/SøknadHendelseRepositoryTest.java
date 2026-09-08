package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakPeriodeDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.FagsakYtelseType;
import no.nav.ung.brukerdialog.sak.fagsak.FagsakEntitet;
import no.nav.ung.brukerdialog.sak.fagsak.FagsakRepository;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Periode;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(CdiAwareExtension.class)
@ExtendWith(JpaExtension.class)
class SøknadHendelseRepositoryTest {

    @Inject
    private EntityManager entityManager;

    private static final FagsakYtelseType YTELSE = FagsakYtelseType.AKTIVITETSPENGER;
    private static final Saksnummer SAKSNUMMER = new Saksnummer("SAK1234");

    @Inject
    private SøknadHendelseRepository repository;

    @Inject
    private FagsakRepository fagsakRepository;

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

        var hendelser = repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, FagsakYtelseType.AKTIVITETSPENGER);
        assertThat(hendelser)
            .extracting(SøknadHendelseEntitet::getSøknadId)
            .containsExactly(nyesteSøknadId, eldsteSøknadId);
        assertThat(hendelser.getFirst().getAktørId()).isEqualTo(aktørId);
        assertThat(hendelser.getFirst().getYtelseType()).isEqualTo(FagsakYtelseType.AKTIVITETSPENGER);
        assertThat(hendelser.getFirst().getMottatt()).isEqualTo(nyesteMottatt);

        assertThat(repository.hentAktiveSøknaderForAktørOgYtelse(annenAktørId, FagsakYtelseType.AKTIVITETSPENGER)).isEmpty();

        var nyeste = hendelser.getFirst();
        nyeste.deaktiver();
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, FagsakYtelseType.AKTIVITETSPENGER))
            .extracting(SøknadHendelseEntitet::getSøknadId)
            .containsExactly(eldsteSøknadId);
        assertThat(entityManager.find(SøknadHendelseEntitet.class, nyeste.getId())).isNotNull();
    }

    @Test
    void skal_koble_søknad_ung_sak_har_meldt_inn_til_fagsaken() {
        var aktørId = AktørId.dummy();
        var søknadId = UUID.randomUUID();
        repository.lagre(søknadHendelse(søknadId, aktørId));
        var fagsak = fagsak(aktørId);
        flushOgTøm();

        repository.markerMottattIFagsak(aktørId, YTELSE, fagsak, Set.of(søknadId));
        flushOgTøm();

        assertThat(repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE))
            .singleElement()
            .extracting(it -> it.getMottattIFagsak().getSaksnummer())
            .isEqualTo(SAKSNUMMER);
    }

    @Test
    void skal_ikke_koble_søknad_ung_sak_ikke_har_meldt_inn() {
        var aktørId = AktørId.dummy();
        repository.lagre(søknadHendelse(UUID.randomUUID(), aktørId));
        var fagsak = fagsak(aktørId);
        flushOgTøm();

        repository.markerMottattIFagsak(aktørId, YTELSE, fagsak, Set.of(UUID.randomUUID()));
        flushOgTøm();

        assertThat(repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE))
            .singleElement()
            .extracting(SøknadHendelseEntitet::getMottattIFagsak)
            .isNull();
    }

    @Test
    void skal_ikke_flytte_søknad_som_allerede_er_koblet_til_en_annen_fagsak() {
        var aktørId = AktørId.dummy();
        var søknadId = UUID.randomUUID();
        repository.lagre(søknadHendelse(søknadId, aktørId));
        var første = fagsak(aktørId);
        flushOgTøm();
        repository.markerMottattIFagsak(aktørId, YTELSE, første, Set.of(søknadId));
        flushOgTøm();

        fagsakRepository.lagreFagsak(aktørId, YTELSE, new Saksnummer("SAK5678"), List.of());
        var andre = fagsakRepository.hentForSaksnummer(new Saksnummer("SAK5678")).orElseThrow();
        repository.markerMottattIFagsak(aktørId, YTELSE, andre, Set.of(søknadId));
        flushOgTøm();

        assertThat(repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE))
            .singleElement()
            .extracting(it -> it.getMottattIFagsak().getSaksnummer())
            .isEqualTo(SAKSNUMMER);
    }

    private FagsakEntitet fagsak(AktørId aktørId) {
        return fagsakRepository.lagreFagsak(aktørId, YTELSE, SAKSNUMMER,
            List.of(new VedtakPeriodeDto(new Periode(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)),
                VedtakResultatType.INNVILGET)));
    }

    private static SøknadHendelseEntitet søknadHendelse(UUID søknadId, AktørId aktørId) {
        return new SøknadHendelseEntitet(søknadId, aktørId, YTELSE, LocalDateTime.of(2025, 1, 2, 10, 30));
    }

    private void flushOgTøm() {
        entityManager.flush();
        entityManager.clear();
    }
}
