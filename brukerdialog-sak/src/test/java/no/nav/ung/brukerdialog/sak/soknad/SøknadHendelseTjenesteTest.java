package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.soknad.OpprettSøknadHendelseRequest;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse;
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
        var tilgjengeligSøknad = finnTilgjengeligSøknad(AktørId.dummy());

        assertThat(tilgjengeligSøknad.type()).isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);
        assertThat(tilgjengeligSøknad.harUbehandletSøknad()).isFalse();
    }

    @Test
    void skal_ikke_kunne_søke_når_bruker_allerede_har_søkt() {
        var aktørId = AktørId.dummy();
        tjeneste.registrer(aktørId, YTELSE, request());

        var tilgjengeligSøknad = finnTilgjengeligSøknad(aktørId);

        assertThat(tilgjengeligSøknad.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
        assertThat(tilgjengeligSøknad.harUbehandletSøknad()).isTrue();

        assertThatThrownBy(() -> tjeneste.registrer(aktørId, YTELSE, request()))
            .isInstanceOf(SøknadIkkeTilgjengeligException.class)
            .hasMessageContaining(MOTTATT.toString());
    }

    @Test
    void deaktivert_søknad_skal_ikke_hindre_bruker_fra_å_søke_på_nytt() {
        var aktørId = AktørId.dummy();
        tjeneste.registrer(aktørId, YTELSE, request());

        var førsteSøknad = repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE).getFirst();
        var førsteSøknadId = førsteSøknad.getId();
        førsteSøknad.deaktiver();
        entityManager.flush();
        entityManager.clear();

        assertThat(finnTilgjengeligSøknad(aktørId).type())
            .isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);

    }

    private static OpprettSøknadHendelseRequest request() {
        return request(UUID.randomUUID());
    }

    private static OpprettSøknadHendelseRequest request(UUID søknadId) {
        return new OpprettSøknadHendelseRequest(søknadId, MOTTATT);
    }

    private TilgjengeligSøknadResponse finnTilgjengeligSøknad(AktørId aktørId) {
        return tjeneste.finnTilgjengeligSøknad(aktørId, YTELSE);
    }

    @Test
    void bruker_med_innvilgelse_innenfor_vinduet_skal_få_ny_periode_søknad() {
        var aktørId = AktørId.dummy();
        var behandletRequest = request();
        tjeneste.registrer(aktørId, YTELSE, behandletRequest);
        mottaSak(aktørId, behandletRequest.søknadId(), List.of(innvilgetTom(LocalDate.now())));

        var tilgjengelig = finnTilgjengeligSøknad(aktørId);
        assertThat(tilgjengelig.type()).isEqualTo(TilgjengeligSøknadType.NY_PERIODE_SØKNAD);
        assertThat(tilgjengelig.harInnsyn()).isTrue();

        tjeneste.registrer(aktørId, YTELSE, request());

        var etterNySøknad = finnTilgjengeligSøknad(aktørId);
        assertThat(etterNySøknad.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
        assertThat(etterNySøknad.harUbehandletSøknad()).isTrue();
        assertThatThrownBy(() -> tjeneste.registrer(aktørId, YTELSE, request()))
            .isInstanceOf(SøknadIkkeTilgjengeligException.class);
    }

    @Test
    void bruker_med_løpende_innvilgelse_utenfor_vinduet_skal_avvises_ved_registrering() {
        var aktørId = AktørId.dummy();
        var behandletRequest = request();
        tjeneste.registrer(aktørId, YTELSE, behandletRequest);
        mottaSak(aktørId, behandletRequest.søknadId(), List.of(innvilgetTom(LocalDate.now().plusMonths(6))));

        assertThat(finnTilgjengeligSøknad(aktørId).type())
            .isEqualTo(TilgjengeligSøknadType.INGEN);

        assertThatThrownBy(() -> tjeneste.registrer(aktørId, YTELSE, request()))
            .isInstanceOf(SøknadIkkeTilgjengeligException.class)
            .hasMessage("Bruker kan ikke sende søknad nå.");
    }

    private void mottaSak(AktørId aktørId, UUID søknadId, List<VedtakPeriodeDto> vedtaksperioder) {
        fagsakTjeneste.motta(YTELSE, new FagSakRequest(
            aktørId,
            new Saksnummer("1234"),
            vedtaksperioder,
            List.of(new MottattSøknadDto(søknadId, MOTTATT.toLocalDate()))));
        entityManager.flush();
        entityManager.clear();
    }

    private VedtakPeriodeDto innvilgetTom(LocalDate tom) {
        return new VedtakPeriodeDto(new Periode(tom.minusWeeks(52).plusDays(1), tom), VedtakResultatType.INNVILGET);
    }
}
