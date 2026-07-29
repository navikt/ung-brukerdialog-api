package no.nav.ung.brukerdialog.oppgave;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrukerdialogOppgaveEntitetTest {

    @Test
    void løs_skal_sette_status_og_løstdato() {
        var oppgave = lagUløstOppgave();

        oppgave.løs(null);

        assertThat(oppgave.getStatus()).isEqualTo(OppgaveStatus.LØST);
        assertThat(oppgave.getLøstDato()).isNotNull();
        assertThat(oppgave.erUløst()).isFalse();
    }

    @Test
    void løs_skal_feile_når_oppgaven_allerede_er_løst() {
        var oppgave = lagUløstOppgave();
        oppgave.løs(null);
        var løstDato = oppgave.getLøstDato();

        assertThatThrownBy(() -> oppgave.løs(null))
            .isInstanceOf(UgyldigOppgaveStatusendringException.class)
            .hasMessageContaining(OppgaveStatus.LØST.name());

        assertThat(oppgave.getLøstDato()).isEqualTo(løstDato);
    }

    @Test
    void avbryt_skal_feile_når_oppgaven_er_løst() {
        var oppgave = lagUløstOppgave();
        oppgave.løs(null);

        assertThatThrownBy(oppgave::avbryt)
            .isInstanceOf(UgyldigOppgaveStatusendringException.class);

        assertThat(oppgave.getStatus()).isEqualTo(OppgaveStatus.LØST);
    }

    @Test
    void utløp_skal_feile_når_oppgaven_er_avbrutt() {
        var oppgave = lagUløstOppgave();
        oppgave.avbryt();

        assertThatThrownBy(oppgave::utløp)
            .isInstanceOf(UgyldigOppgaveStatusendringException.class);

        assertThat(oppgave.getStatus()).isEqualTo(OppgaveStatus.AVBRUTT);
    }

    @Test
    void løs_skal_feile_når_oppgaven_er_utløpt() {
        var oppgave = lagUløstOppgave();
        oppgave.utløp();

        assertThatThrownBy(() -> oppgave.løs(null))
            .isInstanceOf(UgyldigOppgaveStatusendringException.class);

        assertThat(oppgave.getLøstDato()).isNull();
    }

    @Test
    void settStatusVedMigrering_skal_omgå_validering() {
        var oppgave = lagUløstOppgave();
        oppgave.løs(null);

        oppgave.settStatusVedMigrering(OppgaveStatus.AVBRUTT);

        assertThat(oppgave.getStatus()).isEqualTo(OppgaveStatus.AVBRUTT);
    }

    private BrukerdialogOppgaveEntitet lagUløstOppgave() {
        return new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(),
            OppgaveType.SØK_YTELSE,
            new AktørId("1234567890123"),
            OppgaveYtelsetype.UNGDOMSYTELSE,
            null
        );
    }
}
