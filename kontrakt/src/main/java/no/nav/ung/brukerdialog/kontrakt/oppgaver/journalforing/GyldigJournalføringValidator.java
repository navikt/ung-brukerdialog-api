package no.nav.ung.brukerdialog.kontrakt.oppgaver.journalforing;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OpprettOppgaveDto;

import java.util.EnumSet;
import java.util.Set;

/** Krever {@code journalføring.fagsakId} for oppgavetyper som ikke er i {@link #UTEN_FAGSAK}. */
public class GyldigJournalføringValidator implements ConstraintValidator<GyldigJournalføring, OpprettOppgaveDto> {

    /**
     * Ligger her, ikke på {@link OppgaveType}, fordi dette er en journalføringsregel. En glemt
     * {@link OppgaveType} arver «krever fagsak» - en trygg 400, ikke en stille feil.
     */
    static final Set<OppgaveType> UTEN_FAGSAK = EnumSet.of(OppgaveType.SØK_YTELSE);

    private static final String FEILMELDING = "fagsakId er påkrevd for oppgavetype %s. Kun SØK_YTELSE kan journalføres uten fagsak.";

    @Override
    public boolean isValid(OpprettOppgaveDto dto, ConstraintValidatorContext context) {
        if (manglerOppgavetype(dto) || erUnntattFraFagsakkrav(dto) || harFagsakId(dto)) {
            return true;
        }
        rapporterManglendeFagsakId(dto, context);
        return false;
    }

    private boolean manglerOppgavetype(OpprettOppgaveDto dto) {
        return dto == null || dto.oppgavetypeData() == null || dto.oppgavetypeData().oppgavetype() == null;
    }

    /** Fagsak er valgfri, ikke forbudt - {@code SØK_YTELSE} med fagsakId er fortsatt tillatt. */
    private boolean erUnntattFraFagsakkrav(OpprettOppgaveDto dto) {
        return UTEN_FAGSAK.contains(dto.oppgavetypeData().oppgavetype());
    }

    private boolean harFagsakId(OpprettOppgaveDto dto) {
        return dto.journalføring() != null && dto.journalføring().fagsakId() != null;
    }

    private void rapporterManglendeFagsakId(OpprettOppgaveDto dto, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(FEILMELDING.formatted(dto.oppgavetypeData().oppgavetype()))
            .addPropertyNode("journalføring").addPropertyNode("fagsakId")
            .addConstraintViolation();
    }
}
