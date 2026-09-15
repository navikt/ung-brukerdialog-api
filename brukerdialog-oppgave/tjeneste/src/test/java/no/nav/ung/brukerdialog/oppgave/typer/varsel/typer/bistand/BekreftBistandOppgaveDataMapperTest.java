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
    void varseltekst_settes_på_dto_for_begge_variantene() {
        var periode = (BekreftBistandOppgaveDataEntitet) tilEntitet.map(new BekreftBistandOppgavetypeDataDto(FOM, TOM,
            BistandsvilkårIkkeOppfyltÅrsak.IKKE_14A_VEDTAK, null, BistandsavklaringKildeType.BRUKER, null));
        var opphør = (BekreftBistandOppgaveDataEntitet) tilEntitet.map(new BekreftBistandOpphørOppgavetypeDataDto(FOM,
            BistandsvilkårIkkeOppfyltÅrsak.IKKE_14A_VEDTAK, null, BistandsavklaringKildeType.BRUKER, null));

        assertThat(((BekreftBistandOppgavetypeDataDto) tilDto.tilDto(periode, "Varseltekst")).varseltekst()).isEqualTo("Varseltekst");
        assertThat(((BekreftBistandOpphørOppgavetypeDataDto) tilDto.tilDto(opphør, "Varseltekst")).varseltekst()).isEqualTo("Varseltekst");
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
