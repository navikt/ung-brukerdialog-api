package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.enterprise.inject.Instance;
import no.nav.k9.felles.integrasjon.pdl.Navn;
import no.nav.k9.felles.integrasjon.pdl.Pdl;
import no.nav.k9.felles.integrasjon.pdl.PdlException;
import no.nav.k9.felles.integrasjon.pdl.Person;
import no.nav.k9.felles.log.metrics.MetricsUtil;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.felles.exception.HttpStatuskodeException;
import no.nav.ung.brukerdialog.journalforing.dokarkiv.DokArkivKlient;
import no.nav.ung.brukerdialog.journalforing.pdf.PdfGenerator;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveRepository;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.JournalpostId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * HTTP-server (409 skal feile, ikke lagre uten journalpostId). PDL-hullene - person uten
 * folkeregisterident, og PdlException ved navneoppslag - er dekket separat.
 */
@ExtendWith(MockitoExtension.class)
class JournalførOppgaveTaskTest {

    @Mock
    private OppgaveJournalføringRepository journalføringRepository;
    @Mock
    private BrukerdialogOppgaveRepository oppgaveRepository;
    @Mock
    private JournalføringKonfig journalføringKonfig;
    @Mock
    private Instance<OppgaveDokumentUtleder> dokumentUtledere;
    @Mock
    private Instance<OppgaveDokumentUtleder> dokumentUtlederValgt;
    @Mock
    private OppgaveDokumentUtleder dokumentUtleder;
    @Mock
    private Pdl pdl;
    @Mock
    private PdfGenerator pdfGenerator;
    @Mock
    private DokArkivKlient dokArkivKlient;

    private JournalførOppgaveTask task;
    private UUID oppgavereferanse;

    @BeforeEach
    void setUp() {
        task = new JournalførOppgaveTask(journalføringRepository, oppgaveRepository, journalføringKonfig,
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

    private OppgaveJournalføringEntitet planlagtJournalføring(OppgaveType oppgaveType) {
        return new OppgaveJournalføringEntitet(oppgave(oppgaveType), Tema.UNG, Fagsaksystem.UNG_SAK,
            Sakstype.GENERELL_SAK, null);
    }

    /**
     * Leser gjeldende verdi på {@code ung_brukerdialog_journalforing_total} for kombinasjonen.
     * {@code MetricsUtil.REGISTRY} er en statisk, delt registry, så testene sammenligner
     * før/etter i stedet for å anta en startverdi på 0.
     */
    private double telleverdi(OppgaveType oppgaveType, JournalføringMetrikker.Resultat resultat) {
        var counter = MetricsUtil.REGISTRY.find("ung_brukerdialog_journalforing_total")
            .tag("oppgavetype", oppgaveType.name())
            .tag("resultat", resultat.name())
            .counter();
        return counter == null ? 0.0 : counter.count();
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

    @Test
    void requiredProperties_skal_kun_kreve_oppgaveReferanse() {
        assertThat(task.requiredProperties()).containsExactly(JournalførOppgaveTask.OPPGAVE_REFERANSE);
    }

    @Test
    void doTask_uten_journalforingsrad_skal_kaste_illegalStateException() {
        // Arrange
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.empty());

        // Act & assert
        assertThatThrownBy(() -> task.doTask(taskData()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(oppgavereferanse.toString());
        verifyNoInteractions(oppgaveRepository, journalføringKonfig, pdl, pdfGenerator, dokArkivKlient, dokumentUtledere);
    }

    @Test
    void doTask_allerede_journalfort_skal_hoppe_over_idempotent() {
        // Arrange – status er allerede JOURNALFORT (journalpostId settes kun ved reell suksess)
        OppgaveJournalføringEntitet journalføring = planlagtJournalføring(OppgaveType.SØK_YTELSE);
        journalføring.markerJournalført(new JournalpostId("123456789"));
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.of(journalføring));
        double førTelleverdi = telleverdi(OppgaveType.SØK_YTELSE, JournalføringMetrikker.Resultat.HOPPET_OVER);

        // Act
        task.doTask(taskData());

        // Assert – ingen videre behandling, og status/journalpostId endres ikke
        verifyNoInteractions(oppgaveRepository, journalføringKonfig, pdl, pdfGenerator, dokArkivKlient, dokumentUtledere);
        verify(journalføringRepository, never()).oppdater(any());
        // Idempotent-hoppet skal telles som HOPPET_OVER, ikke stille forbigås
        assertThat(telleverdi(OppgaveType.SØK_YTELSE, JournalføringMetrikker.Resultat.HOPPET_OVER)).isEqualTo(førTelleverdi + 1);
    }

    @Test
    void doTask_oppgave_ikke_funnet_skal_kaste_illegalStateException() {
        // Arrange – journalføringsraden finnes, men oppgaven den peker på gjør (uventet) ikke
        OppgaveJournalføringEntitet journalføring = planlagtJournalføring(OppgaveType.SØK_YTELSE);
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.of(journalføring));
        when(oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)).thenReturn(Optional.empty());

        // Act & assert
        assertThatThrownBy(() -> task.doTask(taskData()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(oppgavereferanse.toString());
        verifyNoInteractions(journalføringKonfig, pdl, pdfGenerator, dokArkivKlient, dokumentUtledere);
    }

    @Test
    void doTask_deaktivert_for_oppgavetype_skal_hoppe_over_og_forbli_planlagt() {
        // Arrange – raden forblir PLANLAGT når konfigurasjonen er slått av for oppgavetypen
        // etter at raden ble opprettet.
        OppgaveJournalføringEntitet journalføring = planlagtJournalføring(OppgaveType.SØK_YTELSE);
        BrukerdialogOppgaveEntitet oppgave = journalføring.getOppgave();
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.of(journalføring));
        when(oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)).thenReturn(Optional.of(oppgave));
        when(journalføringKonfig.erAktivertFor(OppgaveType.SØK_YTELSE)).thenReturn(false);
        double førTelleverdi = telleverdi(OppgaveType.SØK_YTELSE, JournalføringMetrikker.Resultat.HOPPET_OVER);

        // Act
        task.doTask(taskData());

        // Assert
        assertThat(journalføring.erJournalført()).isFalse();
        verifyNoInteractions(pdl, pdfGenerator, dokArkivKlient, dokumentUtledere);
        verify(journalføringRepository, never()).oppdater(any());
        // Deaktivert-hoppet skal telles som HOPPET_OVER, ikke stille forbigås
        assertThat(telleverdi(OppgaveType.SØK_YTELSE, JournalføringMetrikker.Resultat.HOPPET_OVER)).isEqualTo(førTelleverdi + 1);
    }

    // ------------------------------------------------------------------------------------------
    // Veiene som går via hentPersonInfo.
    // ------------------------------------------------------------------------------------------

    @Test
    void doTask_ok_skal_markere_journalfort_med_journalpostId_fra_dokarkiv() {
        // Arrange
        OppgaveJournalføringEntitet journalføring = planlagtJournalføring(OppgaveType.SØK_YTELSE);
        BrukerdialogOppgaveEntitet oppgave = journalføring.getOppgave();
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.of(journalføring));
        when(oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)).thenReturn(Optional.of(oppgave));
        when(journalføringKonfig.erAktivertFor(OppgaveType.SØK_YTELSE)).thenReturn(true);
        when(pdl.hentPersonIdentForAktørId(oppgave.getAktørId().getId())).thenReturn(Optional.of("12345678901"));
        arrangerPersonOgDokumentutleder(oppgave, "Søk ytelse");

        DokArkivKlientFake dokArkivKlientFake = new DokArkivKlientFake();
        dokArkivKlientFake.svarMedOk("123456789");
        JournalførOppgaveTask taskMedFake = new JournalførOppgaveTask(journalføringRepository, oppgaveRepository,
            journalføringKonfig, dokumentUtledere, pdl, pdfGenerator, dokArkivKlientFake);

        // Act
        taskMedFake.doTask(taskData());

        // Assert
        assertThat(journalføring.erJournalført()).isTrue();
        assertThat(journalføring.getJournalpostId()).isEqualTo(new JournalpostId("123456789"));
        verify(journalføringRepository).oppdater(journalføring);
        assertThat(dokArkivKlientFake.getSisteRequest().tema()).isEqualTo(Tema.UNG.name());
        assertThat(dokArkivKlientFake.getSisteRequest().sak()).isEqualTo(no.nav.ung.brukerdialog.journalforing.dokarkiv.dto.OpprettJournalpostRequest.Sak.GENERELL_SAK);
    }

    @Test
    void doTask_409_skal_kaste_journalforingException_og_ikke_lagre_journalpostId() {
        // Journalposten finnes allerede i arkivet, men journalpostId kan ikke leses fra
        // 409-responsen med dagens OidcRestClient. Vi lagrer aldri en journalført rad uten
        // journalpostId - tasken skal derfor feile, ikke hoppe stille over.
        OppgaveJournalføringEntitet journalføring = planlagtJournalføring(OppgaveType.SØK_YTELSE);
        BrukerdialogOppgaveEntitet oppgave = journalføring.getOppgave();
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.of(journalføring));
        when(oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)).thenReturn(Optional.of(oppgave));
        when(journalføringKonfig.erAktivertFor(OppgaveType.SØK_YTELSE)).thenReturn(true);
        when(pdl.hentPersonIdentForAktørId(oppgave.getAktørId().getId())).thenReturn(Optional.of("12345678901"));
        arrangerPersonOgDokumentutleder(oppgave, "Søk ytelse");

        DokArkivKlientFake dokArkivKlientFake = new DokArkivKlientFake();
        dokArkivKlientFake.svarMed409();
        JournalførOppgaveTask taskMedFake = new JournalførOppgaveTask(journalføringRepository, oppgaveRepository,
            journalføringKonfig, dokumentUtledere, pdl, pdfGenerator, dokArkivKlientFake);

        // Act & assert
        assertThatThrownBy(() -> taskMedFake.doTask(taskData()))
            .isInstanceOf(JournalføringException.class)
            .hasMessageContaining(oppgavereferanse.toString());
        assertThat(journalføring.erJournalført()).isFalse();
        verify(journalføringRepository, never()).oppdater(any());
    }

    @Test
    void doTask_5xx_skal_propagere_for_at_prosesstask_skal_retrye() {
        // Et transient serverfeil skal IKKE håndteres av tasken selv - unntaket skal propagere
        // slik at prosesstask-rammeverket gjør sitt vanlige retry-forsøk (maxFailedRuns=5,
        // firstDelay=60, thenDelay=300).
        OppgaveJournalføringEntitet journalføring = planlagtJournalføring(OppgaveType.SØK_YTELSE);
        BrukerdialogOppgaveEntitet oppgave = journalføring.getOppgave();
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.of(journalføring));
        when(oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)).thenReturn(Optional.of(oppgave));
        when(journalføringKonfig.erAktivertFor(OppgaveType.SØK_YTELSE)).thenReturn(true);
        when(pdl.hentPersonIdentForAktørId(oppgave.getAktørId().getId())).thenReturn(Optional.of("12345678901"));
        arrangerPersonOgDokumentutleder(oppgave, "Søk ytelse");

        DokArkivKlientFake dokArkivKlientFake = new DokArkivKlientFake();
        dokArkivKlientFake.svarMedFeil(503);
        JournalførOppgaveTask taskMedFake = new JournalførOppgaveTask(journalføringRepository, oppgaveRepository,
            journalføringKonfig, dokumentUtledere, pdl, pdfGenerator, dokArkivKlientFake);

        // Act & assert
        assertThatThrownBy(() -> taskMedFake.doTask(taskData()))
            .isInstanceOf(HttpStatuskodeException.class);
        assertThat(journalføring.erJournalført()).isFalse();
        verify(journalføringRepository, never()).oppdater(any());
    }

    @Test
    void doTask_person_uten_folkeregisterident_skal_kaste_journalforingException_uten_pii_i_meldingen() {
        // PDL gir Optional.empty() når personen ikke har et folkeregisterident (f.eks. kun
        // NPID). Meldingen skal navngi oppgavereferanse og oppgavetype - ALDRI aktørId eller
        // fødselsnummer.
        OppgaveJournalføringEntitet journalføring = planlagtJournalføring(OppgaveType.SØK_YTELSE);
        BrukerdialogOppgaveEntitet oppgave = journalføring.getOppgave();
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.of(journalføring));
        when(oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)).thenReturn(Optional.of(oppgave));
        when(journalføringKonfig.erAktivertFor(OppgaveType.SØK_YTELSE)).thenReturn(true);
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
        OppgaveJournalføringEntitet journalføring = planlagtJournalføring(OppgaveType.SØK_YTELSE);
        BrukerdialogOppgaveEntitet oppgave = journalføring.getOppgave();
        when(journalføringRepository.hentForOppgaveReferanse(oppgavereferanse)).thenReturn(Optional.of(journalføring));
        when(oppgaveRepository.hentOppgaveForOppgavereferanse(oppgavereferanse)).thenReturn(Optional.of(oppgave));
        when(journalføringKonfig.erAktivertFor(OppgaveType.SØK_YTELSE)).thenReturn(true);
        when(pdl.hentPersonIdentForAktørId(oppgave.getAktørId().getId())).thenReturn(Optional.of("12345678901"));
        when(pdl.hentPerson(any(), any(), anyList())).thenThrow(mock(PdlException.class));

        // Act & assert
        assertThatThrownBy(() -> task.doTask(taskData()))
            .isInstanceOf(PdlException.class);
        verifyNoInteractions(dokArkivKlient);
    }
}
