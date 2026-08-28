package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifiserer at {@code ytelsetype} er et påkrevd felt: feltet styrer nå
 * arkivtema og behandlingsgrunnlag ved journalføring, så en manglende verdi skal gi en høylytt
 * valideringsfeil - ikke en stille default.
 */
class OpprettOppgaveDtoTest {

    private static Validator validator;
    private static final AktørId AKTØR_ID = new AktørId("1234567890123");

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void manglende_ytelsetype_skal_gi_constraintViolation_paa_feltet_ytelsetype() {
        var dto = dto(null);

        Set<ConstraintViolation<OpprettOppgaveDto>> violations = validator.validate(dto);

        assertThat(violations)
            .anySatisfy(violation -> {
                assertThat(violation.getPropertyPath().toString()).isEqualTo("ytelsetype");
                assertThat(violation.getMessage()).isNotBlank();
            });
    }

    @Test
    void satt_ytelsetype_skal_ikke_gi_constraintViolation_paa_feltet_ytelsetype() {
        var dto = dto(OppgaveYtelsetype.UNGDOMSYTELSE);

        Set<ConstraintViolation<OpprettOppgaveDto>> violations = validator.validate(dto);

        assertThat(violations)
            .noneMatch(violation -> violation.getPropertyPath().toString().equals("ytelsetype"));
    }

    private static OpprettOppgaveDto dto(OppgaveYtelsetype ytelsetype) {
        return new OpprettOppgaveDto(
            AKTØR_ID,
            ytelsetype,
            UUID.randomUUID(),
            new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 1, 1)),
            null,
            null
        );
    }
}
