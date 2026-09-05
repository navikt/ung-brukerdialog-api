package no.nav.ung.brukerdialog.kontrakt.sak.diagnostikk;

import java.util.List;

public record DiagnostikkSakResponse(
    String aktørId,
    List<FagsakDumpDto> fagsaker,
    List<SøknadHendelseDumpDto> søknadHendelser
) {
}
