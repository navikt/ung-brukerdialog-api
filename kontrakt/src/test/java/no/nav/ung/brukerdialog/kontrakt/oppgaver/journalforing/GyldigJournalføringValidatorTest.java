package no.nav.ung.brukerdialog.kontrakt.oppgaver.journalforing;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OpprettOppgaveDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Se {@link GyldigJournalføringValidator} for spesifikasjonen som testes her.
 */
class GyldigJournalføringValidatorTest {

    private static Validator validator;
    private static final AktørId AKTØR_ID = new AktørId("1234567890123");

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void oppgavetype_som_krever_fagsak_uten_fagsakId_er_ugyldig() {
        var dto = bekreftBostedDto(null);

        Set<ConstraintViolation<OpprettOppgaveDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        ConstraintViolation<OpprettOppgaveDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath()).hasToString("journalføring.fagsakId");
        assertThat(violation.getMessage())
            .contains(OppgaveType.BEKREFT_BOSTED.name())
            .contains(OppgaveType.SØK_YTELSE.name());
    }

    @Test
    void søkYtelse_uten_fagsakId_er_gyldig() {
        // SØK_YTELSE har ingen fagsak ved opprettelse og skal derfor være gyldig uten
        // journalføring/fagsakId (journalføres på GENERELL_SAK).
        var dto = søkYtelseDto(null);
        Set<ConstraintViolation<OpprettOppgaveDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void søkYtelse_med_fagsakId_er_ogsaa_gyldig() {
        // Fagsak er valgfri for SØK_YTELSE, ikke forbudt - en satt fagsakId gir fortsatt en
        // gyldig dto (journalføres da på FAGSAK i stedet for GENERELL_SAK).
        var dto = søkYtelseDto(new JournalføringDto(new Saksnummer("ABC123")));
        Set<ConstraintViolation<OpprettOppgaveDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void feilmelding_navngir_baade_oppgavetype_og_unntatte_typer() {
        var dto = bekreftBostedDto(null);

        Set<ConstraintViolation<OpprettOppgaveDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("fagsakId er påkrevd for oppgavetype BEKREFT_BOSTED. Kun SØK_YTELSE kan journalføres uten fagsak.");
    }

    private static OpprettOppgaveDto søkYtelseDto(JournalføringDto journalføring) {
        return new OpprettOppgaveDto(
            AKTØR_ID,
            OppgaveYtelsetype.UNGDOMSYTELSE,
            UUID.randomUUID(),
            new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 1, 1)),
            null,
            journalføring
        );
    }

    /**
     * Alle {@code @NotNull}-felter på {@link BekreftBostedOppgavetypeDataDto} er populert med
     * gyldige verdier, slik at den kaskaderende {@code @Valid}-valideringen på
     * {@code oppgavetypeData} ikke gir ekstra violations ved siden av den vi tester her.
     */
    private static OpprettOppgaveDto bekreftBostedDto(JournalføringDto journalføring) {
        return new OpprettOppgaveDto(
            AKTØR_ID,
            OppgaveYtelsetype.UNGDOMSYTELSE,
            UUID.randomUUID(),
            new BekreftBostedOppgavetypeDataDto(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                true,
                null,
                BostedsvilkårIkkeOppfyltÅrsak.ANNET),
            null,
            journalføring
        );
    }
}
