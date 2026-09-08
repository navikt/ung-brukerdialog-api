package no.nav.ung.brukerdialog.pdf;

import java.text.NumberFormat;
import java.util.Locale;

public final class NorskBeløpFormat {

    private static final Locale NORSK = Locale.of("no");
    private static final NumberFormat HELE_KRONER = NumberFormat.getIntegerInstance(NORSK);

    private NorskBeløpFormat() {
    }

    /** F.eks. «12 345 kr» - tusenskilletegn og fortegn følger {@code NumberFormat} for norsk locale. */
    public static String kroner(long beløp) {
        return HELE_KRONER.format(beløp).replace('\u00A0', ' ') + " kr";
    }
}
