package no.nav.ung.brukerdialog.pdf;

/**
 * Norsk kronebeløp-formatering til brevtekst, delt mellom {@link PdfGenerator}s Handlebars-hjelper
 * og Java-kode som bygger tabellinnhold direkte (f.eks. {@code OppgaveTabell}-rader), slik at de
 * alltid formaterer likt.
 */
public final class NorskBeløpFormat {

    private NorskBeløpFormat() {
    }

    /** F.eks. «12 345 kr» - mellomrom som tusenskilletegn, ingen desimaler (beløp er alltid hele kroner). */
    public static String kroner(long beløp) {
        String siffer = Long.toString(Math.abs(beløp));
        StringBuilder gruppert = new StringBuilder();
        int count = 0;
        for (int i = siffer.length() - 1; i >= 0; i--) {
            gruppert.append(siffer.charAt(i));
            count++;
            if (count % 3 == 0 && i != 0) {
                gruppert.append(' ');
            }
        }
        return (beløp < 0 ? "-" : "") + gruppert.reverse() + " kr";
    }
}
