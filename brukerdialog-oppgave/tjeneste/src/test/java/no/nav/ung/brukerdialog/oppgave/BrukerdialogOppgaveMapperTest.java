package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.inject.Instance;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto;
import no.nav.ung.brukerdialog.typer.AktørId;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BrukerdialogOppgaveMapperTest {

    @Test
    void tilDto_setter_tekster_fra_innholdUtleder() {
        List<OppgaveTekst> tekster = List.of(new OppgaveAvsnitt("Du har søkt om noe"));
        BrukerdialogOppgaveMapper mapper = mapper(
            mapperSomGir(new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 1, 1))),
            innholdUtlederSomGir(tekster));
        BrukerdialogOppgaveEntitet oppgave = oppgave();

        var dto = mapper.tilDto(oppgave);

        assertThat(dto.varselInnhold()).isEqualTo(tekster);
        assertThat(dto.oppgaveReferanse()).isEqualTo(oppgave.getOppgavereferanse());
        assertThat(dto.oppgavetype()).isEqualTo(oppgave.getOppgaveType());
        assertThat(dto.ytelsetype()).isEqualTo(oppgave.getYtelsetype());
        assertThat(dto.status()).isEqualTo(OppgaveStatus.ULØST);
        assertThat(dto.oppgavetypeData()).isNotNull();
    }

    @Test
    void tilDto_degraderer_til_tom_liste_når_innholdUtleder_kaster() {
        OppgaveInnholdUtleder utleder = mock(OppgaveInnholdUtleder.class);
        when(utleder.varselInnhold(any())).thenThrow(new IllegalStateException("simulert feil i tekstutledning"));
        BrukerdialogOppgaveMapper mapper = mapper(
            mapperSomGir(new SøkYtelseOppgavetypeDataDto(LocalDate.of(2025, 1, 1))),
            instansMed(utleder));
        BrukerdialogOppgaveEntitet oppgave = oppgave();

        // Skal ikke kaste selv om det underliggende SPI-et gjør det - se
        // BrukerdialogOppgaveMapper#innhold sin try/catch-degradering.
        var dto = mapper.tilDto(oppgave);

        assertThat(dto.varselInnhold()).isEmpty();
        // Resten av DTO-en skal fortsatt være korrekt utledet - kun tekster degraderes.
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

    private static Instance<OppgaveDataMapperFraEntitetTilDto> mapperSomGir(OppgavetypeDataDto dto) {
        OppgaveDataMapperFraEntitetTilDto mapper = mock(OppgaveDataMapperFraEntitetTilDto.class);
        when(mapper.tilDto(any())).thenReturn(dto);
        return instansMed(mapper);
    }

    private static Instance<OppgaveInnholdUtleder> innholdUtlederSomGir(List<OppgaveTekst> tekster) {
        OppgaveInnholdUtleder utleder = mock(OppgaveInnholdUtleder.class);
        when(utleder.varselInnhold(any())).thenReturn(tekster);
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
