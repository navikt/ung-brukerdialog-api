package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.soknad.OpprettSøknadHendelseRequest;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.Disabled;
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

        var lagret = repository.hentForAktørOgYtelse(aktørId, YTELSE);
        assertThat(lagret).hasSize(1);
        assertThat(lagret.getFirst().getSøknadId()).isEqualTo(request.søknadId());
        assertThat(lagret.getFirst().getAktørId()).isEqualTo(aktørId);
        assertThat(lagret.getFirst().getYtelseType()).isEqualTo(YTELSE);
        assertThat(lagret.getFirst().getMottatt()).isEqualTo(MOTTATT);
    }

    @Test
    void skal_være_idempotent_på_søknadId_slik_at_innsender_trygt_kan_prøve_på_nytt() {
        var aktørId = AktørId.dummy();
        var request = request();

        tjeneste.registrer(aktørId, YTELSE, request);
        entityManager.flush();
        entityManager.clear();
        // Ny UUID-instans med samme verdi, slik ei deserialisert gjeninnsending fra kbp ser ut.
        tjeneste.registrer(aktørId, YTELSE, request(UUID.fromString(request.søknadId().toString())));

        assertThat(repository.hentForAktørOgYtelse(aktørId, YTELSE)).hasSize(1);
    }

    @Test
    void skal_kunne_sende_førstegangssøknad_når_deltakeren_ikke_har_søkt_før() {
        var tilgjengeligSøknad = tjeneste.finnTilgjengeligSøknad(AktørId.dummy(), YTELSE);

        assertThat(tilgjengeligSøknad.type()).isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);
        assertThat(tilgjengeligSøknad.harUbehandletSøknad()).isFalse();
    }

    @Test
    @Disabled("aktiveres i del 2 PR")
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
        tjeneste.registrer(aktørId, YTELSE, request());

        var førsteSøknad = repository.hentForAktørOgYtelse(aktørId, YTELSE).getFirst();
        var førsteSøknadId = førsteSøknad.getId();
        førsteSøknad.deaktiver();
        entityManager.flush();
        entityManager.clear();

        assertThat(tjeneste.finnTilgjengeligSøknad(aktørId, YTELSE).type())
            .isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);

        var nyRequest = request();
        tjeneste.registrer(aktørId, YTELSE, nyRequest);

        assertThat(repository.hentForAktørOgYtelse(aktørId, YTELSE))
            .extracting(SøknadHendelseEntitet::getSøknadId)
            .containsExactly(nyRequest.søknadId());
        assertThat(entityManager.find(SøknadHendelseEntitet.class, førsteSøknadId)).isNotNull();
    }

    private static OpprettSøknadHendelseRequest request() {
        return request(UUID.randomUUID());
    }

    private static OpprettSøknadHendelseRequest request(UUID søknadId) {
        return new OpprettSøknadHendelseRequest(søknadId, MOTTATT);
    }
}
