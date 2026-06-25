package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bosted;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.time.LocalDate;

@Entity(name = "BekreftBostedOppgaveData")
@Table(name = "BD_OPPGAVE_DATA_BEKREFT_BOSTED")
@Access(AccessType.FIELD)
@OppgaveTypeRef(OppgaveType.BEKREFT_BOSTED)
public class BekreftBostedOppgaveDataEntitet extends OppgaveDataEntitet {

    @Column(name = "fom", nullable = false, updatable = false)
    private LocalDate fom;

    @Column(name = "tom", nullable = false, updatable = false)
    private LocalDate tom;

    @Column(name = "er_bosatt_i_trondheim", nullable = false, updatable = false)
    private boolean erBosattITrondheim;

    @Column(name = "ikke_oppfylt_arsak_fritekstbeskrivelse", nullable = false, updatable = false)
    private String ikkeOppfyltÅrsakFritekstbekrivelse;

    @Enumerated(EnumType.STRING)
    @Column(name = "ikke_oppfylt_arsak", nullable = false, updatable = false)
    private BostedsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak;

    protected BekreftBostedOppgaveDataEntitet() {
        // For JPA
    }

    public BekreftBostedOppgaveDataEntitet(LocalDate fom, LocalDate tom, boolean erBosattITrondheim, String ikkeOppfyltÅrsakFritekstbekrivelse, BostedsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak) {
        this.fom = fom;
        this.tom = tom;
        this.erBosattITrondheim = erBosattITrondheim;
        this.ikkeOppfyltÅrsakFritekstbekrivelse = ikkeOppfyltÅrsakFritekstbekrivelse;
        this.ikkeOppfyltÅrsak = ikkeOppfyltÅrsak;
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

    public String getIkkeOppfyltÅrsakFritekstbekrivelse() {
        return ikkeOppfyltÅrsakFritekstbekrivelse;
    }

    public BostedsvilkårIkkeOppfyltÅrsak getIkkeOppfyltÅrsak() {
        return ikkeOppfyltÅrsak;
    }
}
