package no.nav.ung.brukerdialog.oppgave;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering.InntektsrapporteringOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.oppgave.søkytelse.SøkYtelseOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.bosted.BekreftBostedOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.endretperiode.EndretPeriodeOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.endretsluttdato.EndretSluttdatoOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.endretstartdato.EndretStartdatoOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.opphorvedmaksdato.BekreftOpphørVedMaksdatoOppgaveInnholdUtleder;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class OppgaveInnholdUtlederDekningTest {

    private static final List<Class<? extends OppgaveInnholdUtleder>> IMPLEMENTASJONER = List.of(
        BekreftBostedOppgaveInnholdUtleder.class,
        BekreftOpphørVedMaksdatoOppgaveInnholdUtleder.class,
        EndretPeriodeOppgaveInnholdUtleder.class,
        EndretSluttdatoOppgaveInnholdUtleder.class,
        EndretStartdatoOppgaveInnholdUtleder.class,
        InntektsrapporteringOppgaveInnholdUtleder.class,
        KontrollerRegisterinntektOppgaveInnholdUtleder.class,
        SøkYtelseOppgaveInnholdUtleder.class
    );

    @Test
    void hver_oppgavetype_har_nøyaktig_én_innholdsutleder() {
        var oppgavetyperMedUtleder = IMPLEMENTASJONER.stream()
            .map(cls -> {
                var ref = cls.getAnnotation(OppgaveTypeRef.class);
                assertThat(ref).as(cls.getSimpleName() + " mangler @OppgaveTypeRef").isNotNull();
                return ref.value();
            })
            .collect(Collectors.toList());

        assertThat(oppgavetyperMedUtleder)
            .as("hver OppgaveType skal ha nøyaktig én innholdsutleder - duplikater tyder på copy-paste-feil")
            .doesNotHaveDuplicates();

        assertThat(oppgavetyperMedUtleder)
            .as("alle OppgaveType-verdier skal ha en innholdsutleder - se OppgaveInnholdUtleder")
            .containsExactlyInAnyOrderElementsOf(EnumSet.allOf(OppgaveType.class));
    }

    @Test
    void alle_implementasjoner_er_application_scoped() {
        // OppgaveTypeRef.Lookup.getInstance forbyr @Dependent scope ved Instance-oppslag.
        for (var cls : IMPLEMENTASJONER) {
            assertThat(cls.isAnnotationPresent(ApplicationScoped.class))
                .as(cls.getSimpleName() + " må være @ApplicationScoped, ikke @Dependent")
                .isTrue();
        }
    }
}
