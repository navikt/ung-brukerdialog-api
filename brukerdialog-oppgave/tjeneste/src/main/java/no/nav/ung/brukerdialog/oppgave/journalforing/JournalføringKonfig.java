package no.nav.ung.brukerdialog.oppgave.journalforing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Styrer om {@code JournalførOppgaveTask} opprettes for en gitt oppgave - IKKE om
 * {@code OppgaveJournalføringEntitet}-raden lagres, som skjer uavhengig av denne konfigen.
 */
@ApplicationScoped
public class JournalføringKonfig {

    private boolean journalføringEnabled;
    private Set<OppgaveType> deaktiverteOppgavetyper;

    JournalføringKonfig() {
        // CDI proxy
    }

    @Inject
    public JournalføringKonfig(
        @KonfigVerdi(value = "JOURNALFORING_ENABLED", defaultVerdi = "false")
        boolean journalføringEnabled,
        @KonfigVerdi(value = "JOURNALFORING_DEAKTIVERTE_OPPGAVETYPER", required = false)
        String deaktiverteOppgavetyperRaw
    ) {
        this.journalføringEnabled = journalføringEnabled;
        this.deaktiverteOppgavetyper = parseDeaktiverteOppgavetyper(deaktiverteOppgavetyperRaw);
    }

    /**
     * Defaulten er bevisst {@code false} - et manglende/feilstavet {@code JOURNALFORING_ENABLED}
     * skal aldri slå journalføring PÅ i produksjon.
     */
    public boolean erAktivertFor(OppgaveType oppgaveType) {
        return journalføringEnabled && !deaktiverteOppgavetyper.contains(oppgaveType);
    }

    /**
     * En ukjent verdi feiler ved oppstart i stedet for å bli stille ignorert - en skrivefeil i
     * deny-lista skal ikke kunne late som om en oppgavetype er deaktivert når den egentlig ikke er.
     */
    private static Set<OppgaveType> parseDeaktiverteOppgavetyper(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(JournalføringKonfig::tilOppgaveType)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(OppgaveType.class)));
    }

    private static OppgaveType tilOppgaveType(String navn) {
        try {
            return OppgaveType.valueOf(navn);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Ukjent oppgavetype i JOURNALFORING_DEAKTIVERTE_OPPGAVETYPER: '%s'. Gyldige verdier: %s"
                    .formatted(navn, Arrays.toString(OppgaveType.values())), e);
        }
    }
}
