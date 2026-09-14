package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.livsopphold;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserAvklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.AndreLivsoppholdsytelserIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.time.LocalDate;
import java.util.Objects;

@Entity(name = "BekreftAndreLivsoppholdsytelserOppgaveData")
@Table(name = "BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER")
@Access(AccessType.FIELD)
@OppgaveTypeRef(OppgaveType.BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER)
public class BekreftAndreLivsoppholdsytelserOppgaveDataEntitet extends OppgaveDataEntitet {

    @Column(name = "fom", nullable = false, updatable = false)
    private LocalDate fom;

    /** Null ved opphør – da er perioden åpen. */
    @Column(name = "tom", updatable = false)
    private LocalDate tom;

    @Enumerated(EnumType.STRING)
    @Column(name = "ikke_oppfylt_arsak", nullable = false, updatable = false)
    private AndreLivsoppholdsytelserIkkeOppfyltÅrsak ikkeOppfyltÅrsak;

    @Column(name = "ikke_oppfylt_arsak_fritekstbeskrivelse", updatable = false)
    private String ikkeOppfyltÅrsakFritekstbeskrivelse;

    @Enumerated(EnumType.STRING)
    @Column(name = "kilde", nullable = false, updatable = false)
    private AndreLivsoppholdsytelserAvklaringKildeType kilde;

    @Column(name = "kilde_fritekst", updatable = false)
    private String kildeFritekst;

    protected BekreftAndreLivsoppholdsytelserOppgaveDataEntitet() {
        // For JPA
    }

    public BekreftAndreLivsoppholdsytelserOppgaveDataEntitet(LocalDate fom, LocalDate tom, AndreLivsoppholdsytelserIkkeOppfyltÅrsak ikkeOppfyltÅrsak, String ikkeOppfyltÅrsakFritekstbeskrivelse, AndreLivsoppholdsytelserAvklaringKildeType kilde, String kildeFritekst) {
        this.fom = Objects.requireNonNull(fom, "fom");
        this.tom = tom;
        this.ikkeOppfyltÅrsak = Objects.requireNonNull(ikkeOppfyltÅrsak, "ikkeOppfyltÅrsak");
        this.ikkeOppfyltÅrsakFritekstbeskrivelse = ikkeOppfyltÅrsakFritekstbeskrivelse;
        this.kilde = Objects.requireNonNull(kilde, "kilde");
        if (kilde == AndreLivsoppholdsytelserAvklaringKildeType.ANNET && (kildeFritekst == null || kildeFritekst.isBlank())) {
            throw new IllegalArgumentException("kildeFritekst er påkrevd når kilde = ANNET");
        }
        this.kildeFritekst = kildeFritekst;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public AndreLivsoppholdsytelserIkkeOppfyltÅrsak getIkkeOppfyltÅrsak() {
        return ikkeOppfyltÅrsak;
    }

    public String getIkkeOppfyltÅrsakFritekstbeskrivelse() {
        return ikkeOppfyltÅrsakFritekstbeskrivelse;
    }

    public AndreLivsoppholdsytelserAvklaringKildeType getKilde() {
        return kilde;
    }

    public String getKildeFritekst() {
        return kildeFritekst;
    }
}
