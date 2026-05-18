package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EndreOppgaveStatusValidatorTest {

    private static Validator validator;
    private static final AktørId AKTØR_ID = new AktørId("1234567890123");

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void søkYtelse_uten_datoer_er_gyldig() {
        var dto = new EndreOppgaveStatusDto(AKTØR_ID, OppgaveType.SØK_YTELSE, null, null);
        Set<ConstraintViolation<EndreOppgaveStatusDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void søkYtelse_med_datoer_er_gyldig() {
        var dto = new EndreOppgaveStatusDto(
            AKTØR_ID, OppgaveType.SØK_YTELSE,
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        Set<ConstraintViolation<EndreOppgaveStatusDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void rapporterInntekt_med_datoer_er_gyldig() {
        var dto = new EndreOppgaveStatusDto(
            AKTØR_ID, OppgaveType.RAPPORTER_INNTEKT,
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        Set<ConstraintViolation<EndreOppgaveStatusDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void rapporterInntekt_uten_fomDato_er_ugyldig() {
        var dto = new EndreOppgaveStatusDto(
            AKTØR_ID, OppgaveType.RAPPORTER_INNTEKT,
            null, LocalDate.of(2025, 1, 31));
        Set<ConstraintViolation<EndreOppgaveStatusDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("RAPPORTER_INNTEKT");
    }

    @Test
    void rapporterInntekt_uten_begge_datoer_er_ugyldig() {
        var dto = new EndreOppgaveStatusDto(AKTØR_ID, OppgaveType.RAPPORTER_INNTEKT, null, null);
        Set<ConstraintViolation<EndreOppgaveStatusDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }
}

