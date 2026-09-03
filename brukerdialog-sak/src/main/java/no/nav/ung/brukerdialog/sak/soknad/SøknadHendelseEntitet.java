package no.nav.ung.brukerdialog.sak.soknad;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.BaseEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "SøknadHendelse")
@Table(name = "BD_SOKNAD_HENDELSE")
public class SøknadHendelseEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BD_SOKNAD_HENDELSE")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "soknad_id", nullable = false, updatable = false, unique = true)
    private UUID søknadId;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ytelse_type", nullable = false, updatable = false)
    private FagsakYtelseType ytelseType;

    @Column(name = "mottatt", nullable = false, updatable = false)
    private LocalDateTime mottatt;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    protected SøknadHendelseEntitet() {
        // For JPA
    }

    public SøknadHendelseEntitet(UUID søknadId, AktørId aktørId, FagsakYtelseType ytelseType, LocalDateTime mottatt) {
        this.søknadId = Objects.requireNonNull(søknadId, "søknadId");
        this.aktørId = Objects.requireNonNull(aktørId, "aktørId");
        this.ytelseType = Objects.requireNonNull(ytelseType, "ytelseType");
        this.mottatt = Objects.requireNonNull(mottatt, "mottatt");
    }

    public Long getId() {
        return id;
    }

    public UUID getSøknadId() {
        return søknadId;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public FagsakYtelseType getYtelseType() {
        return ytelseType;
    }

    public LocalDateTime getMottatt() {
        return mottatt;
    }

    public void deaktiver() {
        this.aktiv = false;
    }
}
