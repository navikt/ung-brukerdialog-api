package no.nav.ung.brukerdialog.oppgave.journalforing;

import no.nav.k9.felles.integrasjon.pdl.Behandlingsnummer;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class JournalføringParametreTest {

    /**
     * Kjører over <b>alle</b> verdier av {@link OppgaveYtelsetype} - fanger opp en ny ytelsetype
     * selv om switch-en i {@code utled(...)} skulle bli utvidet med en {@code default}-gren ved
     * et uhell.
     */
    @ParameterizedTest
    @EnumSource(OppgaveYtelsetype.class)
    void utled_gir_forventet_fagsaksystem_tema_og_behandlingsnummer(OppgaveYtelsetype ytelsetype) {
        JournalføringParametre parametre = JournalføringParametre.utled(ytelsetype);

        assertThat(parametre.fagsaksystem()).isEqualTo(Fagsaksystem.UNG_SAK);
        assertThat(parametre.tema()).isEqualTo(Tema.UNG);
        assertThat(parametre.behandlingsnummer()).isEqualTo(switch (ytelsetype) {
            case UNGDOMSYTELSE -> Behandlingsnummer.UNGDOMSPROGRAMYTELSEN;
            case AKTIVITETSPENGER -> Behandlingsnummer.AKTIVITETSPENGER;
        });
    }
}
