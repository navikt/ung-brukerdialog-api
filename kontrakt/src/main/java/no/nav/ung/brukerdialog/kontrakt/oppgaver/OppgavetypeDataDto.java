package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BekreftBistandOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bistand.BekreftBistandOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.BekreftAktivitetOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.aktivitet.BekreftAktivitetOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold.BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.opphorvedmaksdato.BekreftOpphorVedMaksdatoOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.EndretPeriodeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretsluttdato.EndretSluttdatoDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretstartdato.EndretStartdatoDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.InntektsrapporteringOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto;

/**
 * Interface for oppgavetype-spesifikk data.
 * Alle oppgavetyper må implementere dette interfacet.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BekreftBostedOppgavetypeDataDto.class, name = "BOSTED"),
    @JsonSubTypes.Type(value = BekreftBostedOpphørOppgavetypeDataDto.class, name = "BOSTED_OPPHØR"),
    @JsonSubTypes.Type(value = BekreftBistandOppgavetypeDataDto.class, name = "BISTAND"),
    @JsonSubTypes.Type(value = BekreftBistandOpphørOppgavetypeDataDto.class, name = "BISTAND_OPPHØR"),
    @JsonSubTypes.Type(value = BekreftAndreLivsoppholdsytelserOppgavetypeDataDto.class, name = "ANDRE_LIVSOPPHOLDSYTELSER"),
    @JsonSubTypes.Type(value = BekreftAndreLivsoppholdsytelserOpphørOppgavetypeDataDto.class, name = "ANDRE_LIVSOPPHOLDSYTELSER_OPPHØR"),
    @JsonSubTypes.Type(value = BekreftAktivitetOppgavetypeDataDto.class, name = "AKTIVITET"),
    @JsonSubTypes.Type(value = BekreftAktivitetOpphørOppgavetypeDataDto.class, name = "AKTIVITET_OPPHØR"),
    @JsonSubTypes.Type(value = EndretStartdatoDataDto.class, name = "ENDRET_STARTDATO"),
    @JsonSubTypes.Type(value = EndretSluttdatoDataDto.class, name = "ENDRET_SLUTTDATO"),
    @JsonSubTypes.Type(value = EndretPeriodeDataDto.class, name = "ENDRET_PERIODE"),
    @JsonSubTypes.Type(value = KontrollerRegisterinntektOppgavetypeDataDto.class, name = "KONTROLLER_REGISTERINNTEKT"),
    @JsonSubTypes.Type(value = InntektsrapporteringOppgavetypeDataDto.class, name = "INNTEKTSRAPPORTERING"),
    @JsonSubTypes.Type(value = SøkYtelseOppgavetypeDataDto.class, name = "SØK_YTELSE"),
    @JsonSubTypes.Type(value = BekreftOpphorVedMaksdatoOppgavetypeDataDto.class, name = "OPPHOR_VED_MAKSDATO")
})
public interface OppgavetypeDataDto {
    /**
     * Returnerer {@link OppgaveType} som tilsvarer denne oppgavetypedataen.
     */
    OppgaveType oppgavetype();
}
