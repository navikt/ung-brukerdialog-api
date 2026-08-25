package no.nav.ung.brukerdialog.oppgave.journalforing;

import no.nav.k9.felles.integrasjon.pdl.Behandlingsnummer;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;

/**
 * Journalføringsparametre utledet fra oppgavens ytelsetype. {@code behandlingsnummer} brukes
 * kun til PDL-oppslag av navn - ikke til selve dokarkiv-kallet.
 * <p>
 * {@code brevkode} følger formatet {@code FVL – forhåndsvarsel – <ytelse>} - ett dokument er et
 * forhåndsvarsel, jf. fvl. § 16, og brevkoden gjøres dermed selvforklarende per ytelse i stedet
 * for én delt kode på tvers av ytelser.
 *
 * @see <a href="https://lovdata.no/lov/1967-02-10/§16">forvaltningsloven § 16 (forhåndsvarsling)</a>
 */
public record JournalføringParametre(
    Fagsaksystem fagsaksystem,
    Tema tema,
    Behandlingsnummer behandlingsnummer,
    String brevkode) {

    /** Felles prefiks - selve forhåndsvarsel-klassifiseringen er lik på tvers av ytelser. */
    private static final String BREVKODE_PREFIX = "FVL – forhåndsvarsel – ";

    public static JournalføringParametre utled(OppgaveYtelsetype ytelsetype) {
        return switch (ytelsetype) {
            case UNGDOMSYTELSE -> new JournalføringParametre(
                Fagsaksystem.UNG_SAK,
                Tema.UNG,
                Behandlingsnummer.UNGDOMSYTELSEN,
                BREVKODE_PREFIX + "ungdomsytelsen");

            case AKTIVITETSPENGER -> new JournalføringParametre(
                Fagsaksystem.UNG_SAK,
                Tema.UNG, // TODO: Bytt til riktig tema når det er opprettet.
                Behandlingsnummer.AKTIVITETSPENGER,
                BREVKODE_PREFIX + "aktivitetspenger");
        };
    }
}
