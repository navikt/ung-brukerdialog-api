package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bistand;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BistandsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BistandsvilkårIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.time.LocalDate;
import java.util.Objects;

@Entity(name = "BekreftBistandOppgaveData")
@Table(name = "BD_OPPGAVE_DATA_BEKREFT_BISTAND")
@Access(AccessType.FIELD)
@OppgaveTypeRef(OppgaveType.BEKREFT_BISTAND)
public class BekreftBistandOppgaveDataEntitet extends OppgaveDataEntitet {

    @Column(name = "fom", nullable = false, updatable = false)
    private LocalDate fom;

    /** Null ved opphør – da er perioden åpen. */
    @Column(name = "tom", updatable = false)
    private LocalDate tom;

    @Enumerated(EnumType.STRING)
    @Column(name = "ikke_oppfylt_arsak", nullable = false, updatable = false)
    private BistandsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak;

    @Column(name = "ikke_oppfylt_arsak_fritekstbeskrivelse", updatable = false)
    private String ikkeOppfyltÅrsakFritekstbeskrivelse;

    @Enumerated(EnumType.STRING)
    @Column(name = "kilde", nullable = false, updatable = false)
    private BistandsavklaringKildeType kilde;

    @Column(name = "kilde_fritekst", updatable = false)
    private String kildeFritekst;

    protected BekreftBistandOppgaveDataEntitet() {
        // For JPA
    }

    public BekreftBistandOppgaveDataEntitet(LocalDate fom, LocalDate tom, BistandsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak, String ikkeOppfyltÅrsakFritekstbeskrivelse, BistandsavklaringKildeType kilde, String kildeFritekst) {
        this.fom = Objects.requireNonNull(fom, "fom");
        this.tom = tom;
        this.ikkeOppfyltÅrsak = Objects.requireNonNull(ikkeOppfyltÅrsak, "ikkeOppfyltÅrsak");
        this.ikkeOppfyltÅrsakFritekstbeskrivelse = ikkeOppfyltÅrsakFritekstbeskrivelse;
        this.kilde = Objects.requireNonNull(kilde, "kilde");
        if (kilde == BistandsavklaringKildeType.ANNET && (kildeFritekst == null || kildeFritekst.isBlank())) {
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

    public BistandsvilkårIkkeOppfyltÅrsak getIkkeOppfyltÅrsak() {
        return ikkeOppfyltÅrsak;
    }

    public String getIkkeOppfyltÅrsakFritekstbeskrivelse() {
        return ikkeOppfyltÅrsakFritekstbeskrivelse;
    }

    public BistandsavklaringKildeType getKilde() {
        return kilde;
    }

    public String getKildeFritekst() {
        return kildeFritekst;
    }
}
