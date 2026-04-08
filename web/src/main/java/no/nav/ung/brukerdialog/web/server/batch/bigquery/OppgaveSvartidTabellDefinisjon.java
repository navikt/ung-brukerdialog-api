package no.nav.ung.brukerdialog.web.server.batch.bigquery;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryRecord;
import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryTabellDefinisjon;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class OppgaveSvartidTabellDefinisjon implements BigQueryTabellDefinisjon {

    public static final OppgaveSvartidTabellDefinisjon INSTANCE = new OppgaveSvartidTabellDefinisjon();
    static final String TABELL_NAVN = "oppgave_svartid";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private OppgaveSvartidTabellDefinisjon() {
    }

    @Override
    public String getTabellNavn() {
        return TABELL_NAVN;
    }

    @Override
    public Schema getSchema() {
        return Schema.of(
            Field.of("antall", StandardSQLTypeName.BIGNUMERIC),
            Field.of("svartidAntallDager", StandardSQLTypeName.NUMERIC),
            Field.of("erLøst", StandardSQLTypeName.BOOL),
            Field.of("erLukket", StandardSQLTypeName.BOOL),
            Field.of("ikkeMottattOgEldreEnn14Dager", StandardSQLTypeName.BOOL),
            Field.of("oppgaveType", StandardSQLTypeName.STRING),
            Field.of("opprettetTidspunkt", StandardSQLTypeName.DATETIME)
        );
    }

    @Override
    public Function<BigQueryRecord, Map<String, ?>> getRowMapper(Instant now) {
        String tidspunkt = ZonedDateTime.ofInstant(now, ZoneId.of("Europe/Oslo")).format(DATE_TIME_FORMATTER);
        return record -> {
            var r = (OppgaveSvartidRecord) record;
            var row = new HashMap<String, Object>();
            row.put("antall", r.antall());
            row.put("svartidAntallDager", r.svartidAntallDager());
            row.put("erLøst", r.erLøst());
            row.put("erLukket", r.erLukket());
            row.put("ikkeMottattOgEldreEnn14Dager", r.ikkeMottattOgEldreEnn14Dager());
            row.put("oppgaveType", r.oppgaveType());
            row.put("opprettetTidspunkt", tidspunkt);
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
