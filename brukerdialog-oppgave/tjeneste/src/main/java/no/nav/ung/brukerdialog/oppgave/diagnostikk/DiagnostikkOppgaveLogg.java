package no.nav.ung.brukerdialog.oppgave.diagnostikk;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.BaseEntitet;

import java.util.UUID;

@Entity(name = "DiagnostikkOppgaveLogg")
@Table(name = "DIAGNOSTIKK_OPPGAVE_LOGG")
public class DiagnostikkOppgaveLogg extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DIAGNOSTIKK_OPPGAVE_LOGG")
    @SequenceGenerator(name = "SEQ_DIAGNOSTIKK_OPPGAVE_LOGG", sequenceName = "SEQ_DIAGNOSTIKK_OPPGAVE_LOGG", allocationSize = 50)
    @Column(name = "id")
    private Long id;

    @Column(name = "oppgavereferanse", nullable = false, updatable = false)
    private UUID oppgavereferanse;

    @Column(name = "tjeneste", updatable = false, length = 200)
    private String tjeneste;

    @Column(name = "begrunnelse", updatable = false, length = 4000)
    private String begrunnelse;

    DiagnostikkOppgaveLogg() {
        // Hibernate
    }

    public DiagnostikkOppgaveLogg(UUID oppgavereferanse, String tjeneste, String begrunnelse) {
        this.oppgavereferanse = oppgavereferanse;
        this.tjeneste = tjeneste;
        this.begrunnelse = begrunnelse;
    }

    public Long getId() {
        return id;
    }

    public UUID getOppgavereferanse() {
        return oppgavereferanse;
    }

    public String getTjeneste() {
        return tjeneste;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<oppgavereferanse=" + oppgavereferanse + ">";
    }

    @PreRemove
    protected void onDelete() {
        throw new IllegalStateException("Skal aldri kunne slette diagnostikk-logg. [id=" + id + "]");
    }
}
