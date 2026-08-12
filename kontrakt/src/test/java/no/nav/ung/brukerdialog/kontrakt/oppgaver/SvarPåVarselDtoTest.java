package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SvarPåVarselDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void gyldig_med_uttalelse_gir_ingen_violations() {
        var dto = new SvarPåVarselDto(true, "Jeg er enig i dette vedtaket.");
        Set<ConstraintViolation<SvarPåVarselDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void gyldig_uten_uttalelse_gir_ingen_violations() {
        var dto = new SvarPåVarselDto(false, null);
        Set<ConstraintViolation<SvarPåVarselDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void harUttalelse_null_er_ugyldig() {
        var dto = new SvarPåVarselDto(null, "En uttalelse");
        Set<ConstraintViolation<SvarPåVarselDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
            .isEqualTo("harUttalelse");
    }

    @Test
    void uttalelseFraBruker_over_4000_tegn_er_ugyldig() {
        String forLangUttalelse = "a".repeat(4001);
        var dto = new SvarPåVarselDto(true, forLangUttalelse);
        Set<ConstraintViolation<SvarPåVarselDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
            .isEqualTo("uttalelseFraBruker");
    }

    @Test
    void uttalelseFraBruker_med_nøyaktig_4000_tegn_er_gyldig() {
        String maksLengdeUttalelse = "a".repeat(4000);
        var dto = new SvarPåVarselDto(true, maksLengdeUttalelse);
        Set<ConstraintViolation<SvarPåVarselDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void uttalelseFraBruker_med_ugyldige_tegn_er_ugyldig() {
        var dto = new SvarPåVarselDto(true, "Дякую за інформацію");
        Set<ConstraintViolation<SvarPåVarselDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Uttalelse fra bruker inneholder ugyldige tegn");
    }

    @Test
    void uttalelseFraBruker_med_æøå_og_tegnsetting_er_gyldig() {
        var dto = new SvarPåVarselDto(true, "Jeg synes vedtaket er feil, se vedlagt dokumentasjon (2025).");
        Set<ConstraintViolation<SvarPåVarselDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }
}
