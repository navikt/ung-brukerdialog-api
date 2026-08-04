package no.nav.ung.brukerdialog.oppgave;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.BaseEntitet;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveResponsDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "BrukerdialogOppgave")
@Table(name = "BD_OPPGAVE")
public class BrukerdialogOppgaveEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BD_OPPGAVE")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @Column(name = "oppgaveReferanse", nullable = false, updatable = false, unique = true)
    private UUID oppgavereferanse;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OppgaveStatus status = OppgaveStatus.ULØST;

    @Column(name = "ytelsetype")
    @Enumerated(EnumType.STRING)
    private OppgaveYtelsetype ytelsetype;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private OppgaveType oppgaveType;

    @Column(name = "frist_tid")
    private LocalDateTime fristTid;

    @Column(name = "løst_dato")
    private LocalDateTime løstDato; // NOSONAR

    @Convert(converter = OppgaveResponsConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "respons", columnDefinition = "jsonb")
    private OppgaveResponsDto respons;

    @OneToOne(mappedBy = "oppgave", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private OppgaveDataEntitet oppgaveData;

    protected BrukerdialogOppgaveEntitet() {
        // For JPA
    }

    public BrukerdialogOppgaveEntitet(UUID oppgavereferanse,
                                      OppgaveType oppgaveType,
                                      AktørId aktørId,
                                      OppgaveYtelsetype ytelsetype,
                                      LocalDateTime fristTid) {
        this.oppgavereferanse = oppgavereferanse;
        this.oppgaveType = oppgaveType;
        this.aktørId = aktørId;
        this.fristTid = fristTid;
        this.ytelsetype = ytelsetype != null ? ytelsetype : OppgaveYtelsetype.UNGDOMSYTELSE;
    }

    /**
     * Konstruktør for migrering av oppgave fra annen applikasjon.
     * Brukes når alle felter inkludert status og datoer skal settes.
     */
    public BrukerdialogOppgaveEntitet(UUID oppgavereferanse,
                                      OppgaveType oppgaveType,
                                      AktørId aktørId,
                                      OppgaveResponsDto respons,
                                      OppgaveStatus status,
                                      LocalDateTime fristTid,
                                      LocalDateTime løstDato,
                                      LocalDateTime opprettetTidspunkt,
                                      String opprettetAv) {
        this.oppgavereferanse = oppgavereferanse;
        this.oppgaveType = oppgaveType;
        this.aktørId = aktørId;
        this.respons = respons;
        this.status = status;
        this.fristTid = fristTid;
        this.løstDato = løstDato;
        this.ytelsetype = OppgaveYtelsetype.UNGDOMSYTELSE;
        this.setOpprettetTidspunkt(opprettetTidspunkt);
        this.opprettetAv = opprettetAv;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public UUID getOppgavereferanse() {
        return oppgavereferanse;
    }

    public OppgaveStatus getStatus() {
        return status;
    }

    public OppgaveType getOppgaveType() {
        return oppgaveType;
    }

    public OppgaveYtelsetype getYtelsetype() {
        return ytelsetype;
    }


    public LocalDateTime getFristTid() {
        return fristTid;
    }

    public void setFristTid(LocalDateTime fristTid) {
        this.fristTid = fristTid;
    }

    public boolean erUløst() {
        return status == OppgaveStatus.ULØST;
    }

    public void løs(OppgaveResponsDto respons) {
        validerKanEndres(OppgaveStatus.LØST);
        this.status = OppgaveStatus.LØST;
        this.løstDato = LocalDateTime.now();
        if (respons != null) {
            this.respons = respons;
        }
    }

    public void avbryt() {
        validerKanEndres(OppgaveStatus.AVBRUTT);
        this.status = OppgaveStatus.AVBRUTT;
    }

    public void utløp() {
        validerKanEndres(OppgaveStatus.UTLØPT);
        this.status = OppgaveStatus.UTLØPT;
    }

    // ULØST er eneste ikke-terminale status, så enhver overgang er kun lovlig derfra.
    private void validerKanEndres(OppgaveStatus nyStatus) {
        if (!erUløst()) {
            throw new UgyldigOppgaveStatusendringException(oppgavereferanse, status, nyStatus);
        }
    }

    /**
     * Setter status uten validering. Skal kun brukes ved migrering av oppgaver fra andre systemer,
     * der oppgaven allerede har en terminal status som må bevares.
     */
    public void settStatusVedMigrering(OppgaveStatus status) {
        this.status = status;
    }

    public LocalDateTime getLøstDato() {
        return løstDato;
    }

    public OppgaveResponsDto getRespons() {
        return respons;
    }

    Long getId() {
        return id;
    }

    public OppgaveDataEntitet getOppgaveData() {
        return oppgaveData;
    }

    public void setOppgaveData(OppgaveDataEntitet oppgaveData) {
        this.oppgaveData = oppgaveData;
        if (oppgaveData != null) {
            oppgaveData.setOppgave(this);
        }
    }
}
