package no.nav.ung.brukerdialog.oppgave;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsavklaringKildeType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.bosted.BostedsvilkårIkkeOppfyltÅrsak;
import no.nav.ung.brukerdialog.pdf.NorskDatoFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Delt brevtekst-logikk mellom {@code OppgaveInnholdUtleder}-implementasjonene i {@code typer}-
 * pakkene. Bygger {@link OppgaveTekst}-lister - samme tekster brukes i PDF-dokumentet, som
 * min-side-varsel (første element, se {@link OppgaveInnholdUtleder}) og i {@code tekster}-feltet
 * på {@code BrukerdialogOppgaveDto}. To formål:
 * <ol>
 *   <li><b>Ytelseskvalifikator</b> ({@link #ytelsePreposisjonsfrase}/{@link #ytelseNavn}) - gjør
 *   ytelsen eksplisitt der oppgavetypen ellers ikke sier det. {@code OppgaveYtelsetype} er et
 *   fritt felt, uavhengig av {@code OppgaveType} - ikke en kontraktgaranti, selv der kun én
 *   ytelse brukes i praksis i dag.</li>
 *   <li><b>Delt «endret startdato/sluttdato»-tekst</b> - {@code BEKREFT_ENDRET_PERIODE} kan vise
 *   samme narrativ som de dedikerte typene. Samme metode i begge utledere hindrer at teksten
 *   drifter i to retninger.</li>
 * </ol>
 */
public final class OppgaveTekster {

    private OppgaveTekster() {
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

    /** Delt av {@code EndretStartdatoOppgaveInnholdUtleder} og {@code EndretPeriodeOppgaveInnholdUtleder} (gren {@code ENDRET_STARTDATO}). */
    public static List<OppgaveTekst> endretStartdatoInnhold(LocalDate nyStartdato, LocalDate forrigeStartdato,
                                                             OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        List<OppgaveTekst> tekster = new ArrayList<>();
        tekster.add(new OppgaveAvsnitt("Veilederen din har endret startdatoen din %s til %s."
            .formatted(ytelsePreposisjonsfrase(ytelsetype), NorskDatoFormat.datoLang(nyStartdato)), true));
        leggTilStandardSvarSetninger(tekster);
        leggTilSvarfrist(tekster, fristTid, "svare",
            "Hvis vi ikke hører fra deg innen svarfristen har gått ut, bruker vi %s som startdato når vi behandler saken din."
                .formatted(NorskDatoFormat.datoLang(nyStartdato)));
        return tekster;
    }

    public static String endretSluttdatoTittel(OppgaveYtelsetype ytelsetype, boolean erMeldtUt) {
        String base = erMeldtUt ? "Tilbakemelding på sluttdato" : "Tilbakemelding på endret sluttdato";
        return base + " " + ytelsePreposisjonsfrase(ytelsetype);
    }

    /** Delt av {@code EndretSluttdatoOppgaveInnholdUtleder} og {@code EndretPeriodeOppgaveInnholdUtleder} (gren {@code ENDRET_SLUTTDATO}). */
    public static List<OppgaveTekst> endretSluttdatoInnhold(LocalDate nySluttdato, LocalDate forrigeSluttdato,
                                                             OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        List<OppgaveTekst> tekster = new ArrayList<>();
        boolean erMeldtUt = erMeldtUt(forrigeSluttdato);
        tekster.add(new OppgaveAvsnitt(erMeldtUt
            ? "Veilederen din har meldt deg ut %s med sluttdato %s.".formatted(
                ytelsePreposisjonsfrase(ytelsetype), NorskDatoFormat.datoLang(nySluttdato))
            : "Veilederen din har endret sluttdatoen din %s til %s.".formatted(
                ytelsePreposisjonsfrase(ytelsetype), NorskDatoFormat.datoLang(nySluttdato)), true));
        leggTilStandardSvarSetninger(tekster);
        leggTilSvarfrist(tekster, fristTid, "svare",
            "Hvis vi ikke hører fra deg innen svarfristen har gått ut, bruker vi %s som sluttdato når vi behandler saken din."
                .formatted(NorskDatoFormat.datoLang(nySluttdato)));
        return tekster;
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
    public static List<OppgaveTekst> fjernetPeriodeInnhold(OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        List<OppgaveTekst> tekster = new ArrayList<>();
        tekster.add(new OppgaveAvsnitt(switch (ytelsetype) {
            case UNGDOMSYTELSE -> "Veilederen din har meldt deg ut av ungdomsprogrammet fordi du ikke skal delta i programmet likevel.";
            case AKTIVITETSPENGER -> "Veilederen din har meldt deg ut fordi du ikke skal delta likevel.";
        }));
        tekster.add(new OppgaveAvsnitt(switch (ytelsetype) {
            case UNGDOMSYTELSE -> "Du kan bare få ungdomsprogramytelsen hvis du deltar i programmet, og derfor stopper vi den. Du svarer på Min side på nav.no.";
            case AKTIVITETSPENGER -> "Du kan bare få aktivitetspenger hvis du deltar, og derfor stopper vi utbetalingen. Du svarer på Min side på nav.no.";
        }));
        tekster.add(new OppgaveAvsnitt("Har du en tilbakemelding? Ta kontakt med veilederen din først. Når dere har snakket sammen, sender du inn svaret ditt."));
        tekster.add(new OppgaveAvsnitt("Ingen tilbakemelding? Kryss av på \"Nei\" med en gang og send inn svaret ditt. Jo fortere du svarer, jo fortere får vi behandlet saken din."));
        leggTilSvarfrist(tekster, fristTid, "svare", null);
        return tekster;
    }

    public static String endretStartOgSluttdatoTittel(OppgaveYtelsetype ytelsetype) {
        return "Tilbakemelding på ny start- og sluttdato for " + ytelseNavn(ytelsetype);
    }

    /**
     * Kilde: {@code oppgavepaneler/endret-start-og-sluttdato/i18n/nb.ts}. I motsetning til
     * {@link #fjernetPeriodeInnhold} generaliserer denne teksten grammatisk fint til
     * aktivitetspenger, så vanlig frase/substantiv-parametrisering brukes her.
     */
    public static List<OppgaveTekst> endretStartOgSluttdatoInnhold(LocalDate nyFom, LocalDate nyTom,
                                                                    OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        List<OppgaveTekst> tekster = new ArrayList<>();
        tekster.add(new OppgaveAvsnitt("Veilederen din har endret start- og sluttdatoen din %s. Vi vil derfor endre start- og sluttdatoen for %s også."
            .formatted(ytelsePreposisjonsfrase(ytelsetype), ytelseNavn(ytelsetype))));
        tekster.add(new OppgaveAvsnitt("Du vil nå få %s i perioden %s til %s.".formatted(
            ytelseNavn(ytelsetype), NorskDatoFormat.datoLang(nyFom), NorskDatoFormat.datoLang(nyTom)), true));
        tekster.add(new OppgaveAvsnitt("Du får denne meldingen slik at du kan komme med en tilbakemelding på perioden. Du svarer på Min side på nav.no."));
        tekster.add(new OppgaveAvsnitt("Ingen tilbakemelding? Kryss av på \"Nei\" med en gang og send inn svaret ditt. Jo fortere du svarer, jo fortere får vi behandlet saken din."));
        tekster.add(new OppgaveAvsnitt("Har du en tilbakemelding? Ta kontakt med veilederen din først. Når dere har snakket sammen, sender du inn svaret ditt."));
        leggTilSvarfrist(tekster, fristTid, "svare",
            "Hvis vi ikke hører fra deg innen svarfristen har gått ut, bruker vi perioden %s til %s når vi behandler saken din."
                .formatted(NorskDatoFormat.datoLang(nyFom), NorskDatoFormat.datoLang(nyTom)),
            true);
        return tekster;
    }

    public static String ukjentPeriodeendringTittel(OppgaveYtelsetype ytelsetype) {
        return "Tilbakemelding på endring i perioden " + ytelsePreposisjonsfrase(ytelsetype);
    }

    /**
     * Fallback for kombinasjoner uten dedikert tekst (bl.a. {@code ANDRE_ENDRINGER}) eller
     * manglende datoer. Frontend kaster her ({@code parseOppgaverElement.ts}) - journalføring MÅ
     * likevel produsere et gyldig, arkiverbart dokument.
     */
    public static List<OppgaveTekst> ukjentPeriodeendringInnhold(LocalDate nyFom, LocalDate nyTom,
                                                                  OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        List<OppgaveTekst> tekster = new ArrayList<>();
        StringBuilder setning = new StringBuilder("Det er gjort en endring i perioden din ")
            .append(ytelsePreposisjonsfrase(ytelsetype));
        if (nyFom != null) {
            setning.append(", med virkning fra ").append(NorskDatoFormat.datoLang(nyFom));
        }
        if (nyTom != null) {
            setning.append(" til ").append(NorskDatoFormat.datoLang(nyTom));
        }
        setning.append(".");
        // Fet kun når minst én dato faktisk er med - originalen fetter kun selve
        // <strong>{{dato}}</strong>-fragmentene, som begge er betinget av isNotNull. Uten noen
        // dato er hele setningen vanlig skrift i originalen.
        boolean harDato = nyFom != null || nyTom != null;
        tekster.add(new OppgaveAvsnitt(setning.toString(), harDato));
        tekster.add(new OppgaveAvsnitt("Du får denne meldingen slik at du kan komme med en tilbakemelding på endringen. Du svarer på Min side på nav.no."));
        tekster.add(new OppgaveAvsnitt("Ingen tilbakemelding? Kryss av på \"Nei\" med en gang og send inn svaret ditt. Jo fortere du svarer, jo fortere får vi behandlet saken din."));
        tekster.add(new OppgaveAvsnitt("Har du en tilbakemelding? Ta kontakt med veilederen din først. Når dere har snakket sammen, sender du inn svaret ditt."));
        leggTilSvarfrist(tekster, fristTid, "svare", null);
        return tekster;
    }

    /**
     * De to setningene delt av flere narrativer om "endret dato" ("Du får denne meldingen ...",
     * "Ingen tilbakemelding? ...", "Har du en tilbakemelding? ..."). Trekt ut for å unngå
     * risikoen for at de tre drifter fra hverandre på tvers av {@link #endretStartdatoInnhold} og
     * {@link #endretSluttdatoInnhold}.
     */
    private static void leggTilStandardSvarSetninger(List<OppgaveTekst> tekster) {
        tekster.add(new OppgaveAvsnitt("Du får denne meldingen slik at du kan komme med en tilbakemelding på datoen. Du svarer på Min side på nav.no."));
        tekster.add(new OppgaveAvsnitt("Ingen tilbakemelding? Kryss av på \"Nei\" med en gang og send inn svaret ditt. Jo fortere du svarer, jo fortere får vi behandlet saken din."));
        tekster.add(new OppgaveAvsnitt("Har du en tilbakemelding? Ta kontakt med veilederen din først. Når dere har snakket sammen, sender du inn svaret ditt."));
    }

    /**
     * Svarfrist-setning(er), delt av samtlige oppgavetyper. Ingenting legges til dersom
     * {@code fristTid} er {@code null}. Den første setningen er alltid fet (speiler tidligere
     * {@code .frist p:first-child}-CSS-regel, nå uttrykt per blokk i stedet for som en
     * container-effekt - se Fase 1-beslutning i plansporet for denne modellen).
     *
     * @param handlingsverb          «svare» eller «søke» (eneste to som forekommer i dag).
     * @param konsekvensSetningEllerNull setningen om hva som skjer ved manglende svar - {@code null}
     *                               for narrativene som ikke har en slik oppfølgingssetning.
     */
    public static void leggTilSvarfrist(List<OppgaveTekst> tekster, LocalDateTime fristTid,
                                         String handlingsverb, String konsekvensSetningEllerNull) {
        leggTilSvarfrist(tekster, fristTid, handlingsverb, konsekvensSetningEllerNull, false);
    }

    /**
     * Variant av {@link #leggTilSvarfrist} for det ene unntakstilfellet
     * ({@code endretStartOgSluttdatoInnhold}) der konsekvenssetningen opprinnelig også inneholdt
     * fet skrift (datoene i «... bruker vi perioden <b>{{fom}}</b> til <b>{{tom}}</b> ...»).
     */
    public static void leggTilSvarfrist(List<OppgaveTekst> tekster, LocalDateTime fristTid,
                                         String handlingsverb, String konsekvensSetningEllerNull,
                                         boolean konsekvensSetningFet) {
        if (fristTid == null) {
            return;
        }
        tekster.add(new OppgaveAvsnitt(
            "Fristen for å %s er senest %s.".formatted(handlingsverb, NorskDatoFormat.datoLang(fristTid.toLocalDate())),
            true));
        if (konsekvensSetningEllerNull != null) {
            tekster.add(new OppgaveAvsnitt(konsekvensSetningEllerNull, konsekvensSetningFet));
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
            case BRUKER -> "Vi har fått opplysninger om dette fra deg.";
            case FOLKEREGISTER -> "Vi har fått opplysninger om dette fra Folkeregisteret.";
            case ANNET -> "Vi har fått opplysninger om dette fra " + kildeFritekst + ".";
        };
    }
}
