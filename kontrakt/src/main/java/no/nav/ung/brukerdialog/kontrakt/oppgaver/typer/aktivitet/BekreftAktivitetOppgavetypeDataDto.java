package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.k9.felles.validering.InputValideringRegex;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;

import java.time.LocalDate;

/**
 * Data for oppgave om aktivitetsavklaring – bruker kan uttale seg om at aktivitetsvilkåret ikke er
 * oppfylt i en avgrenset periode. Opphørsvarianten er
 * {@link BekreftAktivitetOpphørOppgavetypeDataDto}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BekreftAktivitetOppgavetypeDataDto(
    @NotNull
    LocalDate fom,

    @NotNull
    LocalDate tom,

    @NotNull
    AktivitetsvilkåretIkkeOppfyltÅrsak ikkeOppfyltÅrsak,

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST, message = "ikkeOppfyltÅrsakFritekstbeskrivelse inneholder ugyldige tegn")
    String ikkeOppfyltÅrsakFritekstbeskrivelse,

    @NotNull
    AktivitetsavklaringKildeType kilde,

    @Size(max = 1000)
    @Pattern(regexp = InputValideringRegex.FRITEKST, message = "kildeFritekst inneholder ugyldige tegn")
    String kildeFritekst,

    // Kun satt av backend på output (varselteksten fra PDF-brevet). Ignoreres på input.
    String varseltekst

) implements OppgavetypeDataDto {

    // Konstruktør uten varseltekst - brukes der varseltekst ikke er kjent/relevant enda.
    public BekreftAktivitetOppgavetypeDataDto(LocalDate fom, LocalDate tom, AktivitetsvilkåretIkkeOppfyltÅrsak ikkeOppfyltÅrsak, String ikkeOppfyltÅrsakFritekstbeskrivelse, AktivitetsavklaringKildeType kilde, String kildeFritekst) {
        this(fom, tom, ikkeOppfyltÅrsak, ikkeOppfyltÅrsakFritekstbeskrivelse, kilde, kildeFritekst, null);
    }

    @Override
    public OppgaveType oppgavetype() {
        return OppgaveType.BEKREFT_AKTIVITET;
    }

    @JsonIgnore
    @AssertTrue(message = "kildeFritekst er påkrevd når kilde = ANNET")
    public boolean isKildeFritekstOk() {
        return kilde != AktivitetsavklaringKildeType.ANNET || (kildeFritekst != null && !kildeFritekst.isBlank());
    }
}
