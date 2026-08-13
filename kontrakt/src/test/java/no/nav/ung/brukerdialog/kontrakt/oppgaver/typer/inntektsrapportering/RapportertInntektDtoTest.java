package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RapportertInntektDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void gyldig_dto_gir_ingen_violations() {
        var dto = new RapportertInntektDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), new BigDecimal("15000"));
        Set<ConstraintViolation<RapportertInntektDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void fraOgMed_null_er_ugyldig() {
        var dto = new RapportertInntektDto(null, LocalDate.of(2025, 1, 31), new BigDecimal("15000"));
        Set<ConstraintViolation<RapportertInntektDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
            .isEqualTo("fraOgMed");
    }

    @Test
    void tilOgMed_null_er_ugyldig() {
        var dto = new RapportertInntektDto(LocalDate.of(2025, 1, 1), null, new BigDecimal("15000"));
        Set<ConstraintViolation<RapportertInntektDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
            .isEqualTo("tilOgMed");
    }

    @Test
    void arbeidstakerOgFrilansInntekt_null_er_ugyldig() {
        var dto = new RapportertInntektDto(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), null);
        Set<ConstraintViolation<RapportertInntektDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
            .isEqualTo("arbeidstakerOgFrilansInntekt");
    }

    @Test
    void negativt_beløp_er_ugyldig() {
        var dto = new RapportertInntektDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), new BigDecimal("-1"));
        Set<ConstraintViolation<RapportertInntektDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
            .isEqualTo("arbeidstakerOgFrilansInntekt");
    }

    @Test
    void beløp_over_999999_er_ugyldig() {
        // Et 7-sifret beløp bryter både @Max (999999) og @Digits(integer=6) samtidig.
        var dto = new RapportertInntektDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), new BigDecimal("1000000"));
        Set<ConstraintViolation<RapportertInntektDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations)
            .allSatisfy(v -> assertThat(v.getPropertyPath().toString())
                .isEqualTo("arbeidstakerOgFrilansInntekt"));
    }

    @Test
    void beløp_med_desimaler_er_ugyldig() {
        var dto = new RapportertInntektDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), new BigDecimal("15000.50"));
        Set<ConstraintViolation<RapportertInntektDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
            .isEqualTo("arbeidstakerOgFrilansInntekt");
    }

    @Test
    void beløp_lik_0_er_gyldig() {
        var dto = new RapportertInntektDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), BigDecimal.ZERO);
        Set<ConstraintViolation<RapportertInntektDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void beløp_lik_999999_er_gyldig() {
        var dto = new RapportertInntektDto(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), new BigDecimal("999999"));
        Set<ConstraintViolation<RapportertInntektDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }
}
