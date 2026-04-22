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

    @Column(name = "fra_og_med", nullable = false, updatable = false)
    private LocalDate fraOgMed;

    @Column(name = "til_og_med", nullable = false, updatable = false)
    private LocalDate tilOgMed;

    @Column(name = "er_bosatt_i_trondheim", nullable = false, updatable = false)
    private Boolean erBosattITrondheim;

    protected BekreftBostedOppgaveDataEntitet() {
        // For JPA
    }

    public BekreftBostedOppgaveDataEntitet(LocalDate fraOgMed, LocalDate tilOgMed, Boolean erBosattITrondheim) {
        this.fraOgMed = fraOgMed;
        this.tilOgMed = tilOgMed;
        this.erBosattITrondheim = erBosattITrondheim;
    }

    public LocalDate getFraOgMed() {
        return fraOgMed;
    }

    public LocalDate getTilOgMed() {
        return tilOgMed;
    }

    public Boolean getErBosattITrondheim() {
        return erBosattITrondheim;
    }
}
