package no.nav.ung.brukerdialog.oppgave.saksbehandling;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.EndreOppgaveStatusDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.oppgave.OppgaveLivssyklusTjeneste;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.InntektsrapporteringOppgaveDataEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    void avbrytSøkYtelseOppgave_skal_avbryte_uløst_søkytelse_oppgaver() {
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
        tjeneste.avbrytSøkYtelseOppgaver(aktørId);

        // Assert
        verify(oppgaveLivssyklusTjeneste).avbrytOppgave(oppgave);
    }

    @Test
    void avbrytSøkYtelseOppgave_skal_ikke_feile_når_ingen_oppgaver_finnes() {
        // Arrange
        when(repository.hentOppgaveForType(OppgaveType.SØK_YTELSE, OppgaveStatus.ULØST, aktørId))
            .thenReturn(List.of());

        // Act
        tjeneste.avbrytSøkYtelseOppgaver(aktørId);

        // Assert
        verifyNoInteractions(oppgaveLivssyklusTjeneste);
    }

    @Test
    void avbrytSøkYtelseOppgaver_skal_avbryte_alle_dersom_flere_uløste_oppgaver_finnes() {
        // Arrange
        BrukerdialogOppgaveEntitet oppgave1 = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.SØK_YTELSE, aktørId, OppgaveYtelsetype.UNGDOMSYTELSE, null);
        BrukerdialogOppgaveEntitet oppgave2 = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.SØK_YTELSE, aktørId, OppgaveYtelsetype.UNGDOMSYTELSE, null);
        when(repository.hentOppgaveForType(OppgaveType.SØK_YTELSE, OppgaveStatus.ULØST, aktørId))
            .thenReturn(List.of(oppgave1, oppgave2));

        // Act
        tjeneste.avbrytSøkYtelseOppgaver(aktørId);

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

    @Test
    void settOppgaveTilAvbrutt_søkYtelse_uten_datoer_skal_avbryte_oppgaven() {
        // Arrange – SøkYtelse-oppgave uten periode
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(),
            OppgaveType.SØK_YTELSE,
            aktørId,
            OppgaveYtelsetype.UNGDOMSYTELSE,
            null
        );
        when(repository.hentAlleOppgaverForAktør(aktørId)).thenReturn(List.of(oppgave));

        EndreOppgaveStatusDto dto = new EndreOppgaveStatusDto(aktørId, OppgaveType.SØK_YTELSE, null, null);

        // Act
        tjeneste.settOppgaveTilAvbrutt(dto);

        // Assert
        verify(oppgaveLivssyklusTjeneste).avbrytOppgave(oppgave);
    }

    @Test
    void settOppgaveTilAvbrutt_inntektsrapportering_med_riktig_periode_skal_avbryte_oppgaven() {
        // Arrange
        var fom = LocalDate.of(2025, 1, 1);
        var tom = LocalDate.of(2025, 1, 31);
        var oppgaveData = new InntektsrapporteringOppgaveDataEntitet(fom, tom, false);
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(),
            OppgaveType.RAPPORTER_INNTEKT,
            aktørId,
            OppgaveYtelsetype.UNGDOMSYTELSE,
            null
        );
        oppgave.setOppgaveData(oppgaveData);
        when(repository.hentAlleOppgaverForAktør(aktørId)).thenReturn(List.of(oppgave));

        EndreOppgaveStatusDto dto = new EndreOppgaveStatusDto(aktørId, OppgaveType.RAPPORTER_INNTEKT, fom, tom);

        // Act
        tjeneste.settOppgaveTilAvbrutt(dto);

        // Assert
        verify(oppgaveLivssyklusTjeneste).avbrytOppgave(oppgave);
    }

    @Test
    void settOppgaveTilAvbrutt_inntektsrapportering_med_feil_periode_skal_ikke_avbryte_oppgaven() {
        // Arrange
        var fom = LocalDate.of(2025, 1, 1);
        var tom = LocalDate.of(2025, 1, 31);
        var annenFom = LocalDate.of(2025, 2, 1);
        var annenTom = LocalDate.of(2025, 2, 28);
        var oppgaveData = new InntektsrapporteringOppgaveDataEntitet(fom, tom, false);
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(),
            OppgaveType.RAPPORTER_INNTEKT,
            aktørId,
            OppgaveYtelsetype.UNGDOMSYTELSE,
            null
        );
        oppgave.setOppgaveData(oppgaveData);
        when(repository.hentAlleOppgaverForAktør(aktørId)).thenReturn(List.of(oppgave));

        EndreOppgaveStatusDto dto = new EndreOppgaveStatusDto(aktørId, OppgaveType.RAPPORTER_INNTEKT, annenFom, annenTom);

        // Act
        tjeneste.settOppgaveTilAvbrutt(dto);

        // Assert
        verifyNoInteractions(oppgaveLivssyklusTjeneste);
    }
}
