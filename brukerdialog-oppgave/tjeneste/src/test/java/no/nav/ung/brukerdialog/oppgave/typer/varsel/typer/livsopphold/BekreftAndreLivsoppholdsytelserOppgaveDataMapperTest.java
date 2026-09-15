package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.livsopphold;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserAvklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Opphørsvarianten skilles fra periodevarianten utelukkende på om {@code tom} er satt – begge
 * lagres i samme tabell. Disse testene låser den rundturen fast i begge retninger.
 */
class BekreftAndreLivsoppholdsytelserOppgaveDataMapperTest {

    private static final LocalDate FOM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2025, 1, 31);

    private final BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraDtoTilEntitet tilEntitet = new BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraDtoTilEntitet();
    private final BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraEntitetTilDto tilDto = new BekreftAndreLivsoppholdsytelserOppgaveDataMapperFraEntitetTilDto();

    @Test
    void periodevariant_beholder_tom_gjennom_rundturen() {
        var dto = new BekreftAndreLivsoppholdsytelserOppgavetypeDataDto(FOM, TOM, AndreLivsoppholdsytelserIkkeOppfyltÅrsak.MOTTAR_DAGPENGER,
            null, AndreLivsoppholdsytelserAvklaringKildeType.NAV, null);

        var entitet = (BekreftAndreLivsoppholdsytelserOppgaveDataEntitet) tilEntitet.map(dto);

        assertThat(entitet.getFom()).isEqualTo(FOM);
        assertThat(entitet.getTom()).isEqualTo(TOM);
        assertThat(tilDto.tilDto(entitet)).isEqualTo(dto);
    }

    @Test
    void opphørsvariant_lagres_uten_tom_og_kommer_tilbake_som_opphørsdto() {
        var dto = new BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto(FOM, AndreLivsoppholdsytelserIkkeOppfyltÅrsak.MOTTAR_ANNEN_YTELSE,
            "Du mottar stønad til livsopphold fra en annen offentlig ordning.", AndreLivsoppholdsytelserAvklaringKildeType.ANNET, "kommunen");

        var entitet = (BekreftAndreLivsoppholdsytelserOppgaveDataEntitet) tilEntitet.map(dto);

        assertThat(entitet.getFom()).isEqualTo(FOM);
        assertThat(entitet.getTom()).isNull();
        assertThat(tilDto.tilDto(entitet)).isEqualTo(dto);
    }

    @Test
    void varseltekst_settes_på_dto_for_begge_variantene() {
        var periode = (BekreftAndreLivsoppholdsytelserOppgaveDataEntitet) tilEntitet.map(new BekreftAndreLivsoppholdsytelserOppgavetypeDataDto(FOM, TOM,
            AndreLivsoppholdsytelserIkkeOppfyltÅrsak.MOTTAR_DAGPENGER, null, AndreLivsoppholdsytelserAvklaringKildeType.NAV, null));
        var opphør = (BekreftAndreLivsoppholdsytelserOppgaveDataEntitet) tilEntitet.map(new BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto(FOM,
            AndreLivsoppholdsytelserIkkeOppfyltÅrsak.MOTTAR_DAGPENGER, null, AndreLivsoppholdsytelserAvklaringKildeType.NAV, null));

        assertThat(((BekreftAndreLivsoppholdsytelserOppgavetypeDataDto) tilDto.tilDto(periode, "Varseltekst")).varseltekst()).isEqualTo("Varseltekst");
        assertThat(((BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto) tilDto.tilDto(opphør, "Varseltekst")).varseltekst()).isEqualTo("Varseltekst");
    }

    @Test
    void kilde_annet_uten_fritekst_avvises_av_entiteten() {
        var dto = new BekreftAndreLivsoppholdsytelserOppgavetypeDataDto(FOM, TOM, AndreLivsoppholdsytelserIkkeOppfyltÅrsak.MOTTAR_UFØRETRYGD,
            null, AndreLivsoppholdsytelserAvklaringKildeType.ANNET, null);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> tilEntitet.map(dto))
            .withMessageContaining("kildeFritekst");
    }
}
