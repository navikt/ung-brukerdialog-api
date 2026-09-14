package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand;

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
 * Data for oppgave om bistandsavklaring – bruker kan uttale seg om at bistandsvilkåret ikke
 * lenger er oppfylt fra og med en dato. Skiller seg fra
 * {@link BekreftBistandOppgavetypeDataDto} kun ved at perioden er åpen (ingen tom).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BekreftBistandOpphørOppgavetypeDataDto(
    @NotNull
    LocalDate fom,

    @NotNull
    BistandsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak,

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST, message = "ikkeOppfyltÅrsakFritekstbeskrivelse inneholder ugyldige tegn")
    String ikkeOppfyltÅrsakFritekstbeskrivelse,

    @NotNull
    BistandsavklaringKildeType kilde,

    @Size(max = 1000)
    @Pattern(regexp = InputValideringRegex.FRITEKST, message = "kildeFritekst inneholder ugyldige tegn")
    String kildeFritekst

) implements OppgavetypeDataDto {
    @Override
    public OppgaveType oppgavetype() {
        return OppgaveType.BEKREFT_BISTAND;
    }

    @JsonIgnore
    @AssertTrue(message = "kildeFritekst er påkrevd når kilde = ANNET")
    public boolean isKildeFritekstOk() {
        return kilde != BistandsavklaringKildeType.ANNET || (kildeFritekst != null && !kildeFritekst.isBlank());
    }
}
