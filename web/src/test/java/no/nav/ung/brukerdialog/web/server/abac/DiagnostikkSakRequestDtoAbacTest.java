package no.nav.ung.brukerdialog.web.server.abac;

import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.ung.brukerdialog.kontrakt.sak.diagnostikk.DiagnostikkSakRequestDto;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnostikkSakRequestDtoAbacTest {

    private final AbacAttributtSupplier supplier = new AbacAttributtSupplier();

    @Test
    void aktørId_skal_legges_på_som_standard_abac_attributt() {
        var attributter = supplier.apply(new DiagnostikkSakRequestDto(new AktørId("1234567890123"), null, "feilsøking"));

        assertThat(attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID)).containsExactly("1234567890123");
    }

    @Test
    void saksnummer_skal_legges_på_som_standard_abac_attributt() {
        var attributter = supplier.apply(new DiagnostikkSakRequestDto(null, new Saksnummer("SAK1234"), "feilsøking"));

        assertThat(attributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER)).containsExactly("SAK1234");
    }

}
