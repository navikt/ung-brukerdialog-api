package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;
import no.nav.ung.brukerdialog.db.util.JpaExtension;
import no.nav.ung.brukerdialog.kontrakt.soknad.OpprettSøknadHendelseRequest;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse;
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType;
import no.nav.ung.brukerdialog.kontrakt.vedtak.MottaFagsakRequest;
import no.nav.ung.brukerdialog.kontrakt.vedtak.MottattSøknadDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakPeriodeDto;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.sak.FagsakYtelseType;
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
        LocalDateTime mottatt = LocalDateTime.now().minusWeeks(1);
        var request = request(mottatt);

        tjeneste.registrer(aktørId, YTELSE, request);

        var lagret = repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE);
        assertThat(lagret).hasSize(1);
        assertThat(lagret.getFirst().getSøknadId()).isEqualTo(request.søknadId());
        assertThat(lagret.getFirst().getAktørId()).isEqualTo(aktørId);
        assertThat(lagret.getFirst().getYtelseType()).isEqualTo(YTELSE);
        assertThat(lagret.getFirst().getMottatt()).isEqualTo(mottatt);
    }

    @Test
    void skal_være_idempotent_på_søknadId_slik_at_innsender_trygt_kan_prøve_på_nytt() {
        var aktørId = AktørId.dummy();
        LocalDateTime mottatt = LocalDateTime.now().minusWeeks(1);
        var request = request(mottatt);

        tjeneste.registrer(aktørId, YTELSE, request);
        entityManager.flush();
        entityManager.clear();
        // Ny UUID-instans med samme verdi, slik ei deserialisert gjeninnsending fra kbp ser ut.
        tjeneste.registrer(aktørId, YTELSE, request(UUID.fromString(request.søknadId().toString()), mottatt));

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
        LocalDateTime mottatt = LocalDateTime.now().minusWeeks(1);
        tjeneste.registrer(aktørId, YTELSE, request(mottatt));

        var tilgjengeligSøknad = finnTilgjengeligSøknad(aktørId);

        assertThat(tilgjengeligSøknad.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
        assertThat(tilgjengeligSøknad.harUbehandletSøknad()).isTrue();

        assertThatThrownBy(() -> tjeneste.registrer(aktørId, YTELSE, request(LocalDateTime.now())))
            .isInstanceOf(SøknadIkkeTilgjengeligException.class)
            .hasMessageContaining(mottatt.toString());
    }

    @Test
    void deaktivert_søknad_skal_ikke_hindre_bruker_fra_å_søke_på_nytt() {
        var aktørId = AktørId.dummy();
        LocalDateTime mottatt = LocalDateTime.now().minusWeeks(1);
        tjeneste.registrer(aktørId, YTELSE, request(mottatt));

        var førsteSøknad = repository.hentAktiveSøknaderForAktørOgYtelse(aktørId, YTELSE).getFirst();
        var førsteSøknadId = førsteSøknad.getId();
        førsteSøknad.deaktiver();
        entityManager.flush();
        entityManager.clear();

        assertThat(finnTilgjengeligSøknad(aktørId).type())
            .isEqualTo(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD);

    }

    private static OpprettSøknadHendelseRequest request(LocalDateTime mottatt) {
        return request(UUID.randomUUID(), mottatt);
    }

    private static OpprettSøknadHendelseRequest request(UUID søknadId, LocalDateTime mottatt) {
        return new OpprettSøknadHendelseRequest(søknadId, mottatt);
    }

    private TilgjengeligSøknadResponse finnTilgjengeligSøknad(AktørId aktørId) {
        return tjeneste.finnTilgjengeligSøknad(aktørId, YTELSE);
    }

    @Test
    void bruker_med_innvilgelse_innenfor_vinduet_skal_få_ny_periode_søknad() {
        var aktørId = AktørId.dummy();
        LocalDateTime mottatt = LocalDateTime.now().minusWeeks(50);
        var behandletRequest = request(mottatt);
        tjeneste.registrer(aktørId, YTELSE, behandletRequest);
        mottaSak(aktørId, behandletRequest.søknadId(), behandletRequest.mottatt(), List.of(innvilgetTom(LocalDate.now())));

        var tilgjengelig = finnTilgjengeligSøknad(aktørId);
        assertThat(tilgjengelig.type()).isEqualTo(TilgjengeligSøknadType.NY_PERIODE_SØKNAD);
        assertThat(tilgjengelig.harInnsyn()).isTrue();

        tjeneste.registrer(aktørId, YTELSE, request(LocalDateTime.now()));

        var etterNySøknad = finnTilgjengeligSøknad(aktørId);
        assertThat(etterNySøknad.type()).isEqualTo(TilgjengeligSøknadType.INGEN);
        assertThat(etterNySøknad.harUbehandletSøknad()).isTrue();
        assertThatThrownBy(() -> tjeneste.registrer(aktørId, YTELSE, request(mottatt)))
            .isInstanceOf(SøknadIkkeTilgjengeligException.class);
    }

    @Test
    void bruker_med_løpende_innvilgelse_utenfor_vinduet_skal_avvises_ved_registrering() {
        var aktørId = AktørId.dummy();
        LocalDateTime mottatt = LocalDateTime.now().minusWeeks(50);
        var behandletRequest = request(mottatt);
        tjeneste.registrer(aktørId, YTELSE, behandletRequest);
        mottaSak(aktørId, behandletRequest.søknadId(), behandletRequest.mottatt(), List.of(innvilgetTom(LocalDate.now().plusMonths(6))));

        assertThat(finnTilgjengeligSøknad(aktørId).type())
            .isEqualTo(TilgjengeligSøknadType.INGEN);

        assertThatThrownBy(() -> tjeneste.registrer(aktørId, YTELSE, request(mottatt)))
            .isInstanceOf(SøknadIkkeTilgjengeligException.class)
            .hasMessage("Bruker kan ikke sende søknad nå.");
    }

    private void mottaSak(AktørId aktørId, UUID søknadId, LocalDateTime mottatt, List<VedtakPeriodeDto> vedtaksperioder) {
        fagsakTjeneste.motta(YTELSE, new MottaFagsakRequest(
            aktørId,
            new Saksnummer("1234"),
            vedtaksperioder,
            List.of(new MottattSøknadDto(søknadId, mottatt.toLocalDate()))));
        entityManager.flush();
        entityManager.clear();
    }

    private VedtakPeriodeDto innvilgetTom(LocalDate tom) {
        return new VedtakPeriodeDto(new Periode(tom.minusWeeks(52).plusDays(1), tom), VedtakResultatType.INNVILGET);
    }
}
