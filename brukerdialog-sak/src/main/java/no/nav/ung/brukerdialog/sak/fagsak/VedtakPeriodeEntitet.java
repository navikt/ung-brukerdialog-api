package no.nav.ung.brukerdialog.sak.fagsak;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.BaseEntitet;
import no.nav.ung.brukerdialog.kontrakt.vedtak.VedtakResultatType;
import no.nav.ung.brukerdialog.tid.DatoIntervallEntitet;
import no.nav.ung.brukerdialog.tid.PostgreSQLRangeType;
import no.nav.ung.brukerdialog.tid.Range;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.util.Objects;

@Entity(name = "VedtakPeriode")
@Table(name = "BD_VEDTAK_PERIODE")
public class VedtakPeriodeEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BD_VEDTAK_PERIODE")
    private Long id;

    @Type(PostgreSQLRangeType.class)
    @Column(name = "periode", columnDefinition = "daterange", nullable = false, updatable = false)
    private Range<LocalDate> periode;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultat", nullable = false, updatable = false)
    private VedtakResultatType resultat;

    @ManyToOne
    @JoinColumn(name = "fagsak_id", nullable = false, updatable = false)
    private FagSakEntitet fagsak;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    protected VedtakPeriodeEntitet() {
        // For JPA
    }

    VedtakPeriodeEntitet(FagSakEntitet fagsak, LocalDate fom, LocalDate tom, VedtakResultatType resultat) {
        this.fagsak = Objects.requireNonNull(fagsak, "fagsak");
        this.resultat = Objects.requireNonNull(resultat, "resultat");
        Objects.requireNonNull(fom, "fom");
        Objects.requireNonNull(tom, "tom");
        this.periode = DatoIntervallEntitet.fra(fom, tom).toRange();
    }

    public Long getId() {
        return id;
    }

    public DatoIntervallEntitet getPeriode() {
        return DatoIntervallEntitet.fra(periode);
    }

    public VedtakResultatType getResultat() {
        return resultat;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    void deaktiver() {
        this.aktiv = false;
    }
}
