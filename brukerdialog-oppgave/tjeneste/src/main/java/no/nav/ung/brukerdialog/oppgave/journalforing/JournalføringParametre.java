package no.nav.ung.brukerdialog.oppgave.journalforing;

import no.nav.k9.felles.integrasjon.pdl.Behandlingsnummer;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;

/**
 * Journalføringsparametre utledet fra oppgavens ytelsetype. {@code behandlingsnummer} brukes
 * kun til PDL-oppslag av navn - ikke til selve dokarkiv-kallet.
 */
public record JournalføringParametre(
    Fagsaksystem fagsaksystem,
    Tema tema,
    Behandlingsnummer behandlingsnummer) {

    public static JournalføringParametre utled(OppgaveYtelsetype ytelsetype) {
        return switch (ytelsetype) {
            case UNGDOMSYTELSE -> new JournalføringParametre(
                Fagsaksystem.UNG_SAK,
                Tema.UNG,
                Behandlingsnummer.UNGDOMSYTELSEN);

            case AKTIVITETSPENGER -> new JournalføringParametre(
                Fagsaksystem.UNG_SAK,
                Tema.UNG, // TODO: Bytt til riktig tema når det er opprettet.
                Behandlingsnummer.AKTIVITETSPENGER);
        };
    }
}
