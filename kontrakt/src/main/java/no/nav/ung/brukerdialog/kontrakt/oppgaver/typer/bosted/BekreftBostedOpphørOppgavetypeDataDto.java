package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;

import java.time.LocalDate;

/**
 * Data for oppgave om bostedavklaring – bruker bekrefter om de ikke lenger er bosatt i Trondheim.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BekreftBostedOpphørOppgavetypeDataDto(
    @NotNull
    LocalDate fom,

    @NotNull
    Boolean erBosattITrondheim,

    String ikkeOppfyltÅrsakFritekstbeskrivelse,

    @NotNull
    BostedsvilkårIkkeOppfyltÅrsak ikkeOppfyltÅrsak,

    @NotNull
    BostedsavklaringKildeType kilde,

    @Size(max = 1000)
    String kildeFritekst

) implements OppgavetypeDataDto {
    @Override
    public OppgaveType oppgavetype() {
        return OppgaveType.BEKREFT_BOSTED;
    }
}
