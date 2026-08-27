package no.nav.ung.brukerdialog.kontrakt.oppgaver.journalforing;

import jakarta.validation.ConstraintValidatorContext;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OpprettOppgaveDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BekreftBostedOppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tester {@link GyldigJournalføringValidator} direkte (ikke via bean-validation-refleksjon på
 * {@code OpprettOppgaveDto}) - {@code @GyldigJournalføring} er midlertidig fjernet fra DTO-en
 * (se klassens javadoc og "Kjente begrensninger" i JOURNALFORING.md), men selve validatoren
 * beholdes og testes uendret for enkel gjeninnføring senere.
 */
@ExtendWith(MockitoExtension.class)
class GyldigJournalføringValidatorTest {

    private final GyldigJournalføringValidator validator = new GyldigJournalføringValidator();
    private static final AktørId AKTØR_ID = new AktørId("1234567890123");

    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @Test
    void oppgavetype_som_krever_fagsak_uten_saksnummer_er_ugyldig() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode("journalføring")).thenReturn(nodeBuilder);
        when(nodeBuilder.addPropertyNode("saksnummer")).thenReturn(nodeBuilder);

        boolean gyldig = validator.isValid(bekreftBostedDto(null), context);

        assertThat(gyldig).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(nodeBuilder).addConstraintViolation();
    }

    @Test
    void søkYtelse_uten_saksnummer_er_gyldig() {
        // SØK_YTELSE har ingen fagsak ved opprettelse og skal derfor være gyldig uten
        // journalføring/saksnummer (journalføres på GENERELL_SAK).
        boolean gyldig = validator.isValid(søkYtelseDto(null), context);
        assertThat(gyldig).isTrue();
        verifyNoInteractions(context);
    }

    @Test
    void søkYtelse_med_saksnummer_er_ogsaa_gyldig() {
        // Fagsak er valgfri for SØK_YTELSE, ikke forbudt - et satt saksnummer gir fortsatt en
        // gyldig dto (journalføres da på FAGSAK i stedet for GENERELL_SAK).
        boolean gyldig = validator.isValid(søkYtelseDto(new JournalføringDto(new Saksnummer("ABC123"))), context);
        assertThat(gyldig).isTrue();
        verifyNoInteractions(context);
    }

    @Test
    void feilmelding_navngir_baade_oppgavetype_og_unntatte_typer() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode("journalføring")).thenReturn(nodeBuilder);
        when(nodeBuilder.addPropertyNode("saksnummer")).thenReturn(nodeBuilder);

        validator.isValid(bekreftBostedDto(null), context);

        ArgumentCaptor<String> meldingCaptor = ArgumentCaptor.forClass(String.class);
        verify(context).buildConstraintViolationWithTemplate(meldingCaptor.capture());
        assertThat(meldingCaptor.getValue())
            .isEqualTo("saksnummer er påkrevd for oppgavetype BEKREFT_BOSTED. Kun SØK_YTELSE kan journalføres uten fagsak.");
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
