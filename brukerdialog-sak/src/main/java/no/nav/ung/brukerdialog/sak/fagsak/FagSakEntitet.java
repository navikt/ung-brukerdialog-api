package no.nav.ung.brukerdialog.sak.fagsak;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.BaseEntitet;
import no.nav.ung.brukerdialog.sak.soknad.FagsakYtelseType;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Periode;
import no.nav.ung.brukerdialog.typer.Saksnummer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "Fagsak")
@Table(name = "BD_FAGSAK")
public class FagSakEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BD_FAGSAK")
    private Long id;


    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ytelse_type", nullable = false, updatable = false)
    private FagsakYtelseType ytelseType;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer", nullable = false, updatable = false)))
    private Saksnummer saksnummer;

    @OneToMany(mappedBy = "fagsak", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VedtakPeriodeEntitet> perioder = new ArrayList<>();

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    FagSakEntitet() {
        // For JPA
    }

    public FagSakEntitet(AktørId aktørId, FagsakYtelseType ytelseType, Saksnummer saksnummer) {
        this.aktørId = Objects.requireNonNull(aktørId, "aktørId");
        this.ytelseType = Objects.requireNonNull(ytelseType, "ytelseType");
        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
    }

    public Long getId() {
        return id;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public FagsakYtelseType getYtelseType() {
        return ytelseType;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public List<VedtakPeriodeEntitet> getPerioder() {
        return perioder;
    }

    public void settPerioder(List<Periode> nyePerioder) {
        Objects.requireNonNull(nyePerioder, "nyePerioder").forEach(
            p -> this.perioder.add(new VedtakPeriodeEntitet(this, p.getFom(), p.getTom())));
    }

    public void deaktiver() {
        aktiv = false;
    }
}
