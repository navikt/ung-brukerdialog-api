package no.nav.ung.brukerdialog.sak.mikrofrontend;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.BaseEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;

import java.util.Objects;

@Entity(name = "MinSideMikrofrontend")
@Table(name = "BD_MIN_SIDE_MIKROFRONTEND")
public class MinSideMikrofrontendEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BD_MIN_SIDE_MIKROFRONTEND")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mikrofrontend_id", nullable = false, updatable = false)
    private MikrofrontendId mikrofrontendId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MikrofrontendStatus status;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    protected MinSideMikrofrontendEntitet() {
        // For JPA
    }

    public MinSideMikrofrontendEntitet(AktørId aktørId, MikrofrontendId mikrofrontendId, MikrofrontendStatus status) {
        this.aktørId = Objects.requireNonNull(aktørId, "aktørId");
        this.mikrofrontendId = Objects.requireNonNull(mikrofrontendId, "mikrofrontendId");
        this.status = Objects.requireNonNull(status, "status");
    }

    public Long getId() {
        return id;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public MikrofrontendId getMikrofrontendId() {
        return mikrofrontendId;
    }

    public MikrofrontendStatus getStatus() {
        return status;
    }

    void setStatus(MikrofrontendStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + id + ", mikrofrontendId=" + mikrofrontendId + ", status=" + status + ">";
    }
}
