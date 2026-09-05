package no.nav.ung.brukerdialog.sak.diagnostikk;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.BaseEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.hibernate.annotations.Immutable;

@Entity(name = "DiagnostikkSakLogg")
@Table(name = "DIAGNOSTIKK_SAK_LOGG")
@Immutable
public class DiagnostikkSakLogg extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DIAGNOSTIKK_SAK_LOGG")
    private Long id;

    @Column(name = "aktoer_id", updatable = false)
    private String aktørId;

    @Column(name = "saksnummer", updatable = false)
    private String saksnummer;

    @Column(name = "tjeneste", updatable = false)
    private String tjeneste;

    @Column(name = "begrunnelse", updatable = false)
    private String begrunnelse;

    DiagnostikkSakLogg() {
        // Hibernate
    }

    public DiagnostikkSakLogg(AktørId aktørId, Saksnummer saksnummer, String tjeneste, String begrunnelse) {
        this.aktørId = aktørId != null ? aktørId.getId() : null;
        this.saksnummer = saksnummer != null ? saksnummer.getVerdi() : null;
        this.tjeneste = tjeneste;
        this.begrunnelse = begrunnelse;
    }

    public Long getId() {
        return id;
    }

    public String getAktørId() {
        return aktørId;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public String getTjeneste() {
        return tjeneste;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<saksnummer=" + saksnummer + ">";
    }

    // @Immutable dekker ikke DELETE
    @PreRemove
    protected void onDelete() {
        throw new IllegalStateException("Skal aldri kunne slette diagnostikk-logg. [id=" + id + "]");
    }
}
