package no.nav.ung.brukerdialog.oppgave.journalforing;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Delt brevtekst-logikk mellom {@code typer}-klassene. To formål:
 * <ol>
 *   <li><b>Ytelseskvalifikator</b> ({@link #ytelsePreposisjonsfrase}/{@link #ytelseNavn}) - gjør
 *   ytelsen eksplisitt der oppgavetypen ellers ikke sier det. {@code OppgaveYtelsetype} er et
 *   fritt felt, uavhengig av {@code OppgaveType} - ikke en kontraktgaranti, selv der kun én
 *   ytelse brukes i praksis i dag.</li>
 *   <li><b>Delt «endret startdato/sluttdato»-tekst</b> - {@code BEKREFT_ENDRET_PERIODE} kan vise
 *   samme narrativ som de dedikerte typene. Samme metode + samme Handlebars-partial i begge
 *   maler hindrer at teksten drifter i to retninger.</li>
 * </ol>
 */
public final class OppgaveDokumentTekster {

    private OppgaveDokumentTekster() {
    }

    /** Suffiks til titler uten et innbakt ytelsesnavn, f.eks. «... endret startdato i ungdomsprogrammet». */
    public static String ytelsePreposisjonsfrase(OppgaveYtelsetype ytelsetype) {
        return switch (ytelsetype) {
            case UNGDOMSYTELSE -> "i ungdomsprogrammet";
            case AKTIVITETSPENGER -> "for aktivitetspenger";
        };
    }

    /** Substantiv til bruk der teksten allerede bygger rundt et ytelsesnavn, f.eks. «stans av {{ytelseNavn}}». */
    public static String ytelseNavn(OppgaveYtelsetype ytelsetype) {
        return switch (ytelsetype) {
            case UNGDOMSYTELSE -> "ungdomsprogramytelsen";
            case AKTIVITETSPENGER -> "aktivitetspenger";
        };
    }

    /** Sant når dette er første gang en sluttdato settes (bruker meldes ut), ikke en endring av en eksisterende. */
    public static boolean erMeldtUt(LocalDate forrigeSluttdato) {
        return forrigeSluttdato == null;
    }

    public static String endretStartdatoTittel(OppgaveYtelsetype ytelsetype) {
        return "Tilbakemelding på endret startdato " + ytelsePreposisjonsfrase(ytelsetype);
    }

    /** Delt av {@code EndretStartdatoOppgaveDokumentUtleder} og {@code EndretPeriodeOppgaveDokumentUtleder} (gren {@code ENDRET_STARTDATO}). */
    public static Map<String, Object> endretStartdatoInnhold(LocalDate nyStartdato, LocalDate forrigeStartdato,
                                                              OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("nyStartdato", nyStartdato.toString());
        data.put("forrigeStartdato", forrigeStartdato.toString());
        data.put("ytelsePreposisjonsfrase", ytelsePreposisjonsfrase(ytelsetype));
        leggTilSvarfrist(data, fristTid);
        return data;
    }

    public static String endretSluttdatoTittel(OppgaveYtelsetype ytelsetype, boolean erMeldtUt) {
        String base = erMeldtUt ? "Tilbakemelding på sluttdato" : "Tilbakemelding på endret sluttdato";
        return base + " " + ytelsePreposisjonsfrase(ytelsetype);
    }

    /** Delt av {@code EndretSluttdatoOppgaveDokumentUtleder} og {@code EndretPeriodeOppgaveDokumentUtleder} (gren {@code ENDRET_SLUTTDATO}). */
    public static Map<String, Object> endretSluttdatoInnhold(LocalDate nySluttdato, LocalDate forrigeSluttdato,
                                                              OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("nySluttdato", nySluttdato.toString());
        if (forrigeSluttdato != null) {
            data.put("forrigeSluttdato", forrigeSluttdato.toString());
        }
        data.put("erMeldtUt", erMeldtUt(forrigeSluttdato));
        data.put("ytelsePreposisjonsfrase", ytelsePreposisjonsfrase(ytelsetype));
        leggTilSvarfrist(data, fristTid);
        return data;
    }

    public static String fjernetPeriodeTittel(OppgaveYtelsetype ytelsetype) {
        return "Tilbakemelding på stans av " + ytelseNavn(ytelsetype);
    }

    /**
     * Kilde: {@code oppgavepaneler/fjernet-periode/i18n/nb.ts}.
     * <p>
     * <b>Bevisst forenkling:</b> «aktivitetspenger din» er ugrammatisk (possessiv-bøying passer
     * ikke et pluralisert substantiv), så hver ytelsetype får en fullstendig, hardkodet setning
     * her i stedet for frase/substantiv-parametrisering som de andre narrativene bruker.
     */
    public static Map<String, Object> fjernetPeriodeInnhold(OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("meldtUtSetning", switch (ytelsetype) {
            case UNGDOMSYTELSE -> "Veilederen din har meldt deg ut av ungdomsprogrammet fordi du ikke skal delta i programmet likevel.";
            case AKTIVITETSPENGER -> "Veilederen din har meldt deg ut fordi du ikke skal delta likevel.";
        });
        data.put("stansSetning", switch (ytelsetype) {
            case UNGDOMSYTELSE -> "Du kan bare få ungdomsprogramytelsen hvis du deltar i programmet, og derfor stopper vi den.";
            case AKTIVITETSPENGER -> "Du kan bare få aktivitetspenger hvis du deltar, og derfor stopper vi utbetalingen.";
        });
        leggTilSvarfrist(data, fristTid);
        return data;
    }

    public static String endretStartOgSluttdatoTittel(OppgaveYtelsetype ytelsetype) {
        return "Tilbakemelding på ny start- og sluttdato for " + ytelseNavn(ytelsetype);
    }

    /**
     * Kilde: {@code oppgavepaneler/endret-start-og-sluttdato/i18n/nb.ts}. I motsetning til
     * {@link #fjernetPeriodeInnhold} generaliserer denne teksten grammatisk fint til
     * aktivitetspenger, så vanlig frase/substantiv-parametrisering brukes her.
     */
    public static Map<String, Object> endretStartOgSluttdatoInnhold(LocalDate nyFom, LocalDate nyTom,
                                                                     OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("nyPeriodeFom", nyFom.toString());
        data.put("nyPeriodeTom", nyTom.toString());
        data.put("ytelsePreposisjonsfrase", ytelsePreposisjonsfrase(ytelsetype));
        data.put("ytelseNavn", ytelseNavn(ytelsetype));
        leggTilSvarfrist(data, fristTid);
        return data;
    }

    public static String ukjentPeriodeendringTittel(OppgaveYtelsetype ytelsetype) {
        return "Tilbakemelding på endring i perioden " + ytelsePreposisjonsfrase(ytelsetype);
    }

    /**
     * Fallback for kombinasjoner uten dedikert tekst (bl.a. {@code ANDRE_ENDRINGER}) eller
     * manglende datoer. Frontend kaster her ({@code parseOppgaverElement.ts}) - journalføring MÅ
     * likevel produsere et gyldig, arkiverbart dokument.
     */
    public static Map<String, Object> ukjentPeriodeendringInnhold(LocalDate nyFom, LocalDate nyTom,
                                                                   OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (nyFom != null) {
            data.put("nyPeriodeFom", nyFom.toString());
        }
        if (nyTom != null) {
            data.put("nyPeriodeTom", nyTom.toString());
        }
        data.put("ytelsePreposisjonsfrase", ytelsePreposisjonsfrase(ytelsetype));
        leggTilSvarfrist(data, fristTid);
        return data;
    }

    public static void leggTilSvarfrist(Map<String, Object> data, LocalDateTime fristTid) {
        if (fristTid != null) {
            data.put("svarfrist", fristTid.toLocalDate().toString());
        }
    }

    /**
     * Nye visningstekster uten frontend-forelegg ({@code BostedVilkarOppgavePanelOppgavetekst.tsx}
     * viser ikke årsaken i dag). {@code null} for {@code UDEFINERT} (vilkåret er oppfylt) - skal
     * da ikke vises.
     */
    public static String bostedIkkeOppfyltForklaring(BostedsvilkårIkkeOppfyltÅrsak årsak, String fritekstbeskrivelse) {
        return switch (årsak) {
            case IKKE_BOSATTADRESSE_I_TRONDHEIM -> "Du er ikke registrert med bostedsadresse i Trondheim.";
            case IKKE_BOSTEDSADRESSE_OG_IKKE_FOLKEREGISTRERT_I_TRONDHEIM -> "Du er verken bostedsregistrert eller folkeregistrert i Trondheim.";
            case STUDIE_ELLER_ARBEIDSSTED_UTENFOR_TRONDHEIM -> "Studiestedet eller arbeidsstedet ditt er utenfor Trondheim.";
            case ANNET -> (fritekstbeskrivelse != null && !fritekstbeskrivelse.isBlank()) ? fritekstbeskrivelse : "Annet.";
            case UDEFINERT -> null;
        };
    }

    public static String bostedKildeForklaring(BostedsavklaringKildeType kilde, String kildeFritekst) {
        return switch (kilde) {
            case BRUKER -> "Vi har fått opplysninger fra deg.";
            case FOLKEREGISTER -> "Vi har fått opplysninger fra Folkeregisteret";
            case ANNET -> "Vi har fått opplysninger fra " + kildeFritekst;
        };
    }
}
