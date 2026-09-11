package no.nav.ung.brukerdialog.pdf;

import java.text.NumberFormat;
import java.util.Locale;

public final class NorskBeløpFormat {

    private static final Locale NORSK = Locale.of("no");
    private static final NumberFormat HELE_KRONER = NumberFormat.getIntegerInstance(NORSK);

    private NorskBeløpFormat() {
    }

    /**
     * F.eks. «12 345 kr» - tusenskille og mellomrommet foran «kr» er non-breaking space (U+00A0),
     * slik at beløpet aldri brytes over to linjer i PDF-en. Merk: verdien brukes også i
     * {@code varselInnhold} (API-kontrakten mot frontend), så NBSP-tegnet følger med dit også -
     * ren tekstsammenligning/-parsing i frontend må ta høyde for dette.
     */
    public static String kroner(long beløp) {
        return HELE_KRONER.format(beløp) + "\u00A0kr";
    }
}
