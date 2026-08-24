package no.nav.ung.brukerdialog.oppgave.journalforing;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JournalføringKonfigTest {

    @ParameterizedTest
    @EnumSource(OppgaveType.class)
    void default_uten_miljøvariabel_er_deaktivert_for_alle_oppgavetyper(OppgaveType oppgaveType) {
        // Simulerer manglende/feilstavet JOURNALFORING_ENABLED i manifestet.
        var konfig = new JournalføringKonfig(false, "");

        assertThat(konfig.erAktivertFor(oppgaveType)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(OppgaveType.class)
    void aktivert_uten_deny_liste_gjelder_alle_oppgavetyper(OppgaveType oppgaveType) {
        var konfig = new JournalføringKonfig(true, "");

        assertThat(konfig.erAktivertFor(oppgaveType)).isTrue();
    }

    @Test
    void global_deaktivering_overstyrer_selv_om_typen_ikke_er_i_deny_listen() {
        var konfig = new JournalføringKonfig(false, "");

        assertThat(konfig.erAktivertFor(OppgaveType.BEKREFT_BOSTED)).isFalse();
    }

    @Test
    void deny_liste_deaktiverer_kun_angitte_typer() {
        var konfig = new JournalføringKonfig(true, "BEKREFT_BOSTED, RAPPORTER_INNTEKT");

        assertThat(konfig.erAktivertFor(OppgaveType.BEKREFT_BOSTED)).isFalse();
        assertThat(konfig.erAktivertFor(OppgaveType.RAPPORTER_INNTEKT)).isFalse();
        assertThat(konfig.erAktivertFor(OppgaveType.SØK_YTELSE)).isTrue();
    }

    @Test
    void deny_liste_med_kun_blanke_tegn_behandles_som_tom() {
        var konfig = new JournalføringKonfig(true, "   ");

        assertThat(konfig.erAktivertFor(OppgaveType.SØK_YTELSE)).isTrue();
    }

    @Test
    void ukjent_oppgavetype_i_deny_listen_feiler_ved_oppstart_med_forklarende_melding() {
        assertThatThrownBy(() -> new JournalføringKonfig(true, "IKKE_EN_GYLDIG_TYPE"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("IKKE_EN_GYLDIG_TYPE")
            .hasMessageContaining("JOURNALFORING_DEAKTIVERTE_OPPGAVETYPER");
    }

    @Test
    void enkelt_ugyldig_verdi_blant_gyldige_feiler_også() {
        assertThatThrownBy(() -> new JournalføringKonfig(true, "BEKREFT_BOSTED,TYPO_HER"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TYPO_HER");
    }
}
