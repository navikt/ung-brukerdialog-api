package no.nav.ung.brukerdialog.kontrakt.oppgaver.journalforing;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OpprettOppgaveDto;

import java.util.EnumSet;
import java.util.Set;

/**
 * Validerer at {@code journalføring.fagsakId} er satt når
 * {@code oppgavetypeData.oppgavetype()} <b>ikke</b> er i {@link #UTEN_FAGSAK}:
 * <ul>
 *   <li>{@code dto}, {@code oppgavetypeData} eller {@code oppgavetype()} er {@code null} →
 *       gyldig (håndteres av {@code @NotNull} på {@code oppgavetypeData} andre steder).</li>
 *   <li>Oppgavetype i {@link #UTEN_FAGSAK} → gyldig uansett (fagsak er valgfri, ikke forbudt -
 *       {@code SØK_YTELSE} <i>med</i> fagsakId er fortsatt tillatt).</li>
 *   <li>Andre oppgavetyper uten {@code fagsakId} → ugyldig. Feilmeldingen navngir både
 *       oppgavetypen og hvilke typer som er unntatt, og constraint violation peker på
 *       {@code journalføring.fagsakId} via
 *       {@code addPropertyNode("journalføring").addPropertyNode("fagsakId")}.</li>
 * </ul>
 */
public class GyldigJournalføringValidator implements ConstraintValidator<GyldigJournalføring, OpprettOppgaveDto> {

    /**
     * Oppgavetyper som ikke har fagsak ved opprettelse og derfor kan journalføres på generell
     * sak. Ligger her (ikke på {@link OppgaveType}) fordi det er en journalføringsregel, ikke en
     * egenskap ved oppgavetypen generelt. En ny {@link OppgaveType} som glemmes her arver
     * «krever fagsak», som gir en høylytt 400 - et trygt default.
     */
    static final Set<OppgaveType> UTEN_FAGSAK = EnumSet.of(OppgaveType.SØK_YTELSE);

    private static final String FEILMELDING = "fagsakId er påkrevd for oppgavetype %s. Kun SØK_YTELSE kan journalføres uten fagsak.";

    @Override
    public boolean isValid(OpprettOppgaveDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.oppgavetypeData() == null || dto.oppgavetypeData().oppgavetype() == null) {
            return true;
        }

        OppgaveType oppgavetype = dto.oppgavetypeData().oppgavetype();
        if (UTEN_FAGSAK.contains(oppgavetype)) {
            return true;
        }

        if (dto.journalføring() != null && dto.journalføring().fagsakId() != null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(FEILMELDING.formatted(oppgavetype))
            .addPropertyNode("journalføring").addPropertyNode("fagsakId")
            .addConstraintViolation();
        return false;
    }
}
