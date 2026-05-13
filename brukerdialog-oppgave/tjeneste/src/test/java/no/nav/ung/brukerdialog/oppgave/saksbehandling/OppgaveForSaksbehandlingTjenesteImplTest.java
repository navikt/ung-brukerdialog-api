package no.nav.ung.brukerdialog.oppgave.saksbehandling;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.OppgaveLivssyklusTjeneste;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OppgaveForSaksbehandlingTjenesteImplTest {

    @Mock
    private BrukerdialogOppgaveRepository repository;

    @Mock
    private OppgaveLivssyklusTjeneste oppgaveLivssyklusTjeneste;

    @InjectMocks
    private OppgaveForSaksbehandlingTjenesteImpl tjeneste;

    private AktørId aktørId;

    @BeforeEach
    void setUp() {
        aktørId = new AktørId("1234567890123");
    }

    @Test
    void avbrytSøkYtelseOppgave_skal_avbryte_uløst_søkytelse_oppgave() {
        // Arrange
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(),
            OppgaveType.SØK_YTELSE,
            aktørId,
            OppgaveYtelsetype.UNGDOMSYTELSE,
            null
        );
        when(repository.hentOppgaveForType(OppgaveType.SØK_YTELSE, OppgaveStatus.ULØST, aktørId))
            .thenReturn(List.of(oppgave));

        // Act
        tjeneste.avbrytSøkYtelseOppgave(aktørId);

        // Assert
        verify(oppgaveLivssyklusTjeneste).avbrytOppgave(oppgave);
    }

    @Test
    void avbrytSøkYtelseOppgave_skal_ikke_feile_når_ingen_oppgave_finnes() {
        // Arrange
        when(repository.hentOppgaveForType(OppgaveType.SØK_YTELSE, OppgaveStatus.ULØST, aktørId))
            .thenReturn(List.of());

        // Act
        tjeneste.avbrytSøkYtelseOppgave(aktørId);

        // Assert
        verifyNoInteractions(oppgaveLivssyklusTjeneste);
    }

    @Test
    void avbrytSøkYtelseOppgave_skal_avbryte_alle_dersom_flere_uløste_oppgaver_finnes() {
        // Arrange
        BrukerdialogOppgaveEntitet oppgave1 = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.SØK_YTELSE, aktørId, OppgaveYtelsetype.UNGDOMSYTELSE, null);
        BrukerdialogOppgaveEntitet oppgave2 = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.SØK_YTELSE, aktørId, OppgaveYtelsetype.UNGDOMSYTELSE, null);
        when(repository.hentOppgaveForType(OppgaveType.SØK_YTELSE, OppgaveStatus.ULØST, aktørId))
            .thenReturn(List.of(oppgave1, oppgave2));

        // Act
        tjeneste.avbrytSøkYtelseOppgave(aktørId);

        // Assert
        verify(oppgaveLivssyklusTjeneste).avbrytOppgave(oppgave1);
        verify(oppgaveLivssyklusTjeneste).avbrytOppgave(oppgave2);
    }

    @Test
    void løsSøkYtelseOppgave_skal_løse_uløst_søkytelse_oppgave() {
        // Arrange
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(),
            OppgaveType.SØK_YTELSE,
            aktørId,
            OppgaveYtelsetype.UNGDOMSYTELSE,
            null
        );
        when(repository.hentOppgaveForType(OppgaveType.SØK_YTELSE, OppgaveStatus.ULØST, aktørId))
            .thenReturn(List.of(oppgave));

        // Act
        tjeneste.løsSøkYtelseOppgave(aktørId);

        // Assert
        verify(oppgaveLivssyklusTjeneste).løsOppgave(eq(oppgave), any());
    }
}

