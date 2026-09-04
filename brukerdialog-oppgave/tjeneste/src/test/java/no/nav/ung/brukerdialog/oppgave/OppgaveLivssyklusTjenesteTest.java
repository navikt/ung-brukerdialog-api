package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.inject.Instance;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.journalforing.JournalføringDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.journalforing.JournalførOppgaveTask;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Dekker den nye «skal journalføres?»-sperren i {@link OppgaveLivssyklusTjeneste#opprettOppgave},
 * som avgjør om {@link JournalførOppgaveTask} opprettes i det hele tatt. Selve journalførings-
 * raden opprettes ikke lenger her - det gjør tasken selv, først etter en vellykket journalføring
 * (se {@code JournalførOppgaveTaskTest}). Merk at {@code OppgaveForSaksbehandlingTjenesteImplTest}
 * også har en karakteriseringstest for {@code opprettOppgave}, men den klassen mocker ut
 * {@code OppgaveLivssyklusTjeneste} i sin helhet - denne klassen, bygget nettopp for å teste
 * {@code opprettOppgave} med full kontroll på avhengighetene, er det riktige stedet i praksis.
 */
@ExtendWith(MockitoExtension.class)
class OppgaveLivssyklusTjenesteTest {

    @Mock
    private ProsessTaskTjeneste prosessTaskTjeneste;
    @Mock
    private BrukerdialogOppgaveRepository brukerdialogOppgaveRepository;
    @Mock
    private Instance<OppgaveInnholdUtleder> innholdUtledereInstance;
    @Mock
    private Instance<OppgaveInnholdUtleder> innholdUtlederValgt;
    @Mock
    private OppgaveInnholdUtleder innholdUtleder;
    @Mock
    private Instance<OppgaveDataMapperFraDtoTilEntitet> oppgaveDataMapperInstance;
    @Mock
    private Instance<OppgaveDataMapperFraDtoTilEntitet> oppgaveDataMapperValgt;
    @Mock
    private OppgaveDataMapperFraDtoTilEntitet oppgaveDataMapper;

    private OppgaveLivssyklusTjeneste tjeneste;

    @BeforeEach
    void setUp() {
        when(innholdUtledereInstance.select(any(Annotation.class))).thenReturn(innholdUtlederValgt);
        when(innholdUtlederValgt.isResolvable()).thenReturn(true);
        when(innholdUtlederValgt.get()).thenReturn(innholdUtleder);
        when(innholdUtleder.tekster(any())).thenReturn(List.of(new OppgaveAvsnitt("Varseltekst")));
        when(innholdUtleder.varselLenke(any())).thenReturn("https://nav.no/minside");

        when(oppgaveDataMapperInstance.select(any(Annotation.class))).thenReturn(oppgaveDataMapperValgt);
        when(oppgaveDataMapperValgt.isResolvable()).thenReturn(true);
        when(oppgaveDataMapperValgt.get()).thenReturn(oppgaveDataMapper);
        when(oppgaveDataMapper.map(any())).thenReturn(mock(OppgaveDataEntitet.class));

        tjeneste = new OppgaveLivssyklusTjeneste(
            prosessTaskTjeneste,
            brukerdialogOppgaveRepository,
            innholdUtledereInstance,
            oppgaveDataMapperInstance
        );
    }

    private ProsessTaskData journalføringTask(List<ProsessTaskData> alle) {
        return alle.stream()
            .filter(data -> JournalførOppgaveTask.TASKTYPE.equals(data.getTaskType()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Fant ingen JournalførOppgaveTask blant lagrede tasker"));
    }

    @Test
    void opprettOppgave_uten_saksnummer_for_type_som_krever_fagsak_skal_ikke_opprette_journalførOppgaveTask() {
        // Arrange – BEKREFT_BOSTED krever fagsak (er ikke i UTEN_FAGSAK), og ingen
        // journalføring-blokk sendes inn. Speiler dagens virkelighet: ung-sak sender ennå ikke
        // saksnummer.
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.BEKREFT_BOSTED, new AktørId("1234567890123"),
            OppgaveYtelsetype.UNGDOMSYTELSE, null);
        var oppgavetypeData = new BekreftBostedOppgavetypeDataDto(
            LocalDate.now(), LocalDate.now().plusMonths(1), true, null, null, BostedsavklaringKildeType.BRUKER, null);

        // Act
        tjeneste.opprettOppgave(oppgave, oppgavetypeData, null);

        // Assert – oppgaven og Min Side-varselet opprettes som normalt …
        verify(brukerdialogOppgaveRepository).lagre(oppgave);
        // … men KUN Min Side-tasken - ingen JournalførOppgaveTask, siden saksnummer mangler for
        // en type som krever fagsak. Dette logges som WARN, ikke 400.
        verify(prosessTaskTjeneste, times(1)).lagre(any(ProsessTaskData.class));
    }

    // ------------------------------------------------------------------------------------------
    // Karakteriseringstester for «lykkelig dag»-veien: JournalførOppgaveTask opprettes med
    // riktige properties.
    // ------------------------------------------------------------------------------------------

    @Test
    void opprettOppgave_søkYtelse_uten_saksnummer_skal_opprette_journalførOppgaveTask_uten_saksnummer() {
        // Arrange – SØK_YTELSE er i UTEN_FAGSAK og skal derfor journalføres selv uten saksnummer.
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.SØK_YTELSE, new AktørId("1234567890123"),
            OppgaveYtelsetype.UNGDOMSYTELSE, null);
        var oppgavetypeData = new BekreftBostedOppgavetypeDataDto(
            LocalDate.now(), LocalDate.now().plusMonths(1), true, null, null, BostedsavklaringKildeType.BRUKER, null);

        // Act
        tjeneste.opprettOppgave(oppgave, oppgavetypeData, null);

        // Assert – både Min Side-varsel og JournalførOppgaveTask opprettes.
        ArgumentCaptor<ProsessTaskData> captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(prosessTaskTjeneste, times(2)).lagre(captor.capture());
        ProsessTaskData journalføringTask = journalføringTask(captor.getAllValues());
        assertThat(journalføringTask.getPropertyValue(JournalførOppgaveTask.OPPGAVE_REFERANSE))
            .isEqualTo(oppgave.getOppgavereferanse().toString());
        assertThat(journalføringTask.getPropertyValue(JournalførOppgaveTask.SAKSNUMMER)).isNull();
    }

    @Test
    void opprettOppgave_med_saksnummer_skal_opprette_journalførOppgaveTask_med_saksnummer() {
        // Arrange – BEKREFT_BOSTED krever fagsak, og saksnummer er satt (som når ung-sak sender
        // saksnummer for oppgavetyper som krever det).
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.BEKREFT_BOSTED, new AktørId("1234567890123"),
            OppgaveYtelsetype.UNGDOMSYTELSE, null);
        var oppgavetypeData = new BekreftBostedOppgavetypeDataDto(
            LocalDate.now(), LocalDate.now().plusMonths(1), true, null, null, BostedsavklaringKildeType.BRUKER, null);
        var saksnummer = new Saksnummer("ABC123");

        // Act
        tjeneste.opprettOppgave(oppgave, oppgavetypeData, new JournalføringDto(saksnummer));

        // Assert
        ArgumentCaptor<ProsessTaskData> captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(prosessTaskTjeneste, times(2)).lagre(captor.capture());
        ProsessTaskData journalføringTask = journalføringTask(captor.getAllValues());
        assertThat(journalføringTask.getPropertyValue(JournalførOppgaveTask.SAKSNUMMER)).isEqualTo("ABC123");
    }
}
