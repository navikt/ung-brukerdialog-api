package no.nav.ung.brukerdialog.web.server.batch.bigquery;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryRecord;
import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryTabellDefinisjon;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class BekreftAvvikOppgaveTabellDefinisjon implements BigQueryTabellDefinisjon {

    public static final BekreftAvvikOppgaveTabellDefinisjon INSTANCE = new BekreftAvvikOppgaveTabellDefinisjon();
    public static final String TABELL_NAVN = "oppgave_bekreft_avvik_2";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private BekreftAvvikOppgaveTabellDefinisjon() {
    }

    @Override
    public String getTabellNavn() {
        return TABELL_NAVN;
    }

    @Override
    public Schema getSchema() {
        return Schema.of(
            Field.of("opprettetTidspunkt", StandardSQLTypeName.DATETIME),
            Field.of("oppgaveReferanse", StandardSQLTypeName.STRING),
            Field.of("oppgaveStatus", StandardSQLTypeName.STRING),
            Field.of("fom", StandardSQLTypeName.DATE),
            Field.of("tom", StandardSQLTypeName.DATE),
            Field.of("gjelderDelerAvPerioden", StandardSQLTypeName.BOOL),
            Field.of("harRegisterInntekt", StandardSQLTypeName.BOOL)
        );
    }

    @Override
    public Function<BigQueryRecord, Map<String, ?>> getRowMapper(Instant now) {
        return record -> {
            var r = (BekreftAvvikRecord) record;
            var row = new HashMap<String, Object>();
            row.put("opprettetTidspunkt", r.opprettetTidspunkt().format(DATE_TIME_FORMATTER));
            row.put("oppgaveReferanse", r.oppgaveReferanse().toString());
            row.put("oppgaveStatus", r.oppgaveStatus());
            row.put("fom", r.fraOgMed().format(DATE_FORMATTER));
            row.put("tom", r.tilOgMed().format(DATE_FORMATTER));
            row.put("gjelderDelerAvPerioden", r.gjelderDelerAvMåned());
            row.put("harRegisterInntekt", r.harRegisterInntekt());
            return row;
        };
    }

    @Override
    public boolean skalTømmeFørSkriv() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof BigQueryTabellDefinisjon other
            && getTabellNavn().equals(other.getTabellNavn()));
    }

    @Override
    public int hashCode() {
        return getTabellNavn().hashCode();
    }
}
