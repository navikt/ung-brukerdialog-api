package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.inject.Instance;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BrukerdialogOppgaveMapperTest {

    @Test
    void tilDto_setter_varseltekst_fra_innholdUtleder() {
        String varseltekst = "Du har søkt om noe";
        BrukerdialogOppgaveMapper mapper = mapper(
            mapperSomEkkoerVarseltekst(),
            innholdUtlederSomGir(varseltekst));
        BrukerdialogOppgaveEntitet oppgave = oppgave();

        var dto = mapper.tilDto(oppgave);

        assertThat(((SøkYtelseOppgavetypeDataDto) dto.oppgavetypeData()).varseltekst()).isEqualTo(varseltekst);
        assertThat(dto.oppgaveReferanse()).isEqualTo(oppgave.getOppgavereferanse());
        assertThat(dto.oppgavetype()).isEqualTo(oppgave.getOppgaveType());
        assertThat(dto.ytelsetype()).isEqualTo(oppgave.getYtelsetype());
        assertThat(dto.status()).isEqualTo(OppgaveStatus.ULØST);
    }

    @Test
    void tilDto_degraderer_til_null_når_innholdUtleder_kaster() {
        OppgaveInnholdUtleder utleder = mock(OppgaveInnholdUtleder.class);
        when(utleder.varseltekst(any())).thenThrow(new IllegalStateException("simulert feil i tekstutledning"));
        BrukerdialogOppgaveMapper mapper = mapper(
            mapperSomEkkoerVarseltekst(),
            instansMed(utleder));
        BrukerdialogOppgaveEntitet oppgave = oppgave();

        // Skal ikke kaste selv om det underliggende SPI-et gjør det - se
        // BrukerdialogOppgaveMapper#varseltekst sin try/catch-degradering.
        var dto = mapper.tilDto(oppgave);

        assertThat(((SøkYtelseOppgavetypeDataDto) dto.oppgavetypeData()).varseltekst()).isNull();
        // Resten av DTO-en skal fortsatt være korrekt utledet - kun varseltekst degraderes.
        assertThat(dto.oppgaveReferanse()).isEqualTo(oppgave.getOppgavereferanse());
        assertThat(dto.oppgavetypeData()).isNotNull();
    }

    private static BrukerdialogOppgaveMapper mapper(Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
                                                      Instance<OppgaveInnholdUtleder> innholdUtledere) {
        return new BrukerdialogOppgaveMapper(mappere, innholdUtledere);
    }

    private static BrukerdialogOppgaveEntitet oppgave() {
        return new BrukerdialogOppgaveEntitet(UUID.randomUUID(), OppgaveType.SØK_YTELSE, new AktørId("1234567890123"),
            OppgaveYtelsetype.UNGDOMSYTELSE, null);
    }

    // Speiler tilbake varseltekst-argumentet i den returnerte DTO-en, slik at testene kan
    // verifisere at BrukerdialogOppgaveMapper faktisk sender innholdUtlederens varseltekst videre.
    private static Instance<OppgaveDataMapperFraEntitetTilDto> mapperSomEkkoerVarseltekst() {
        OppgaveDataMapperFraEntitetTilDto mapper = mock(OppgaveDataMapperFraEntitetTilDto.class);
        when(mapper.tilDto(any(), any())).thenAnswer(invocation ->
            new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 1, 1), invocation.getArgument(1)));
        return instansMed(mapper);
    }

    private static Instance<OppgaveInnholdUtleder> innholdUtlederSomGir(String varseltekst) {
        OppgaveInnholdUtleder utleder = mock(OppgaveInnholdUtleder.class);
        when(utleder.varseltekst(any())).thenReturn(varseltekst);
        return instansMed(utleder);
    }

    /**
     * Mocker CDI-oppslaget {@code Instance<T>} slik at {@code OppgaveTypeRef.Lookup.find} alltid
     * løser til {@code tjeneste}, uavhengig av hvilken {@code OppgaveType} den blir spurt om -
     * se {@code OppgaveInnholdUtlederInnholdTest} for samme mønster og forklaring på
     * {@code isResolvable()}-stubbingen.
     */
    @SuppressWarnings("unchecked")
    private static <T> Instance<T> instansMed(T tjeneste) {
        Instance<T> instance = mock(Instance.class);
        when(instance.select(any(Annotation.class))).thenReturn(instance);
        when(instance.isResolvable()).thenReturn(true);
        when(instance.get()).thenReturn(tjeneste);
        return instance;
    }
}
