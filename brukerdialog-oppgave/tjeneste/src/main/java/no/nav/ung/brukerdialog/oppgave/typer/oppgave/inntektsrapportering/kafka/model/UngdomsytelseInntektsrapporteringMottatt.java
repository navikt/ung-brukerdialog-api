package no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.kafka.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.nav.k9.søknad.ytelse.ung.v1.inntekt.OppgittInntektForPeriode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UngdomsytelseInntektsrapporteringMottatt(
    String oppgaveReferanse,
    OppgittInntektForPeriode oppgittInntektForPeriode
) {
}
