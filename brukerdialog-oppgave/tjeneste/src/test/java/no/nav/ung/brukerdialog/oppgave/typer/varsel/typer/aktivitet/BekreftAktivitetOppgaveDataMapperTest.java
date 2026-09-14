package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.aktivitet;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.AktivitetsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.AktivitetsvilkåretIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.BekreftAktivitetOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.BekreftAktivitetOpphørOppgavetypeDataDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Opphørsvarianten skilles fra periodevarianten utelukkende på om {@code tom} er satt – begge
 * lagres i samme tabell. Disse testene låser den rundturen fast i begge retninger.
 */
class BekreftAktivitetOppgaveDataMapperTest {

    private static final LocalDate FOM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2025, 1, 31);

    private final BekreftAktivitetOppgaveDataMapperFraDtoTilEntitet tilEntitet = new BekreftAktivitetOppgaveDataMapperFraDtoTilEntitet();
    private final BekreftAktivitetOppgaveDataMapperFraEntitetTilDto tilDto = new BekreftAktivitetOppgaveDataMapperFraEntitetTilDto();

    @Test
    void periodevariant_beholder_tom_gjennom_rundturen() {
        var dto = new BekreftAktivitetOppgavetypeDataDto(FOM, TOM, AktivitetsvilkåretIkkeOppfyltÅrsak.ANNET,
            "Du har ikke møtt til den avtalte aktiviteten.", AktivitetsavklaringKildeType.NAV, null);

        var entitet = (BekreftAktivitetOppgaveDataEntitet) tilEntitet.map(dto);

        assertThat(entitet.getFom()).isEqualTo(FOM);
        assertThat(entitet.getTom()).isEqualTo(TOM);
        assertThat(tilDto.tilDto(entitet)).isEqualTo(dto);
    }

    @Test
    void opphørsvariant_lagres_uten_tom_og_kommer_tilbake_som_opphørsdto() {
        var dto = new BekreftAktivitetOpphørOppgavetypeDataDto(FOM, AktivitetsvilkåretIkkeOppfyltÅrsak.ANNET,
            "Aktiviteten ble avsluttet.", AktivitetsavklaringKildeType.ANNET, "veilederen din");

        var entitet = (BekreftAktivitetOppgaveDataEntitet) tilEntitet.map(dto);

        assertThat(entitet.getFom()).isEqualTo(FOM);
        assertThat(entitet.getTom()).isNull();
        assertThat(tilDto.tilDto(entitet)).isEqualTo(dto);
    }

    @Test
    void kilde_annet_uten_fritekst_avvises_av_entiteten() {
        var dto = new BekreftAktivitetOppgavetypeDataDto(FOM, TOM, AktivitetsvilkåretIkkeOppfyltÅrsak.ANNET,
            "Du har ikke møtt til den avtalte aktiviteten.", AktivitetsavklaringKildeType.ANNET, null);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> tilEntitet.map(dto))
            .withMessageContaining("kildeFritekst");
    }
}
