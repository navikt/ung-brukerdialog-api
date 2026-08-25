package no.nav.ung.brukerdialog.journalforing.pdf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Norsk datoformatering til brevtekst, delt mellom {@link PdfGenerator}s Handlebars-hjelpere og
 * titel-utledningen, slik at de alltid formaterer likt.
 * <p>
 * Speiler {@code sif-brukerdialog/packages/sif-utils/src/dateFormatter.ts}. {@link Locale#of(String)}
 * med {@code "no"} er verifisert å gi små forbokstaver på månedsnavn («januar», ikke «Januar»).
 */
public final class NorskDatoFormat {

    private static final Locale NORSK = Locale.of("no");
    private static final DateTimeFormatter DATO_LANG = DateTimeFormatter.ofPattern("d. MMMM yyyy", NORSK);
    private static final DateTimeFormatter MÅNED = DateTimeFormatter.ofPattern("MMMM", NORSK);
    private static final DateTimeFormatter MÅNED_ÅR = DateTimeFormatter.ofPattern("MMMM yyyy", NORSK);

    private NorskDatoFormat() {
    }

    /** F.eks. «1. januar 2021» - {@code dateFormatter.full}. */
    public static String datoLang(LocalDate dato) {
        return DATO_LANG.format(dato);
    }

    /** F.eks. «januar» - {@code dateFormatter.month}. */
    public static String måned(LocalDate dato) {
        return MÅNED.format(dato);
    }

    /** F.eks. «januar 2021» - {@code dateFormatter.monthFullYear}. */
    public static String månedÅr(LocalDate dato) {
        return MÅNED_ÅR.format(dato);
    }
}
