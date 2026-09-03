package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.soknad.OpprettSøknadHendelseRequest;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(CdiAwareExtension.class)
@ExtendWith(JpaExtension.class)
class SøknadHendelseTjenesteTest {

    private static final LocalDateTime MOTTATT = LocalDateTime.of(2025, 1, 2, 10, 30);
    private static final FagsakYtelseType YTELSE = FagsakYtelseType.AKTIVITETSPENGER;

    @Inject
    private EntityManager entityManager;

    @Inject
    private SøknadHendelseTjeneste tjeneste;

    @Inject
    private SøknadHendelseRepository repository;

    @Test
    void skal_registrere_ny_søknadshendelse() {
        var aktørId = AktørId.dummy();
        var request = request();

        tjeneste.registrer(aktørId, YTELSE, request);

        var lagret = repository.hentForSøknadId(request.søknadId());
        assertThat(lagret).isPresent();
        assertThat(lagret.get().getAktørId()).isEqualTo(aktørId);
        assertThat(lagret.get().getYtelseType()).isEqualTo(YTELSE);
        assertThat(lagret.get().getMottatt()).isEqualTo(MOTTATT);
    }

    @Test
    void skal_være_idempotent_på_søknadId_slik_at_innsender_trygt_kan_prøve_på_nytt() {
        var aktørId = AktørId.dummy();
        var request = request();

        tjeneste.registrer(aktørId, YTELSE, request);
        tjeneste.registrer(aktørId, YTELSE, request);

        assertThat(repository.hentForAktørOgYtelse(aktørId, YTELSE)).hasSize(1);
    }

    @Test
    void skal_kunne_sende_førstegangssøknad_når_deltakeren_ikke_har_søkt_før() {
        var tilgjengeligSøknad = tjeneste.finnTilgjengeligSøknad(AktørId.dummy(), YTELSE);

        assertThat(tilgjengeligSøknad.type()).isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);
        assertThat(tilgjengeligSøknad.harUbehandletSøknad()).isFalse();
    }

    @Test
    void skal_ikke_kunne_søke_når_deltakeren_allerede_har_søkt() {
        var aktørId = AktørId.dummy();
        tjeneste.registrer(aktørId, YTELSE, request());

        var tilgjengeligSøknad = tjeneste.finnTilgjengeligSøknad(aktørId, YTELSE);

        assertThat(tilgjengeligSøknad.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
        assertThat(tilgjengeligSøknad.harUbehandletSøknad()).isTrue();

        assertThatThrownBy(() -> tjeneste.registrer(aktørId, YTELSE, request()))
            .isInstanceOf(SøknadIkkeTilgjengeligException.class)
            .hasMessageContaining(MOTTATT.toString());
    }

    @Test
    void deaktivert_søknad_skal_ikke_sperre_deltakeren_fra_å_søke_på_nytt() {
        var aktørId = AktørId.dummy();
        var førsteRequest = request();
        tjeneste.registrer(aktørId, YTELSE, førsteRequest);

        repository.hentForSøknadId(førsteRequest.søknadId()).orElseThrow().deaktiver();
        entityManager.flush();
        entityManager.clear();

        assertThat(tjeneste.finnTilgjengeligSøknad(aktørId, YTELSE).type())
            .isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);

        var nyRequest = request();
        tjeneste.registrer(aktørId, YTELSE, nyRequest);

        assertThat(repository.hentForAktørOgYtelse(aktørId, YTELSE))
            .extracting(SøknadHendelseEntitet::getSøknadId)
            .containsExactly(nyRequest.søknadId());
        assertThat(repository.hentForSøknadId(førsteRequest.søknadId())).isPresent();
    }

    private static OpprettSøknadHendelseRequest request() {
        return new OpprettSøknadHendelseRequest(UUID.randomUUID(), MOTTATT);
    }
}
