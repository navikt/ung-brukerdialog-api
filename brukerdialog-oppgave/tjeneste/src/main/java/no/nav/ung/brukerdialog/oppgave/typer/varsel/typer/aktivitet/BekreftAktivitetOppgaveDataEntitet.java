package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.aktivitet;

import jakarta.persistence.*;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.AktivitetsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.AktivitetsvilkåretIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.typer.OppgaveDataEntitet;

import java.time.LocalDate;
import java.util.Objects;

@Entity(name = "BekreftAktivitetOppgaveData")
@Table(name = "BD_OPPGAVE_DATA_BEKREFT_AKTIVITET")
@Access(AccessType.FIELD)
@OppgaveTypeRef(OppgaveType.BEKREFT_AKTIVITET)
public class BekreftAktivitetOppgaveDataEntitet extends OppgaveDataEntitet {

    @Column(name = "fom", nullable = false, updatable = false)
    private LocalDate fom;

    /** Null ved opphør – da er perioden åpen. */
    @Column(name = "tom", updatable = false)
    private LocalDate tom;

    @Enumerated(EnumType.STRING)
    @Column(name = "ikke_oppfylt_arsak", nullable = false, updatable = false)
    private AktivitetsvilkåretIkkeOppfyltÅrsak ikkeOppfyltÅrsak;

    @Column(name = "ikke_oppfylt_arsak_fritekstbeskrivelse", updatable = false)
    private String ikkeOppfyltÅrsakFritekstbeskrivelse;

    @Enumerated(EnumType.STRING)
    @Column(name = "kilde", nullable = false, updatable = false)
    private AktivitetsavklaringKildeType kilde;

    @Column(name = "kilde_fritekst", updatable = false)
    private String kildeFritekst;

    protected BekreftAktivitetOppgaveDataEntitet() {
        // For JPA
    }

    public BekreftAktivitetOppgaveDataEntitet(LocalDate fom, LocalDate tom, AktivitetsvilkåretIkkeOppfyltÅrsak ikkeOppfyltÅrsak, String ikkeOppfyltÅrsakFritekstbeskrivelse, AktivitetsavklaringKildeType kilde, String kildeFritekst) {
        this.fom = Objects.requireNonNull(fom, "fom");
        this.tom = tom;
        this.ikkeOppfyltÅrsak = Objects.requireNonNull(ikkeOppfyltÅrsak, "ikkeOppfyltÅrsak");
        this.ikkeOppfyltÅrsakFritekstbeskrivelse = ikkeOppfyltÅrsakFritekstbeskrivelse;
        this.kilde = Objects.requireNonNull(kilde, "kilde");
        if (kilde == AktivitetsavklaringKildeType.ANNET && (kildeFritekst == null || kildeFritekst.isBlank())) {
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

    public AktivitetsvilkåretIkkeOppfyltÅrsak getIkkeOppfyltÅrsak() {
        return ikkeOppfyltÅrsak;
    }

    public String getIkkeOppfyltÅrsakFritekstbeskrivelse() {
        return ikkeOppfyltÅrsakFritekstbeskrivelse;
    }

    public AktivitetsavklaringKildeType getKilde() {
        return kilde;
    }

    public String getKildeFritekst() {
        return kildeFritekst;
    }
}
