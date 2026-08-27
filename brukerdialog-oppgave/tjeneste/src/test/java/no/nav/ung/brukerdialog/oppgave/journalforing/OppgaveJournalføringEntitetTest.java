package no.nav.ung.brukerdialog.oppgave.journalforing;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.JournalpostId;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OppgaveJournalføringEntitetTest {

    private final BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
        UUID.randomUUID(),
        OppgaveType.SØK_YTELSE,
        new AktørId("1234567890123"),
        OppgaveYtelsetype.UNGDOMSYTELSE,
        null
    );

    @Test
    void skal_opprette_med_fagsak_når_sakstype_er_fagsak() {
        var journalføring = new OppgaveJournalføringEntitet(
            oppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.FAGSAK, new Saksnummer("ABC123"),
            new JournalpostId("123456789"));

        assertThat(journalføring.getSakstype()).isEqualTo(Sakstype.FAGSAK);
        assertThat(journalføring.getFagsakId()).isEqualTo(new Saksnummer("ABC123"));
        assertThat(journalføring.getJournalpostId()).isEqualTo(new JournalpostId("123456789"));
        assertThat(journalføring.getJournalførtTid()).isNotNull();
    }

    @Test
    void skal_opprette_uten_fagsak_når_sakstype_er_generell_sak() {
        var journalføring = new OppgaveJournalføringEntitet(
            oppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.GENERELL_SAK, null,
            new JournalpostId("123456789"));

        assertThat(journalføring.getFagsakId()).isNull();
        assertThat(journalføring.getSakstype()).isEqualTo(Sakstype.GENERELL_SAK);
    }

    @Test
    void skal_feile_når_fagsak_id_mangler_for_sakstype_fagsak() {
        assertThatThrownBy(() -> new OppgaveJournalføringEntitet(
            oppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.FAGSAK, null,
            new JournalpostId("123456789")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("fagsakId");
    }

    @Test
    void skal_feile_når_fagsak_id_er_satt_for_sakstype_generell_sak() {
        assertThatThrownBy(() -> new OppgaveJournalføringEntitet(
            oppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.GENERELL_SAK, new Saksnummer("ABC123"),
            new JournalpostId("123456789")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("fagsakId");
    }

    @Test
    void skal_feile_når_journalpostId_mangler() {
        assertThatThrownBy(() -> new OppgaveJournalføringEntitet(
            oppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.GENERELL_SAK, null, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("journalpostId");
    }
}
