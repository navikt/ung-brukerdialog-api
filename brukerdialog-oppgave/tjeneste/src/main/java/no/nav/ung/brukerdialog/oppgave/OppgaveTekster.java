package no.nav.ung.brukerdialog.oppgave;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.pdf.OppgaveTekstfragmentRenderer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OppgaveTekster {

    private OppgaveTekster() {
    }

    public static final String VARSEL_OM_NYE_OPPLYSNINGER_TITTEL = "Varsel om nye opplysninger";

    private static final int MAKS_LENGDE_VARSELTEKST_OPPGAVE = 500;

    public static void validerVarselTekstLengde(String varselTekst, OppgaveType oppgaveType) {
        if (varselTekst.length() > MAKS_LENGDE_VARSELTEKST_OPPGAVE) {
            throw new IllegalStateException(
                "Varseltekst for min-side-oppgave er %d tegn, maks er %d (oppgaveType=%s). Korriger teksten i OppgaveInnholdUtleder for denne typen."
                    .formatted(varselTekst.length(), MAKS_LENGDE_VARSELTEKST_OPPGAVE, oppgaveType));
        }
    }

    public static String ytelseTitelcase(OppgaveYtelsetype ytelsetype) {
        return switch (ytelsetype) {
            case UNGDOMSYTELSE -> "Ungdomsprogramytelsen";
            case AKTIVITETSPENGER -> "Aktivitetspenger";
        };
    }

    public static String fristDato(LocalDateTime fristTid) {
        return fristTid != null ? fristTid.toLocalDate().toString() : null;
    }

    public static String ytelsePreposisjonsfrase(OppgaveYtelsetype ytelsetype) {
        return switch (ytelsetype) {
            case UNGDOMSYTELSE -> "i ungdomsprogrammet";
            case AKTIVITETSPENGER -> "for aktivitetspenger";
        };
    }

    public static String ytelseNavn(OppgaveYtelsetype ytelsetype) {
        return switch (ytelsetype) {
            case UNGDOMSYTELSE -> "ungdomsprogramytelsen";
            case AKTIVITETSPENGER -> "aktivitetspenger";
        };
    }

    public static boolean erMeldtUt(LocalDate forrigeSluttdato) {
        return forrigeSluttdato == null;
    }

    public static String endretStartdatoTittel() {
        return "Endret startdato";
    }

    public static OppgaveTekstfragmentRenderer.Resultat endretStartdatoInnhold(OppgaveTekstfragmentRenderer renderer,
                                                             LocalDate nyStartdato,
                                                             OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ytelsePreposisjonsfrase", ytelsePreposisjonsfrase(ytelsetype));
        data.put("nyStartdato", nyStartdato.toString());
        data.put("fristDato", fristDato(fristTid));
        return renderer.rendre("tekstfragmenter/periode/endret_startdato", data);
    }

    public static String endretSluttdatoTittel(boolean erMeldtUt) {
        return erMeldtUt ? "Sluttdato" : "Endret sluttdato";
    }

    public static OppgaveTekstfragmentRenderer.Resultat endretSluttdatoInnhold(OppgaveTekstfragmentRenderer renderer,
                                                             LocalDate nySluttdato, LocalDate forrigeSluttdato,
                                                             OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("erMeldtUt", erMeldtUt(forrigeSluttdato));
        data.put("ytelsePreposisjonsfrase", ytelsePreposisjonsfrase(ytelsetype));
        data.put("nySluttdato", nySluttdato.toString());
        data.put("fristDato", fristDato(fristTid));
        return renderer.rendre("tekstfragmenter/periode/endret_sluttdato", data);
    }

    public static String fjernetPeriodeTittel() {
        return "Stans";
    }

    public static OppgaveTekstfragmentRenderer.Resultat fjernetPeriodeInnhold(OppgaveTekstfragmentRenderer renderer,
                                                            OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ytelsetype", ytelsetype.name());
        data.put("fristDato", fristDato(fristTid));
        return renderer.rendre("tekstfragmenter/periode/fjernet_periode", data);
    }

    public static String endretStartOgSluttdatoTittel() {
        return "Ny start- og sluttdato";
    }

    public static OppgaveTekstfragmentRenderer.Resultat endretStartOgSluttdatoInnhold(OppgaveTekstfragmentRenderer renderer,
                                                                    LocalDate nyFom, LocalDate nyTom,
                                                                    OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ytelsePreposisjonsfrase", ytelsePreposisjonsfrase(ytelsetype));
        data.put("ytelseNavn", ytelseNavn(ytelsetype));
        data.put("nyFom", nyFom.toString());
        data.put("nyTom", nyTom.toString());
        data.put("fristDato", fristDato(fristTid));
        return renderer.rendre("tekstfragmenter/periode/endret_start_og_sluttdato", data);
    }

    public static String ukjentPeriodeendringTittel() {
        return "Endring i perioden";
    }

    public static OppgaveTekstfragmentRenderer.Resultat ukjentPeriodeendringInnhold(OppgaveTekstfragmentRenderer renderer,
                                                                  LocalDate nyFom, LocalDate nyTom,
                                                                  OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ytelsePreposisjonsfrase", ytelsePreposisjonsfrase(ytelsetype));
        data.put("nyFom", nyFom != null ? nyFom.toString() : null);
        data.put("nyTom", nyTom != null ? nyTom.toString() : null);
        data.put("fristDato", fristDato(fristTid));
        return renderer.rendre("tekstfragmenter/periode/ukjent_periodeendring", data);
    }
}
