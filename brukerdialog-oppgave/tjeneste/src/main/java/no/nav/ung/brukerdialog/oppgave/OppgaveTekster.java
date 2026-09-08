package no.nav.ung.brukerdialog.oppgave;

import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
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

    public static List<OppgaveTekst> omVarselSeksjon() {
        List<OppgaveTekst> tekster = new ArrayList<>();
        tekster.add(new OppgaveAvsnitt(
            "Om «%s»".formatted(VARSEL_OM_NYE_OPPLYSNINGER_TITTEL),
            "Dette varselet sendes ut slik at brukeren har mulighet til å komme med en tilbakemelding på opplysningene før Nav fatter vedtak. Tilbakemeldingen sendes inn via Min side på nav.no.",
            false));
        tekster.add(new OppgaveAvsnitt("Hvis vi ikke hører noe fra brukeren, bruker Nav opplysningene over når vedtaket fattes."));
        return tekster;
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

    public static String endretSluttdatoTittel(boolean erMeldtUt) {
        return erMeldtUt ? "Sluttdato" : "Endret sluttdato";
    }

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

    public static String fjernetPeriodeTittel() {
        return "Stans";
    }

    public static List<OppgaveTekst> fjernetPeriodeInnhold(OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        List<OppgaveTekst> tekster = new ArrayList<>();
        tekster.add(new OppgaveAvsnitt(switch (ytelsetype) {
            case UNGDOMSYTELSE -> "Veilederen din har meldt deg ut av ungdomsprogrammet fordi du ikke skal delta i programmet likevel.";
            case AKTIVITETSPENGER -> "Veilederen din har meldt deg ut fordi du ikke skal delta likevel.";
        }));
        tekster.add(new OppgaveAvsnitt(switch (ytelsetype) {
            case UNGDOMSYTELSE -> "Du kan bare få ungdomsprogramytelsen hvis du deltar i programmet, og derfor stopper vi den. Du svarer på Min side på nav.no.";
            case AKTIVITETSPENGER -> "Du kan bare få aktivitetspenger hvis du opprettholder aktiviteten Nav har bestemt for deg, og derfor stopper vi utbetalingen. Du svarer på Min side på nav.no.";
        }));
        tekster.add(new OppgaveAvsnitt("Har du en tilbakemelding? Ta kontakt med veilederen din først. Når dere har snakket sammen, sender du inn svaret ditt."));
        leggTilSvarfrist(tekster, fristTid, "svare", null);
        return tekster;
    }

    public static String endretStartOgSluttdatoTittel() {
        return "Ny start- og sluttdato";
    }

    public static List<OppgaveTekst> endretStartOgSluttdatoInnhold(LocalDate nyFom, LocalDate nyTom,
                                                                    OppgaveYtelsetype ytelsetype, LocalDateTime fristTid) {
        List<OppgaveTekst> tekster = new ArrayList<>();
        tekster.add(new OppgaveAvsnitt("Veilederen din har endret start- og sluttdatoen din %s. Vi vil derfor endre start- og sluttdatoen for %s også."
            .formatted(ytelsePreposisjonsfrase(ytelsetype), ytelseNavn(ytelsetype))));
        tekster.add(new OppgaveAvsnitt("Du vil nå få %s i perioden %s til %s.".formatted(
            ytelseNavn(ytelsetype), NorskDatoFormat.datoLang(nyFom), NorskDatoFormat.datoLang(nyTom)), true));
        tekster.add(new OppgaveAvsnitt("Du får denne meldingen slik at du kan komme med en tilbakemelding på perioden. Du svarer på Min side på nav.no."));
        tekster.add(new OppgaveAvsnitt("Har du en tilbakemelding? Ta kontakt med veilederen din først. Når dere har snakket sammen, sender du inn svaret ditt."));
        leggTilSvarfrist(tekster, fristTid, "svare",
            "Hvis vi ikke hører fra deg innen svarfristen har gått ut, bruker vi perioden %s til %s når vi behandler saken din."
                .formatted(NorskDatoFormat.datoLang(nyFom), NorskDatoFormat.datoLang(nyTom)),
            true);
        return tekster;
    }

    public static String ukjentPeriodeendringTittel() {
        return "Endring i perioden";
    }

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
        boolean harDato = nyFom != null || nyTom != null;
        tekster.add(new OppgaveAvsnitt(setning.toString(), harDato));
        tekster.add(new OppgaveAvsnitt("Du får denne meldingen slik at du kan komme med en tilbakemelding på endringen. Du svarer på Min side på nav.no."));
        tekster.add(new OppgaveAvsnitt("Har du en tilbakemelding? Ta kontakt med veilederen din først. Når dere har snakket sammen, sender du inn svaret ditt."));
        leggTilSvarfrist(tekster, fristTid, "svare", null);
        return tekster;
    }

    private static void leggTilStandardSvarSetninger(List<OppgaveTekst> tekster) {
        tekster.add(new OppgaveAvsnitt("Du får denne meldingen slik at du kan komme med en tilbakemelding på datoen. Du svarer på Min side på nav.no."));
        tekster.add(new OppgaveAvsnitt("Har du en tilbakemelding? Ta kontakt med veilederen din først. Når dere har snakket sammen, sender du inn svaret ditt."));
    }

    public static void leggTilSvarfrist(List<OppgaveTekst> tekster, LocalDateTime fristTid,
                                         String handlingsverb, String konsekvensSetningEllerNull) {
        leggTilSvarfrist(tekster, fristTid, handlingsverb, konsekvensSetningEllerNull, false);
    }

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

    public static String bostedVarselTekst(BostedsvilkårIkkeOppfyltÅrsak årsak, LocalDate fom, LocalDate tom) {
        String tidsrom = tom != null
            ? "i perioden %s til %s ikke".formatted(NorskDatoFormat.datoLang(fom), NorskDatoFormat.datoLang(tom))
            : "fra %s ikke lenger".formatted(NorskDatoFormat.datoLang(fom));

        return switch (årsak) {
            case IKKE_BOSATTADRESSE_I_TRONDHEIM -> "Vi har fått opplysninger om at du %s bor i Trondheim kommune. Du må ha bostedsadresse i Trondheim kommune for å få aktivitetspenger."
                .formatted(tidsrom);
            case IKKE_BOSTEDSADRESSE_OG_IKKE_FOLKEREGISTRERT_I_TRONDHEIM -> "Vi har fått opplysninger om at du %s bor i Trondheim kommune, og at du heller ikke er folkeregistrert der. Du må ha bostedsadresse i Trondheim kommune for å få aktivitetspenger."
                .formatted(tidsrom);
            case STUDIE_ELLER_ARBEIDSSTED_UTENFOR_TRONDHEIM -> "Vi har fått opplysninger om at du %s har studie- eller arbeidssted i Trondheim kommune. Du må bo i Trondheim kommune for å få aktivitetspenger."
                .formatted(tidsrom);
            case ANNET, UDEFINERT -> "Vi har fått opplysninger om at du %s bor i Trondheim kommune. Du må bo i Trondheim kommune for å få aktivitetspenger."
                .formatted(tidsrom);
        };
    }

    public static String bostedAnnetFritekst(BostedsvilkårIkkeOppfyltÅrsak årsak, String fritekstbeskrivelse) {
        if (årsak != BostedsvilkårIkkeOppfyltÅrsak.ANNET) {
            return null;
        }
        return (fritekstbeskrivelse != null && !fritekstbeskrivelse.isBlank()) ? fritekstbeskrivelse : "Annet.";
    }

    public static String bostedKildeLabel(BostedsavklaringKildeType kilde, String kildeFritekst) {
        return switch (kilde) {
            case BRUKER -> "Deg";
            case FOLKEREGISTER -> "Folkeregisteret";
            case ANNET -> kildeFritekst;
        };
    }

    public static OppgaveAvsnitt bostedKildeAvsnitt(BostedsavklaringKildeType kilde, String kildeFritekst) {
        return new OppgaveAvsnitt("Hvor har vi fått opplysningene fra?", bostedKildeLabel(kilde, kildeFritekst), false);
    }
}
