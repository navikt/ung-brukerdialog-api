package no.nav.ung.brukerdialog.kontrakt.oppgaver;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EndreOppgaveStatusValidator implements ConstraintValidator<GyldigEndreOppgaveStatus, EndreOppgaveStatusDto> {

    @Override
    public boolean isValid(EndreOppgaveStatusDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.oppgavetype() == null) {
            return true; // @NotNull på oppgavetype håndterer null-tilfellet
        }

        if (dto.oppgavetype().kreverPeriode()) {
            boolean gyldig = dto.fomDato() != null && dto.tomDato() != null;
            if (!gyldig) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "fomDato og tomDato er påkrevd for oppgavetype " + dto.oppgavetype())
                    .addPropertyNode("fomDato")
                    .addConstraintViolation();
            }
            return gyldig;
        }

        return true;
    }
}

