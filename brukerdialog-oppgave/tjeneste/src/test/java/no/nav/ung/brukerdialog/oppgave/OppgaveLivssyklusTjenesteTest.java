package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.inject.Instance;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.journalforing.JournalføringDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.journalforing.Fagsaksystem;
import no.nav.ung.brukerdialog.oppgave.journalforing.JournalføringKonfig;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveJournalføringEntitet;
import no.nav.ung.brukerdialog.oppgave.journalforing.OppgaveJournalføringRepository;
import no.nav.ung.brukerdialog.oppgave.journalforing.Sakstype;
import no.nav.ung.brukerdialog.oppgave.journalforing.Tema;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bosted.BekreftBostedOppgavelInnholdUtleder;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Dekker den nye «skal journalføres?»-sperren i {@link OppgaveLivssyklusTjeneste#opprettOppgave},
 * samt selve journalføringsraden som opprettes når oppgaven skal journalføres (se
 * {@code JournalføringParametre.utled(...)} og {@code JournalføringParametreTest} for
 * utledningen isolert). Merk at {@code OppgaveForSaksbehandlingTjenesteImplTest} også har en
 * karakteriseringstest for {@code opprettOppgave}, men den klassen mocker ut
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
    private OppgaveJournalføringRepository oppgaveJournalføringRepository;
    @Mock
    private JournalføringKonfig journalføringKonfig;
    @Mock
    private Instance<OppgavelInnholdUtleder> varselInnholdUtledereInstance;
    @Mock
    private Instance<OppgavelInnholdUtleder> varselInnholdUtlederValgt;
    @Mock
    private Instance<OppgaveDataMapperFraDtoTilEntitet> oppgaveDataMapperInstance;
    @Mock
    private Instance<OppgaveDataMapperFraDtoTilEntitet> oppgaveDataMapperValgt;
    @Mock
    private OppgaveDataMapperFraDtoTilEntitet oppgaveDataMapper;

    private OppgaveLivssyklusTjeneste tjeneste;

    @BeforeEach
    void setUp() {
        when(varselInnholdUtledereInstance.select(any(Annotation.class))).thenReturn(varselInnholdUtlederValgt);
        when(varselInnholdUtlederValgt.isResolvable()).thenReturn(true);
        when(varselInnholdUtlederValgt.get()).thenReturn(new BekreftBostedOppgavelInnholdUtleder());

        when(oppgaveDataMapperInstance.select(any(Annotation.class))).thenReturn(oppgaveDataMapperValgt);
        when(oppgaveDataMapperValgt.isResolvable()).thenReturn(true);
        when(oppgaveDataMapperValgt.get()).thenReturn(oppgaveDataMapper);
        when(oppgaveDataMapper.map(any())).thenReturn(mock(OppgaveDataEntitet.class));

        tjeneste = new OppgaveLivssyklusTjeneste(
            prosessTaskTjeneste,
            brukerdialogOppgaveRepository,
            oppgaveJournalføringRepository,
            journalføringKonfig,
            varselInnholdUtledereInstance,
            oppgaveDataMapperInstance
        );
    }

    @Test
    void opprettOppgave_uten_fagsakId_for_type_som_krever_fagsak_skal_ikke_opprette_journalforingsrad() {
        // Arrange – BEKREFT_BOSTED krever fagsak (er ikke i UTEN_FAGSAK), og ingen
        // journalføring-blokk sendes inn. Speiler dagens virkelighet: ung-sak sender ennå ikke
        // fagsakId.
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.BEKREFT_BOSTED, new AktørId("1234567890123"),
            OppgaveYtelsetype.UNGDOMSYTELSE, null);
        var oppgavetypeData = new BekreftBostedOppgavetypeDataDto(
            LocalDate.now(), LocalDate.now().plusMonths(1), true, null, null);

        // Act
        tjeneste.opprettOppgave(oppgave, oppgavetypeData, null);

        // Assert – oppgaven og Min Side-varselet opprettes som normalt …
        verify(brukerdialogOppgaveRepository).lagre(oppgave);
        verify(prosessTaskTjeneste).lagre(any(ProsessTaskData.class));
        // … men INGEN journalføringsrad, siden fagsakId mangler for en type som krever fagsak.
        // Dette logges som WARN, ikke 400 - og JournalføringParametre.utled(...) kalles derfor
        // aldri her.
        verifyNoInteractions(oppgaveJournalføringRepository);
        verifyNoInteractions(journalføringKonfig);
    }

    // ------------------------------------------------------------------------------------------
    // Karakteriseringstester for «lykkelig dag»-veien, som går via
    // JournalføringParametre.utled(...) (UNGDOMSYTELSE → UNG_SAK/UNG).
    // ------------------------------------------------------------------------------------------

    @Test
    void opprettOppgave_søkYtelse_uten_fagsakId_skal_opprette_rad_med_generellSak_og_begge_tasker() {
        // Arrange – SØK_YTELSE er i UTEN_FAGSAK og skal derfor journalføres selv uten fagsakId,
        // på GENERELL_SAK.
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.SØK_YTELSE, new AktørId("1234567890123"),
            OppgaveYtelsetype.UNGDOMSYTELSE, null);
        var oppgavetypeData = new BekreftBostedOppgavetypeDataDto(
            LocalDate.now(), LocalDate.now().plusMonths(1), true, null, null);
        when(journalføringKonfig.erAktivertFor(OppgaveType.SØK_YTELSE)).thenReturn(true);

        // Act
        tjeneste.opprettOppgave(oppgave, oppgavetypeData, null);

        // Assert
        ArgumentCaptor<OppgaveJournalføringEntitet> captor = ArgumentCaptor.forClass(OppgaveJournalføringEntitet.class);
        verify(oppgaveJournalføringRepository).lagre(captor.capture());
        OppgaveJournalføringEntitet journalføring = captor.getValue();
        assertThat(journalføring.getTema()).isEqualTo(Tema.UNG);
        assertThat(journalføring.getFagsaksystem()).isEqualTo(Fagsaksystem.UNG_SAK);
        assertThat(journalføring.getSakstype()).isEqualTo(Sakstype.GENERELL_SAK);
        assertThat(journalføring.getFagsakId()).isNull();
        // Både Min Side-varsel og journalføring skal opprette en task hver.
        verify(prosessTaskTjeneste, times(2)).lagre(any(ProsessTaskData.class));
    }

    @Test
    void opprettOppgave_med_fagsakId_skal_opprette_rad_med_fagsak_og_begge_tasker() {
        // Arrange – BEKREFT_BOSTED krever fagsak, og fagsakId er satt (som når ung-sak sender
        // fagsakId for oppgavetyper som krever det).
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.BEKREFT_BOSTED, new AktørId("1234567890123"),
            OppgaveYtelsetype.UNGDOMSYTELSE, null);
        var oppgavetypeData = new BekreftBostedOppgavetypeDataDto(
            LocalDate.now(), LocalDate.now().plusMonths(1), true, null, null);
        var fagsakId = new Saksnummer("ABC123");
        when(journalføringKonfig.erAktivertFor(OppgaveType.BEKREFT_BOSTED)).thenReturn(true);

        // Act
        tjeneste.opprettOppgave(oppgave, oppgavetypeData, new JournalføringDto(fagsakId));

        // Assert
        ArgumentCaptor<OppgaveJournalføringEntitet> captor = ArgumentCaptor.forClass(OppgaveJournalføringEntitet.class);
        verify(oppgaveJournalføringRepository).lagre(captor.capture());
        OppgaveJournalføringEntitet journalføring = captor.getValue();
        assertThat(journalføring.getSakstype()).isEqualTo(Sakstype.FAGSAK);
        assertThat(journalføring.getFagsakId()).isEqualTo(fagsakId);
        verify(prosessTaskTjeneste, times(2)).lagre(any(ProsessTaskData.class));
    }

    @Test
    void opprettOppgave_journalføring_deaktivert_skal_opprette_rad_men_ikke_journalførOppgaveTask() {
        // Arrange – raden opprettes uansett, kun tasken er betinget av konfigurasjonen.
        // Etterslepet blir dermed komplett og spørrbart.
        BrukerdialogOppgaveEntitet oppgave = new BrukerdialogOppgaveEntitet(
            UUID.randomUUID(), OppgaveType.SØK_YTELSE, new AktørId("1234567890123"),
            OppgaveYtelsetype.UNGDOMSYTELSE, null);
        var oppgavetypeData = new BekreftBostedOppgavetypeDataDto(
            LocalDate.now(), LocalDate.now().plusMonths(1), true, null, null);
        when(journalføringKonfig.erAktivertFor(OppgaveType.SØK_YTELSE)).thenReturn(false);

        // Act
        tjeneste.opprettOppgave(oppgave, oppgavetypeData, null);

        // Assert – raden lagres uansett …
        verify(oppgaveJournalføringRepository).lagre(any(OppgaveJournalføringEntitet.class));
        // … men kun Min Side-tasken opprettes, ikke JournalførOppgaveTask.
        verify(prosessTaskTjeneste, times(1)).lagre(any(ProsessTaskData.class));
    }
}
