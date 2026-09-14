package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold;

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
 * Data for oppgave om andre livsoppholdsytelser – bruker kan uttale seg om at vilkåret ikke lenger
 * er oppfylt fra og med en dato. Skiller seg fra
 * {@link BekreftAndreLivsoppholdsytelserOppgavetypeDataDto} kun ved at perioden er åpen (ingen tom).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto(
    @NotNull
    LocalDate fom,

    @NotNull
    AndreLivsoppholdsytelserIkkeOppfyltÅrsak ikkeOppfyltÅrsak,

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST, message = "ikkeOppfyltÅrsakFritekstbeskrivelse inneholder ugyldige tegn")
    String ikkeOppfyltÅrsakFritekstbeskrivelse,

    @NotNull
    AndreLivsoppholdsytelserAvklaringKildeType kilde,

    @Size(max = 1000)
    @Pattern(regexp = InputValideringRegex.FRITEKST, message = "kildeFritekst inneholder ugyldige tegn")
    String kildeFritekst

) implements OppgavetypeDataDto {
    @Override
    public OppgaveType oppgavetype() {
        return OppgaveType.BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER;
    }

    @JsonIgnore
    @AssertTrue(message = "kildeFritekst er påkrevd når kilde = ANNET")
    public boolean isKildeFritekstOk() {
        return kilde != AndreLivsoppholdsytelserAvklaringKildeType.ANNET || (kildeFritekst != null && !kildeFritekst.isBlank());
    }
}
