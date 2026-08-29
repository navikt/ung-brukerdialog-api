package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bosted;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.time.LocalDate;
import java.util.Objects;

@Entity(name = "BekreftBostedOppgaveData")
@Table(name = "BD_OPPGAVE_DATA_BEKREFT_BOSTED")
@Access(AccessType.FIELD)
@OppgaveTypeRef(OppgaveType.BEKREFT_BOSTED)
public class BekreftBostedOppgaveDataEntitet extends OppgaveDataEntitet {

    @Column(name = "fom", nullable = false, updatable = false)
    private LocalDate fom;

    @Column(name = "tom", updatable = false)
    private LocalDate tom;

    @Column(name = "er_bosatt_i_trondheim", nullable = false, updatable = false)
    private boolean erBosattITrondheim;

    @Column(name = "ikke_oppfylt_arsak_fritekstbeskrivelse", updatable = false)
    private String ikkeOppfyltÅrsakFritekstbeskrivelse;

    @Enumerated(EnumType.STRING)
    @Column(name = "ikke_oppfylt_arsak", nullable = false, updatable = false)
    private BostedsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak;

    @Enumerated(EnumType.STRING)
    @Column(name = "kilde", nullable = false, updatable = false)
    private BostedsavklaringKildeType kilde;

    @Column(name = "kilde_fritekst", updatable = false)
    private String kildeFritekst;

    protected BekreftBostedOppgaveDataEntitet() {
        // For JPA
    }

    public BekreftBostedOppgaveDataEntitet(LocalDate fom, LocalDate tom, boolean erBosattITrondheim, String ikkeOppfyltÅrsakFritekstbeskrivelse, BostedsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak, BostedsavklaringKildeType kilde, String kildeFritekst) {
        this.fom = fom;
        this.tom = tom;
        this.erBosattITrondheim = erBosattITrondheim;
        this.ikkeOppfyltÅrsakFritekstbeskrivelse = ikkeOppfyltÅrsakFritekstbeskrivelse;
        this.ikkeOppfyltÅrsak = ikkeOppfyltÅrsak;
        this.kilde = Objects.requireNonNull(kilde, "kilde");
        this.kildeFritekst = kildeFritekst;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public boolean isErBosattITrondheim() {
        return erBosattITrondheim;
    }

    public String getIkkeOppfyltÅrsakFritekstbeskrivelse() {
        return ikkeOppfyltÅrsakFritekstbeskrivelse;
    }

    public BostedsvilkårIkkeOppfyltÅrsak getIkkeOppfyltÅrsak() {
        return ikkeOppfyltÅrsak;
    }

    public BostedsavklaringKildeType getKilde() {
        return kilde;
    }

    public String getKildeFritekst() {
        return kildeFritekst;
    }
}
