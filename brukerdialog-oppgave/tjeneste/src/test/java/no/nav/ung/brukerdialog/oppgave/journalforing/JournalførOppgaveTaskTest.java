package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.enterprise.inject.Instance;
import no.nav.k9.felles.integrasjon.pdl.Navn;
import no.nav.k9.felles.integrasjon.pdl.PdlKlient;
import no.nav.k9.felles.integrasjon.pdl.PdlException;
import no.nav.k9.felles.integrasjon.pdl.Person;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.felles.exception.HttpStatuskodeException;
import no.nav.k9.felles.integrasjon.dokarkiv.DokarkivKlient;
import no.nav.ung.brukerdialog.pdf.PdfGenerator;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.JournalpostId;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Dekker {@link JournalførOppgaveTask} fullt ut, inkludert veiene gjennom {@code hentPersonInfo}.
 * <p>
 * OK/409/5xx-testene bruker {@link DokArkivKlientFake} for å styre dokarkiv-svaret uten en ekte
 * HTTP-server (409 markeres journalført med samme journalpostId, som en vanlig 201). PDL-hullene -
 * person uten folkeregisterident, og PdlException ved navneoppslag - er dekket separat.
 * <p>
 */
@ExtendWith(MockitoExtension.class)
class JournalførOppgaveTaskTest {

    @Mock
    private OppgaveJournalføringRepository journalføringRepository;
    @Mock
    private BrukerdialogOppgaveRepository oppgaveRepository;
    @Mock
    private Instance<OppgaveDokumentUtleder> dokumentUtledere;
    @Mock
    private Instance<OppgaveDokumentUtleder> dokumentUtlederValgt;
    @Mock
    private OppgaveDokumentUtleder dokumentUtleder;
    @Mock
    private PdlKlient pdl;
    @Mock
    private PdfGenerator pdfGenerator;
    @Mock
    private DokarkivKlient dokArkivKlient;

    private JournalførOppgaveTask task;
    private UUID oppgavereferanse;

    @BeforeEach
    void setUp() {
        task = new JournalførOppgaveTask(journalføringRepository, oppgaveRepository, true,
            dokumentUtledere, pdl, pdfGenerator, dokArkivKlient);
        oppgavereferanse = UUID.randomUUID();
    }

    private ProsessTaskData taskData() {
        ProsessTaskData data = ProsessTaskData.forProsessTask(JournalførOppgaveTask.class);
        data.setProperty(JournalførOppgaveTask.OPPGAVE_REFERANSE, oppgavereferanse.toString());
        return data;
    }

    /**
     * {@code opprettetTidspunkt} settes normalt av {@code BaseEntitet#onCreate}
     * (JPA {@code @PrePersist}) - denne fixturen konstruerer entiteten direkte uten å gå via
     * JPA, så feltet må settes eksplisitt via migreringskonstruktøren for at
     * {@code JournalførOppgaveTask#doTask} (som leser {@code getOpprettetTidspunkt()} for
     * PDF-brevhodet) ikke skal få en NPE.
     */
    private BrukerdialogOppgaveEntitet oppgave(OppgaveType oppgaveType) {
        return new BrukerdialogOppgaveEntitet(oppgavereferanse, oppgaveType, new AktørId("1234567890123"),
            null, OppgaveStatus.ULØST, OppgaveYtelsetype.UNGDOMSYTELSE, null, null,
            LocalDateTime.of(2025, 1, 15, 10, 0), "VL");
    }

    /**
     * Stubber PDL-navneoppslaget ({@code hentPersonInfo}) og
     * {@code dokumentUtledere}-lookupen ({@link OppgaveDokumentUtleder#finnUtleder}) som alle tre
     * testene mot {@link DokArkivKlientFake} (ok/409/5xx) trenger for å nå fram til selve
     * dokarkiv-kallet, uten å NPE på Mockitos default-svar (null) for {@code Person}/
     * {@code Instance}. Se {@code OppgaveLivssyklusTjenesteTest} for samme
     * select/isResolvable/get-mønster på {@code Instance<T>}.
     */
    private void arrangerPersonOgDokumentutleder(BrukerdialogOppgaveEntitet oppgave, String tittel) {
        Navn navn = new Navn();
        navn.setFornavn("Kari");
        navn.setEtternavn("Nordmann");
        Person person = new Person();
        person.setNavn(List.of(navn));
        when(pdl.hentPerson(any(), any(), anyList())).thenReturn(person);

        when(dokumentUtledere.select(any(Annotation.class))).thenReturn(dokumentUtlederValgt);
        when(dokumentUtlederValgt.isResolvable()).thenReturn(true);
        when(dokumentUtlederValgt.get()).thenReturn(dokumentUtleder);
        when(dokumentUtleder.utledTittel(oppgave)).thenReturn(tittel);
        when(dokumentUtleder.malnavn()).thenReturn("typer/test-mal");
        when(dokumentUtleder.utledInnholdsdata(oppgave)).thenReturn(Map.of());
        when(pdfGenerator.genererPdf(any())).thenReturn(new byte[]{1, 2, 3});
    }

    /**
     * Arrangerer de vanlige oppslagene (rad finnes ikke, oppgave finnes, journalføring aktivert)
     * som alle «kommer forbi sperrene»-testene trenger før de når fram til selve forsøket.
     */
    private BrukerdialogOppgaveEntitet arrangerOppgaveKlarForJournalføring(OppgaveType oppgaveType) {
        BrukerdialogOppgaveEntitet oppgave = oppgave(oppgaveType);
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.empty());
        when(oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)).thenReturn(Optional.of(oppgave));
        when(pdl.hentPersonIdentForAktørId(oppgave.getAktørId().getId())).thenReturn(Optional.of("12345678901"));
        return oppgave;
    }

    @Test
    void requiredProperties_skal_kun_kreve_oppgaveReferanse() {
        assertThat(task.requiredProperties()).containsExactly(JournalførOppgaveTask.OPPGAVE_REFERANSE);
    }

    @Test
    void doTask_allerede_journalfort_skal_hoppe_over_idempotent() {
        // Arrange – raden finnes allerede fra et tidligere, vellykket forsøk.
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.SØK_YTELSE);
        OppgaveJournalføringEntitet eksisterende = new OppgaveJournalføringEntitet(
            oppgave, Tema.UNG, Fagsaksystem.UNG_SAK, Sakstype.GENERELL_SAK, null,
            new JournalpostId("123456789"));
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.of(eksisterende));

        // Act
        task.doTask(taskData());

        // Assert – ingen videre behandling
        verifyNoInteractions(oppgaveRepository, pdl, pdfGenerator, dokArkivKlient, dokumentUtledere);
        verify(journalføringRepository, never()).lagre(any());
    }

    @Test
    void doTask_oppgave_ikke_funnet_skal_kaste_illegalStateException() {
        // Arrange – ingen rad finnes, og oppgaven den skulle vise til gjør (uventet) heller ikke det
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.empty());
        when(oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)).thenReturn(Optional.empty());

        // Act & assert
        assertThatThrownBy(() -> task.doTask(taskData()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(oppgavereferanse.toString());
        verifyNoInteractions(pdl, pdfGenerator, dokArkivKlient, dokumentUtledere);
    }

    @Test
    void doTask_journalføring_deaktivert_globalt_skal_hoppe_over_uten_å_lagre_rad() {
        // Arrange – journalføring er slått av globalt (JOURNALFORING_ENABLED=false)
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.SØK_YTELSE);
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.empty());
        when(oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)).thenReturn(Optional.of(oppgave));
        JournalførOppgaveTask taskDeaktivert = new JournalførOppgaveTask(journalføringRepository, oppgaveRepository,
            false, dokumentUtledere, pdl, pdfGenerator, dokArkivKlient);

        // Act
        taskDeaktivert.doTask(taskData());

        // Assert – verken forsøk på journalføring eller lagring
        verifyNoInteractions(pdl, pdfGenerator, dokArkivKlient, dokumentUtledere);
        verify(journalføringRepository, never()).lagre(any());
    }

    // ------------------------------------------------------------------------------------------
    // Veiene som går via hentPersonInfo.
    // ------------------------------------------------------------------------------------------

    @Test
    void doTask_ok_skal_lagre_journalføringsrad_med_journalpostId_fra_dokarkiv() {
        // Arrange
        BrukerdialogOppgaveEntitet oppgave = arrangerOppgaveKlarForJournalføring(OppgaveType.SØK_YTELSE);
        arrangerPersonOgDokumentutleder(oppgave, "Søk ytelse");

        DokArkivKlientFake dokArkivKlientFake = new DokArkivKlientFake();
        dokArkivKlientFake.svarMedOk("123456789");
        JournalførOppgaveTask taskMedFake = new JournalførOppgaveTask(journalføringRepository, oppgaveRepository,
            true, dokumentUtledere, pdl, pdfGenerator, dokArkivKlientFake);

        // Act
        taskMedFake.doTask(taskData());

        // Assert
        ArgumentCaptor<OppgaveJournalføringEntitet> captor = ArgumentCaptor.forClass(OppgaveJournalføringEntitet.class);
        verify(journalføringRepository).lagre(captor.capture());
        OppgaveJournalføringEntitet lagret = captor.getValue();
        assertThat(lagret.getJournalpostId()).isEqualTo(new JournalpostId("123456789"));
        assertThat(lagret.getSakstype()).isEqualTo(Sakstype.GENERELL_SAK);
        assertThat(lagret.getSaksnummer()).isNull();
        assertThat(dokArkivKlientFake.getSisteRequest().tema()).isEqualTo(Tema.UNG.name());
        assertThat(dokArkivKlientFake.getSisteRequest().sak()).isEqualTo(no.nav.k9.felles.integrasjon.dokarkiv.dto.OpprettJournalpostRequest.Sak.GENERELL_FAGSAK);
    }

    @Test
    void doTask_ok_med_saksnummer_property_skal_lagre_rad_med_sakstype_fagsak() {
        // Arrange – ProsessTaskData har saksnummer satt, som når oppgavetypen krever fagsak.
        BrukerdialogOppgaveEntitet oppgave = arrangerOppgaveKlarForJournalføring(OppgaveType.BEKREFT_BOSTED);
        arrangerPersonOgDokumentutleder(oppgave, "Bekreft bosted");

        DokArkivKlientFake dokArkivKlientFake = new DokArkivKlientFake();
        dokArkivKlientFake.svarMedOk("123456789");
        JournalførOppgaveTask taskMedFake = new JournalførOppgaveTask(journalføringRepository, oppgaveRepository,
            true, dokumentUtledere, pdl, pdfGenerator, dokArkivKlientFake);

        ProsessTaskData data = taskData();
        data.setProperty(JournalførOppgaveTask.SAKSNUMMER, "ABC123");

        // Act
        taskMedFake.doTask(data);

        // Assert
        ArgumentCaptor<OppgaveJournalføringEntitet> captor = ArgumentCaptor.forClass(OppgaveJournalføringEntitet.class);
        verify(journalføringRepository).lagre(captor.capture());
        OppgaveJournalføringEntitet lagret = captor.getValue();
        assertThat(lagret.getSakstype()).isEqualTo(Sakstype.FAGSAK);
        assertThat(lagret.getSaksnummer()).isEqualTo(new Saksnummer("ABC123"));
    }

    @Test
    void doTask_409_skal_lagre_journalføringsrad_med_eksisterende_journalpostId() {
        // Journalposten finnes allerede i arkivet fra et tidligere (delvis fullført) forsøk.
        // Siden k9-felles 12.1.0 behandler DokarkivKlient et 409-svar som en gyldig respons med
        // journalpostId - tasken skal derfor lagre en rad, akkurat som ved en vanlig 201, ikke
        // feile.
        BrukerdialogOppgaveEntitet oppgave = arrangerOppgaveKlarForJournalføring(OppgaveType.SØK_YTELSE);
        arrangerPersonOgDokumentutleder(oppgave, "Søk ytelse");

        DokArkivKlientFake dokArkivKlientFake = new DokArkivKlientFake();
        dokArkivKlientFake.svarMedDuplikat("123456789");
        JournalførOppgaveTask taskMedFake = new JournalførOppgaveTask(journalføringRepository, oppgaveRepository,
            true, dokumentUtledere, pdl, pdfGenerator, dokArkivKlientFake);

        // Act
        taskMedFake.doTask(taskData());

        // Assert
        ArgumentCaptor<OppgaveJournalføringEntitet> captor = ArgumentCaptor.forClass(OppgaveJournalføringEntitet.class);
        verify(journalføringRepository).lagre(captor.capture());
        assertThat(captor.getValue().getJournalpostId()).isEqualTo(new JournalpostId("123456789"));
    }

    @Test
    void doTask_ikke_ferdigstilt_skal_kaste_illegalStateException_uten_å_lagre_rad() {
        // Journalposten ble opprettet i Dokarkiv, men automatisk ferdigstilling feilet (f.eks.
        // inaktivt tema eller manglende avsenderMottaker.navn). Tasken skal IKKE lagre raden -
        // den regnes ikke som journalført før den faktisk er ferdigstilt - og skal i stedet
        // kaste, slik at prosesstask-rammeverket retryer (en retry gjenbruker samme
        // eksternReferanseId, så Dokarkiv oppretter ikke noen duplikat journalpost, men forsøker
        // ferdigstilling på nytt).
        BrukerdialogOppgaveEntitet oppgave = arrangerOppgaveKlarForJournalføring(OppgaveType.SØK_YTELSE);
        arrangerPersonOgDokumentutleder(oppgave, "Søk ytelse");

        DokArkivKlientFake dokArkivKlientFake = new DokArkivKlientFake();
        dokArkivKlientFake.svarMedOpprettetIkkeFerdigstilt("123456789", "Tema=UNG er ikke gyldig for ferdigstilling");
        JournalførOppgaveTask taskMedFake = new JournalførOppgaveTask(journalføringRepository, oppgaveRepository,
            true, dokumentUtledere, pdl, pdfGenerator, dokArkivKlientFake);

        // Act & assert
        assertThatThrownBy(() -> taskMedFake.doTask(taskData()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("123456789")
            .hasMessageContaining(oppgavereferanse.toString())
            .hasMessageContaining("Tema=UNG er ikke gyldig for ferdigstilling");
        verify(journalføringRepository, never()).lagre(any());
    }

    @Test
    void doTask_5xx_skal_propagere_for_at_prosesstask_skal_retrye() {
        // Et transient serverfeil skal IKKE håndteres av tasken selv - unntaket skal propagere
        // slik at prosesstask-rammeverket gjør sitt vanlige retry-forsøk (maxFailedRuns=5,
        // firstDelay=60, thenDelay=300). Ingen rad skal lagres når forsøket feiler.
        BrukerdialogOppgaveEntitet oppgave = arrangerOppgaveKlarForJournalføring(OppgaveType.SØK_YTELSE);
        arrangerPersonOgDokumentutleder(oppgave, "Søk ytelse");

        DokArkivKlientFake dokArkivKlientFake = new DokArkivKlientFake();
        dokArkivKlientFake.svarMedFeil(503);
        JournalførOppgaveTask taskMedFake = new JournalførOppgaveTask(journalføringRepository, oppgaveRepository,
            true, dokumentUtledere, pdl, pdfGenerator, dokArkivKlientFake);

        // Act & assert
        assertThatThrownBy(() -> taskMedFake.doTask(taskData()))
            .isInstanceOf(HttpStatuskodeException.class);
        verify(journalføringRepository, never()).lagre(any());
    }

    @Test
    void doTask_person_uten_folkeregisterident_skal_kaste_journalforingException_uten_pii_i_meldingen() {
        // PDL gir Optional.empty() når personen ikke har et folkeregisterident (f.eks. kun
        // NPID). Meldingen skal navngi oppgavereferanse og oppgavetype - ALDRI aktørId eller
        // fødselsnummer.
        BrukerdialogOppgaveEntitet oppgave = oppgave(OppgaveType.SØK_YTELSE);
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.empty());
        when(oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)).thenReturn(Optional.of(oppgave));
        when(pdl.hentPersonIdentForAktørId(oppgave.getAktørId().getId())).thenReturn(Optional.empty());

        // Act & assert
        assertThatThrownBy(() -> task.doTask(taskData()))
            .isInstanceOf(JournalføringException.class)
            .hasMessageContaining(oppgavereferanse.toString())
            .hasMessageContaining(oppgave.getOppgaveType().name())
            .hasMessageNotContaining(oppgave.getAktørId().getId());
        verifyNoInteractions(dokArkivKlient);
    }

    @Test
    void doTask_pdlException_ved_navneoppslag_skal_propagere_for_retry() {
        // PDL-nedetid (5xx) er genuint retrybart - i motsetning til 409 skal unntaket her bare
        // propagere, ikke oversettes til JournalføringException.
        BrukerdialogOppgaveEntitet oppgave = arrangerOppgaveKlarForJournalføring(OppgaveType.SØK_YTELSE);
        when(pdl.hentPerson(any(), any(), anyList())).thenThrow(mock(PdlException.class));

        // Act & assert
        assertThatThrownBy(() -> task.doTask(taskData()))
            .isInstanceOf(PdlException.class);
        verifyNoInteractions(dokArkivKlient);
    }
}
