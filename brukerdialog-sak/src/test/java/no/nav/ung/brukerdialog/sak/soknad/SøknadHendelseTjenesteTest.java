package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.soknad.OpprettSøknadHendelseRequest;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.kontrakt.vedtak.FagSakRequest;
import no.nav.ung.brukerdialog.kontrakt.vedtak.MottattSøknadDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakPeriodeDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.fagsak.FagsakTjeneste;
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

    @Inject
    private FagsakTjeneste fagsakTjeneste;

    @Test
    void skal_registrere_ny_søknadshendelse() {
        var aktørId = AktørId.dummy();
        var request = request();

        tjeneste.registrer(aktørId, YTELSE, request);

        var lagret = repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE);
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

        assertThat(repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE)).hasSize(1);
    }

    @Test
    void skal_kunne_sende_førstegangssøknad_når_bruker_ikke_har_søkt_før() {
        var tilgjengeligSøknad = tjeneste.finnTilgjengeligSøknad(AktørId.dummy(), YTELSE);

        assertThat(tilgjengeligSøknad.type()).isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);
        assertThat(tilgjengeligSøknad.harUbehandletSøknad()).isFalse();
    }

    @Test
    void skal_ikke_kunne_søke_når_bruker_allerede_har_søkt() {
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
    void deaktivert_søknad_skal_ikke_bruker_deltakeren_fra_å_søke_på_nytt() {
        var aktørId = AktørId.dummy();
        tjeneste.registrer(aktørId, YTELSE, request());

        var førsteSøknad = repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE).getFirst();
        var førsteSøknadId = førsteSøknad.getId();
        førsteSøknad.deaktiver();
        entityManager.flush();
        entityManager.clear();

        assertThat(tjeneste.finnTilgjengeligSøknad(aktørId, YTELSE).type())
            .isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);

        var nyRequest = request();
        tjeneste.registrer(aktørId, YTELSE, nyRequest);

        assertThat(repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE))
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

    @Test
    void deltaker_med_fullt_avslag_skal_kunne_sende_ny_førstegangssøknad() {
        var aktørId = AktørId.dummy();
        var behandletRequest = request();
        tjeneste.registrer(aktørId, YTELSE, behandletRequest);
        behandleISak(aktørId, behandletRequest.søknadId(), List.of());

        assertThat(tjeneste.finnTilgjengeligSøknad(aktørId, YTELSE).type())
            .isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);

        var nyRequest = request();
        tjeneste.registrer(aktørId, YTELSE, nyRequest);

        assertThat(repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE)).isNotNull();
    }

    @Test
    void deltaker_med_innvilgelse_innenfor_vinduet_skal_få_ny_periode_søknad() {
        var aktørId = AktørId.dummy();
        var behandletRequest = request();
        tjeneste.registrer(aktørId, YTELSE, behandletRequest);
        behandleISak(aktørId, behandletRequest.søknadId(), List.of(periodeMedTom(LocalDate.now())));

        var tilgjengelig = tjeneste.finnTilgjengeligSøknad(aktørId, YTELSE);
        assertThat(tilgjengelig.type()).isEqualTo(TilgjengeligSøknadType.NY_PERIODE_SØKNAD);
        assertThat(tilgjengelig.harInnsyn()).isTrue();

        var nyRequest = request();
        tjeneste.registrer(aktørId, YTELSE, nyRequest);

          assertThat(repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE)).isNotNull();
    }

    @Test
    void deltaker_med_løpende_innvilgelse_utenfor_vinduet_skal_avvises_ved_registrering() {
        var aktørId = AktørId.dummy();
        var behandletRequest = request();
        tjeneste.registrer(aktørId, YTELSE, behandletRequest);
        behandleISak(aktørId, behandletRequest.søknadId(), List.of(periodeMedTom(LocalDate.now().plusMonths(6))));

        assertThat(tjeneste.finnTilgjengeligSøknad(aktørId, YTELSE).type())
            .isEqualTo(TilgjengeligSøknadType.INGEN);

        assertThatThrownBy(() -> tjeneste.registrer(aktørId, YTELSE, request()))
            .isInstanceOf(SøknadIkkeTilgjengeligException.class);
    }

    @Test
    void deltaker_som_avsluttet_programmet_for_lenge_siden_skal_få_førstegangssøknad() {
        var aktørId = AktørId.dummy();
        var behandletRequest = request();
        tjeneste.registrer(aktørId, YTELSE, behandletRequest);
        behandleISak(aktørId, behandletRequest.søknadId(), List.of(periodeMedTom(LocalDate.now().minusWeeks(53))));

        assertThat(tjeneste.finnTilgjengeligSøknad(aktørId, YTELSE).type())
            .isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);
    }

    private void behandleISak(AktørId aktørId, UUID søknadId, List<VedtakPeriodeDto> vedtaksperioder) {
        fagsakTjeneste.motta(YTELSE, new FagSakRequest(
            aktørId,
            new Saksnummer("1234"),
            vedtaksperioder,
            List.of(new MottattSøknadDto(søknadId, MOTTATT.toLocalDate()))));
        entityManager.flush();
        entityManager.clear();
    }

    private VedtakPeriodeDto periodeMedTom(LocalDate tom) {
        return new VedtakPeriodeDto(new Periode(tom.minusWeeks(52).plusDays(1), tom), VedtakResultatType.INNVILGET);
    }

}
