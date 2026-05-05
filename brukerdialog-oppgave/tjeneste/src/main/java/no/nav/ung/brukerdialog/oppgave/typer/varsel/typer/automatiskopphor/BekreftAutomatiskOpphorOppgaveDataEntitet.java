package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.automatiskopphor;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.time.LocalDate;

/**
 * Databasestruktur for oppgavedata av type BEKREFT_AUTOMATISK_OPPHOR.
 * Lagrer sluttdato og max-dato for det automatiske opphøret.
 */
@Entity(name = "BekreftAutomatiskOpphorOppgaveData")
@Table(name = "BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR")
@Access(AccessType.FIELD)
@OppgaveTypeRef(OppgaveType.BEKREFT_AUTOMATISK_OPPHOR)
public class BekreftAutomatiskOpphorOppgaveDataEntitet extends OppgaveDataEntitet {

    @Column(name = "sluttdato", nullable = false, updatable = false)
    private LocalDate sluttdato;

    @Column(name = "max_dato", nullable = false, updatable = false)
    private LocalDate maxDato;

    protected BekreftAutomatiskOpphorOppgaveDataEntitet() {
        // For JPA
    }

    public BekreftAutomatiskOpphorOppgaveDataEntitet(LocalDate sluttdato, LocalDate maxDato) {
        this.sluttdato = sluttdato;
        this.maxDato = maxDato;
    }

    public LocalDate getSluttdato() {
        return sluttdato;
    }

    public LocalDate getMaxDato() {
        return maxDato;
    }
}
