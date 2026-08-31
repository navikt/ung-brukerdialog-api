package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted;

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
 * Data for oppgave om bostedavklaring – bruker bekrefter om de er bosatt i Trondheim.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BekreftBostedOppgavetypeDataDto(
    @NotNull
    LocalDate fom,

    @NotNull
    LocalDate tom,

    @NotNull
    Boolean erBosattITrondheim,

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST, message = "ikkeOppfyltÅrsakFritekstbeskrivelse inneholder ugyldige tegn")
    String ikkeOppfyltÅrsakFritekstbeskrivelse,

    @NotNull
    BostedsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak,

    @NotNull
    BostedsavklaringKildeType kilde,

    @Size(max = 1000)
    @Pattern(regexp = InputValideringRegex.FRITEKST, message = "kildeFritekst inneholder ugyldige tegn")
    String kildeFritekst

) implements OppgavetypeDataDto {
    @Override
    public OppgaveType oppgavetype() {
        return OppgaveType.BEKREFT_BOSTED;
    }

    @AssertTrue(message = "kildeFritekst er påkrevd når kilde = ANNET")
    public boolean isKildeFritekstOk() {
        return kilde != BostedsavklaringKildeType.ANNET || (kildeFritekst != null && !kildeFritekst.isBlank());
    }
}
