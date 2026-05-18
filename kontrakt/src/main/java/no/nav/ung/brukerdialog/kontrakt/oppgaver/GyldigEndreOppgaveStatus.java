package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validerer at {@code fomDato} og {@code tomDato} er satt dersom oppgavetypen krever periode
 * (se {@link OppgaveType#kreverPeriode()}).
 */
@Documented
@Constraint(validatedBy = EndreOppgaveStatusValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface GyldigEndreOppgaveStatus {

    String message() default "fomDato og tomDato er påkrevd for oppgavetype {oppgavetype}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

