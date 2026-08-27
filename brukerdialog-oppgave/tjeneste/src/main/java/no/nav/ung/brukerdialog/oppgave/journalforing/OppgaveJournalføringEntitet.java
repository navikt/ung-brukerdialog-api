package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.ung.brukerdialog.BaseEntitet;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.typer.JournalpostId;
import no.nav.ung.brukerdialog.typer.Saksnummer;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Journalføring av en brukerdialogoppgave mot Dokarkiv. Én rad per oppgave.
 * <p>
 * Raden finnes hvis og bare hvis oppgaven faktisk er journalført - det finnes ingen
 * mellomtilstand. Enten journalfører {@code JournalførOppgaveTask} og lagrer en komplett rad
 * (med {@code journalpostId} satt), eller så skjer det ingenting (ingen rad).
 * <p>
 * {@code tema}/{@code fagsaksystem}/{@code sakstype} utledes av tasken idet journalføringen
 * lykkes, og lagres her som et etterrettelig spor - uavhengig av senere endringer i
 * utledningsregelen.
 */
@Entity(name = "OppgaveJournalføring")
@Table(name = "BD_OPPGAVE_JOURNALFORING")
public class OppgaveJournalføringEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BD_OPPGAVE_JOURNALFORING")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bd_oppgave_id", nullable = false, updatable = false)
    private BrukerdialogOppgaveEntitet oppgave;

    @Enumerated(EnumType.STRING)
    @Column(name = "tema", nullable = false, updatable = false)
    private Tema tema;

    @Enumerated(EnumType.STRING)
    @Column(name = "fagsaksystem", nullable = false, updatable = false)
    private Fagsaksystem fagsaksystem;

    @Enumerated(EnumType.STRING)
    @Column(name = "sakstype", nullable = false, updatable = false)
    private Sakstype sakstype;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "fagsak_id", updatable = false)))
    private Saksnummer fagsakId;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "journalpostId", column = @Column(name = "journalpost_id", nullable = false, updatable = false)))
    private JournalpostId journalpostId;

    @Column(name = "journalfort_tid", nullable = false, updatable = false)
    private LocalDateTime journalførtTid;

    protected OppgaveJournalføringEntitet() {
        // For JPA
    }

    /**
     * @param fagsakId     påkrevd for {@code FAGSAK}, forbudt for {@code GENERELL_SAK} - håndhevet
     *                     her og som DB-constraint.
     * @param journalpostId journalpost-ID-en Dokarkiv returnerte ved vellykket journalføring -
     *                      raden skal aldri opprettes før dette er kjent.
     */
    public OppgaveJournalføringEntitet(BrukerdialogOppgaveEntitet oppgave,
                                        Tema tema,
                                        Fagsaksystem fagsaksystem,
                                        Sakstype sakstype,
                                        Saksnummer fagsakId,
                                        JournalpostId journalpostId) {
        this.oppgave = Objects.requireNonNull(oppgave, "oppgave");
        this.tema = Objects.requireNonNull(tema, "tema");
        this.fagsaksystem = Objects.requireNonNull(fagsaksystem, "fagsaksystem");
        this.sakstype = Objects.requireNonNull(sakstype, "sakstype");
        if (sakstype == Sakstype.FAGSAK && fagsakId == null) {
            throw new IllegalArgumentException("fagsakId må være satt når sakstype er FAGSAK");
        }
        if (sakstype == Sakstype.GENERELL_SAK && fagsakId != null) {
            throw new IllegalArgumentException("fagsakId skal ikke være satt når sakstype er GENERELL_SAK");
        }
        this.fagsakId = fagsakId;
        this.journalpostId = Objects.requireNonNull(journalpostId, "journalpostId");
        this.journalførtTid = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public BrukerdialogOppgaveEntitet getOppgave() {
        return oppgave;
    }

    public Tema getTema() {
        return tema;
    }

    public Fagsaksystem getFagsaksystem() {
        return fagsaksystem;
    }

    public Sakstype getSakstype() {
        return sakstype;
    }

    public Saksnummer getFagsakId() {
        return fagsakId;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public LocalDateTime getJournalførtTid() {
        return journalførtTid;
    }
}
