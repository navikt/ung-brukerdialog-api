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
 * Raden opprettes <b>alltid</b> ved oppgaveopprettelse, uavhengig av om
 * {@code JournalføringKonfig} er aktivert for oppgavetypen - det er kun den tilhørende
 * {@code JournalførOppgaveTask} som er betinget av konfigurasjonen. Dermed blir
 * et eventuelt etterslep (flagget av, eller tasken har ikke kjørt ennå) komplett og spørrbart:
 * {@code status = 'PLANLAGT'}.
 * <p>
 * {@code tema}, {@code fagsaksystem} og {@code sakstype} utledes én gang ved opprettelse (se
 * {@code JournalføringParametre.utled(...)}) og lagres her - raden er dermed et etterrettelig
 * spor av hva som faktisk ble sendt til arkivet, uavhengig av senere endringer i
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JournalføringStatus status = JournalføringStatus.PLANLAGT;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "journalpostId", column = @Column(name = "journalpost_id")))
    private JournalpostId journalpostId;

    @Column(name = "journalfort_tid")
    private LocalDateTime journalførtTid;

    protected OppgaveJournalføringEntitet() {
        // For JPA
    }

    /**
     * @param fagsakId må være satt når {@code sakstype == FAGSAK}, og må være {@code null} når
     *                 {@code sakstype == GENERELL_SAK} - håndheves både her og som DB-constraint
     *                 (defence in depth).
     */
    public OppgaveJournalføringEntitet(BrukerdialogOppgaveEntitet oppgave,
                                        Tema tema,
                                        Fagsaksystem fagsaksystem,
                                        Sakstype sakstype,
                                        Saksnummer fagsakId) {
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

    public JournalføringStatus getStatus() {
        return status;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public LocalDateTime getJournalførtTid() {
        return journalførtTid;
    }

    public boolean erJournalført() {
        return status == JournalføringStatus.JOURNALFORT;
    }

    /**
     * Eneste tillatte tilstandsovergang: fra {@code PLANLAGT} til {@code JOURNALFORT}. Setter
     * {@code journalpostId} og {@code status} atomisk, slik at invarianten «journalpost_id satt
     * ⟺ status = JOURNALFORT» (håndhevet som DB-constraint) aldri kan brytes fra
     * applikasjonssiden. Vi lagrer aldri en journalført oppgave uten journalpostId - denne
     * metoden er derfor den eneste veien til {@code JOURNALFORT}.
     */
    public void markerJournalført(JournalpostId journalpostId) {
        Objects.requireNonNull(journalpostId, "journalpostId");
        if (erJournalført()) {
            throw new IllegalStateException(
                "Oppgave er allerede journalført med journalpostId " + this.journalpostId);
        }
        this.journalpostId = journalpostId;
        this.status = JournalføringStatus.JOURNALFORT;
        this.journalførtTid = LocalDateTime.now();
    }
}
