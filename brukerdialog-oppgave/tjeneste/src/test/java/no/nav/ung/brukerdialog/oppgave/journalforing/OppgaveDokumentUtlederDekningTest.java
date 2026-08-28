package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.BekreftBostedOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.BekreftOpphorVedMaksdatoOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.EndretPeriodeOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.EndretSluttdatoOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.EndretStartdatoOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.InntektsrapporteringOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.KontrollerRegisterinntektOppgaveDokumentUtleder;
import no.nav.ung.brukerdialog.oppgave.journalforing.typer.SøkYtelseOppgaveDokumentUtleder;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifiserer at det finnes nøyaktig én {@link OppgaveDokumentUtleder}-implementasjon per
 * {@link OppgaveType} - samme hensikt som den uttømmende switch-en i
 * {@link JournalføringParametre#utled}, men håndhevet ved refleksjon siden koblingen her skjer
 * via CDI-qualifier ({@link OppgaveTypeRef}) og ikke fanges av kompilatoren. Fanger opp en ny
 * {@code OppgaveType} som mangler tilhørende dokumentutleder.
 * <p>
 * Grønn sone - tester kun at stillaset er komplett og riktig koblet, ikke innholdet i
 * implementasjonene (se {@link OppgaveDokumentUtlederInnholdTest} for det).
 */
class OppgaveDokumentUtlederDekningTest {

    private static final List<Class<? extends OppgaveDokumentUtleder>> IMPLEMENTASJONER = List.of(
        BekreftBostedOppgaveDokumentUtleder.class,
        BekreftOpphorVedMaksdatoOppgaveDokumentUtleder.class,
        EndretPeriodeOppgaveDokumentUtleder.class,
        EndretSluttdatoOppgaveDokumentUtleder.class,
        EndretStartdatoOppgaveDokumentUtleder.class,
        InntektsrapporteringOppgaveDokumentUtleder.class,
        KontrollerRegisterinntektOppgaveDokumentUtleder.class,
        SøkYtelseOppgaveDokumentUtleder.class
    );

    @Test
    void hver_oppgavetype_har_nøyaktig_én_dokumentutleder() {
        var oppgavetyperMedUtleder = IMPLEMENTASJONER.stream()
            .map(cls -> {
                var ref = cls.getAnnotation(OppgaveTypeRef.class);
                assertThat(ref).as(cls.getSimpleName() + " mangler @OppgaveTypeRef").isNotNull();
                return ref.value();
            })
            .collect(Collectors.toList());

        assertThat(oppgavetyperMedUtleder)
            .as("hver OppgaveType skal ha nøyaktig én dokumentutleder - duplikater tyder på copy-paste-feil")
            .doesNotHaveDuplicates();

        assertThat(oppgavetyperMedUtleder)
            .as("alle OppgaveType-verdier skal ha en dokumentutleder - se OppgaveDokumentUtleder")
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
