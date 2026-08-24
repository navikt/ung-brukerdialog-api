package no.nav.ung.brukerdialog.kontrakt.oppgaver.journalforing;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validerer at {@code journalføring.fagsakId} er satt når oppgavetypen
 * ({@code oppgavetypeData.oppgavetype()}) krever fagsak ved journalføring.
 * <p>
 * Se {@link GyldigJournalføringValidator} for hvilke oppgavetyper som er unntatt
 * (journalføres på generell sak i stedet).
 */
@Documented
@Constraint(validatedBy = GyldigJournalføringValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface GyldigJournalføring {

    String message() default "fagsakId er påkrevd for denne oppgavetypen";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
