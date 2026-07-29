package no.nav.ung.brukerdialog.oppgave.brukerdialog;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveMapper;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.OppgaveLivssyklusTjeneste;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrukerdialogOppgaveTjenesteImplTest {

    @Mock
    private BrukerdialogOppgaveRepository repository;

    @Mock
    private OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste;

    @Mock
    private BrukerdialogOppgaveMapper mapper;

    @InjectMocks
    private BrukerdialogOppgaveTjenesteImpl tjeneste;

    private AktørId aktørId;
    private UUID oppgavereferanse;

    @BeforeEach
    void setUp() {
        aktørId = new AktørId("1234567890123");
        oppgavereferanse = UUID.randomUUID();
    }

    @Test
    void skal_løse_uløst_oppgave() {
        var oppgave = lagOppgave();
        when(repository.hentOppgaveForOppgavereferanse(oppgavereferanse, aktørId)).thenReturn(Optional.of(oppgave));
        when(oppgaveLivssyklusTjeneste.løsOppgave(eq(oppgave), any())).thenReturn(oppgave);

        tjeneste.løsOppgave(oppgavereferanse, aktørId, Optional.empty());

        verify(oppgaveLivssyklusTjeneste).løsOppgave(eq(oppgave), any());
    }

    @Test
    void skal_feile_og_ikke_løse_på_nytt_når_oppgaven_allerede_er_løst() {
        var oppgave = lagOppgave();
        oppgave.setStatus(OppgaveStatus.LØST);
        when(repository.hentOppgaveForOppgavereferanse(oppgavereferanse, aktørId)).thenReturn(Optional.of(oppgave));

        assertThatThrownBy(() -> tjeneste.løsOppgave(oppgavereferanse, aktørId, Optional.empty()))
            .isInstanceOf(OppgaveKanIkkeLøsesException.class)
            .hasMessageContaining(OppgaveStatus.LØST.name());

        verify(oppgaveLivssyklusTjeneste, never()).løsOppgave(any(), any());
    }

    @Test
    void skal_feile_når_oppgaven_er_avbrutt() {
        var oppgave = lagOppgave();
        oppgave.setStatus(OppgaveStatus.AVBRUTT);
        when(repository.hentOppgaveForOppgavereferanse(oppgavereferanse, aktørId)).thenReturn(Optional.of(oppgave));

        assertThatThrownBy(() -> tjeneste.løsOppgave(oppgavereferanse, aktørId, Optional.empty()))
            .isInstanceOf(OppgaveKanIkkeLøsesException.class);

        verify(oppgaveLivssyklusTjeneste, never()).løsOppgave(any(), any());
    }

    private BrukerdialogOppgaveEntitet lagOppgave() {
        return new BrukerdialogOppgaveEntitet(
            oppgavereferanse,
            OppgaveType.SØK_YTELSE,
            aktørId,
            OppgaveYtelsetype.UNGDOMSYTELSE,
            null
        );
    }
}
