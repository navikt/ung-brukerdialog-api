package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bistand;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BekreftBistandOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BekreftBistandOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BistandsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BistandsvilkårIkkeOppfyltÅrsak;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Opphørsvarianten skilles fra periodevarianten utelukkende på om {@code tom} er satt – begge
 * lagres i samme tabell. Disse testene låser den rundturen fast i begge retninger.
 */
class BekreftBistandOppgaveDataMapperTest {

    private static final LocalDate FOM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2025, 1, 31);

    private final BekreftBistandOppgaveDataMapperFraDtoTilEntitet tilEntitet = new BekreftBistandOppgaveDataMapperFraDtoTilEntitet();
    private final BekreftBistandOppgaveDataMapperFraEntitetTilDto tilDto = new BekreftBistandOppgaveDataMapperFraEntitetTilDto();

    @Test
    void periodevariant_beholder_tom_gjennom_rundturen() {
        var dto = new BekreftBistandOppgavetypeDataDto(FOM, TOM, BistandsvilkårIkkeOppfyltÅrsak.IKKE_14A_VEDTAK,
            "Ingen oppfølging registrert.", BistandsavklaringKildeType.BRUKER, null);

        var entitet = (BekreftBistandOppgaveDataEntitet) tilEntitet.map(dto);

        assertThat(entitet.getFom()).isEqualTo(FOM);
        assertThat(entitet.getTom()).isEqualTo(TOM);
        assertThat(tilDto.tilDto(entitet)).isEqualTo(dto);
    }

    @Test
    void opphørsvariant_lagres_uten_tom_og_kommer_tilbake_som_opphørsdto() {
        var dto = new BekreftBistandOpphørOppgavetypeDataDto(FOM, BistandsvilkårIkkeOppfyltÅrsak.IKKE_14A_VEDTAK,
            "Oppfølgingsvedtaket er avsluttet.", BistandsavklaringKildeType.ANNET, "veilederen din");

        var entitet = (BekreftBistandOppgaveDataEntitet) tilEntitet.map(dto);

        assertThat(entitet.getFom()).isEqualTo(FOM);
        assertThat(entitet.getTom()).isNull();
        assertThat(tilDto.tilDto(entitet)).isEqualTo(dto);
    }

    @Test
    void kilde_annet_uten_fritekst_avvises_av_entiteten() {
        var dto = new BekreftBistandOppgavetypeDataDto(FOM, TOM, BistandsvilkårIkkeOppfyltÅrsak.IKKE_14A_VEDTAK,
            "Ingen oppfølging registrert.", BistandsavklaringKildeType.ANNET, null);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> tilEntitet.map(dto))
            .withMessageContaining("kildeFritekst");
    }
}
