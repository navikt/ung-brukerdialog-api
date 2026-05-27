package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.opphorvedmaksdato;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.time.LocalDate;

/**
 * Databasestruktur for oppgavedata av type BEKREFT_OPPHOR_VED_MAKSDATO.
 * Lagrer sluttdato og max-dato for opphøret.
 */
@Entity(name = "BekreftOpphorVedMaksdatoOppgaveData")
@Table(name = "BD_OPPGAVE_DATA_BEKREFT_OPPHOR_VED_MAKSDATO")
@Access(AccessType.FIELD)
@OppgaveTypeRef(OppgaveType.BEKREFT_OPPHOR_VED_MAKSDATO)
public class BekreftOpphorVedMaksdatoOppgaveDataEntitet extends OppgaveDataEntitet {

    @Column(name = "sluttdato", nullable = false, updatable = false)
    private LocalDate sluttdato;

    @Column(name = "max_dato", nullable = false, updatable = false)
    private LocalDate maxDato;

    protected BekreftOpphorVedMaksdatoOppgaveDataEntitet() {
        // For JPA
    }

    public BekreftOpphorVedMaksdatoOppgaveDataEntitet(LocalDate sluttdato, LocalDate maxDato) {
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
