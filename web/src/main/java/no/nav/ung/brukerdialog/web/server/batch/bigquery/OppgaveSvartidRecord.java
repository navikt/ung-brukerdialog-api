package no.nav.ung.brukerdialog.web.server.batch.bigquery;

import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryRecord;
import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryTabellDefinisjon;

public record OppgaveSvartidRecord(
    Long svartidAntallDager,
    boolean erLøst,
    boolean erLukket,
    boolean ikkeMottattOgEldreEnn14Dager,
    String oppgaveType,
    int antall
) implements BigQueryRecord {
    @Override
    public BigQueryTabellDefinisjon tabellDefinisjon() {
        return OppgaveSvartidTabellDefinisjon.INSTANCE;
    }
}
