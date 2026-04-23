package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bosted;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
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

    protected BekreftBostedOppgaveDataEntitet() {
        // For JPA
    }

    public BekreftBostedOppgaveDataEntitet(LocalDate fom, LocalDate tom, boolean erBosattITrondheim) {
        this.fom = fom;
        this.tom = tom;
        this.erBosattITrondheim = erBosattITrondheim;
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
}
