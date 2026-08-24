package no.nav.ung.brukerdialog.oppgave.journalforing;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Delt brevtekst-logikk mellom {@code typer}-klassene.
 * <p>
 * To formål:
 * <ol>
 *   <li><b>Ytelseskvalifikator</b> ({@link #ytelsePreposisjonsfrase}/{@link #ytelseNavn}) - gjør
 *   ytelsen (ungdomsytelse/aktivitetspenger) eksplisitt i tittel og brødtekst for oppgavetyper
 *   som ellers ikke sier det. {@code OppgaveYtelsetype} er et fritt felt på entiteten, uavhengig
 *   av {@code OppgaveType} - selv om enkelte typer i praksis kun brukes for én ytelse i dag
 *   (se {@code sif-brukerdialog}s {@code lovverk.ts}), er det en frontend-antakelse, ikke en
 *   kontraktgaranti. Kvalifikatoren gjør dokumentet korrekt uansett.</li>
 *   <li><b>Delt «endret startdato»/«endret sluttdato»-tekst</b> - {@code BEKREFT_ENDRET_PERIODE}
 *   kan vise nøyaktig samme narrativ som de dedikerte {@code BEKREFT_ENDRET_STARTDATO}/
 *   {@code _SLUTTDATO}-typene (f.eks. når {@code endringer = {ENDRET_STARTDATO}} o.l.).
 *   Ved å hente både tittel og data fra samme sted her, og bruke samme Handlebars-partial
 *   ({@code partial/innhold/endret-startdato}, {@code .../endret-sluttdato}) i begge maler, kan
 *   ikke teksten drifte i to retninger over tid.</li>
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

    /**
     * Innholdsdata for «endret startdato»-narrativet, brukt både av
     * {@code EndretStartdatoOppgaveDokumentUtleder} og {@code EndretPeriodeOppgaveDokumentUtleder}
     * (gren {@code ENDRET_STARTDATO}) sammen med Handlebars-partialen
     * {@code partial/innhold/endret-startdato}.
     */
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

    /**
     * Innholdsdata for «endret sluttdato»/«meldt ut»-narrativet (branches internt på
     * {@code erMeldtUt}), brukt både av {@code EndretSluttdatoOppgaveDokumentUtleder} og
     * {@code EndretPeriodeOppgaveDokumentUtleder} (gren {@code ENDRET_SLUTTDATO}) sammen med
     * Handlebars-partialen {@code partial/innhold/endret-sluttdato}.
     */
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
     * Innholdsdata for «fjernet periode»-narrativet ({@code BEKREFT_ENDRET_PERIODE} med
     * {@code endringer = {FJERNET_PERIODE}}), kuratert fra
     * {@code oppgavepaneler/fjernet-periode/i18n/nb.ts}.
     * <p>
     * <b>Bevisst forenkling:</b> kildeteksten («meldt deg ut <i>av ungdomsprogrammet</i>»,
     * «stoppe ungdomsprogramytelsen <i>din</i>») bruker et fast «programmet»-begrep og
     * possessiv-bøying som ikke generaliserer grammatisk til aktivitetspenger (et pluralisert
     * substantiv - «aktivitetspenger din» er ugrammatisk). Derfor to fullstendige,
     * ytelsetype-spesifikke setninger her, framfor ordrett gjenbruk av en frase/substantiv-
     * parameter slik de øvrige narrativene gjør. {@code SØK_YTELSE}/{@code BEKREFT_ENDRET_PERIODE}
     * med aktivitetspenger er i dag ikke en reell kombinasjon
     * ({@code sif-brukerdialog}s {@code lovverk.ts} markerer denne oppgavetypen som
     * ungdomsytelse-only i praksis), men journalføringen skal likevel gi et korrekt og
     * arkivverdig dokument om det skulle skje.
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
     * Innholdsdata for «endret start- og sluttdato»-narrativet ({@code BEKREFT_ENDRET_PERIODE}
     * med {@code endringer = {ENDRET_STARTDATO, ENDRET_SLUTTDATO}}), kuratert fra
     * {@code oppgavepaneler/endret-start-og-sluttdato/i18n/nb.ts}. I motsetning til
     * {@link #fjernetPeriodeInnhold}, generaliserer denne kildeteksten grammatisk fint til
     * aktivitetspenger (ingen possessiv-bøying av ytelsesnavnet), så her brukes samme
     * frase/substantiv-parametrisering som i de andre narrativene.
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
     * Fallback-narrativ for enhver {@code endringer}-kombinasjon ingen dedikert tekst er
     * skrevet for ennå (bl.a. {@code ANDRE_ENDRINGER}), eller en ellers gjenkjent kombinasjon
     * der forventede datoer mangler. Frontend kaster i dette tilfellet
     * (ser {@code parseOppgaverElement.ts}) - journalføring MÅ likevel produsere et gyldig,
     * arkiverbart dokument. {@code nyPeriodeFom}/{@code nyPeriodeTom} er begge valgfrie her,
     * i motsetning til de andre narrativene.
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

    /**
     * Svarfrist vises unntaksløst når satt, uavhengig av oppgavetype - ingen særbehandling per
     * type.
     */
    public static void leggTilSvarfrist(Map<String, Object> data, LocalDateTime fristTid) {
        if (fristTid != null) {
            data.put("svarfrist", fristTid.toLocalDate().toString());
        }
    }

    /**
     * Visningstekst for {@code BostedsvilkårIkkeOppfyltÅrsak} - fem nye
     * visningstekster uten frontend-forelegg, siden {@code BostedVilkarOppgavePanelOppgavetekst.tsx}
     * ikke viser årsaken i dag. {@code null} når vilkåret er oppfylt ({@code UDEFINERT}) - skal da
     * ikke vises i det hele tatt.
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
}
