package no.nav.ung.brukerdialog.oppgave.typer.oppgave.inntektsrapportering;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveListe;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.InntektsrapporteringOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.OppgaveTekster;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.pdf.NorskDatoFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Kilde: {@code sif-brukerdialog/.../oppgavepaneler/rapporter-inntekt/i18n/nb.ts} og
 * {@code RapporterInntektOppgavetekst.tsx}. «Les mer om inntekt» er i kilden en sammenleggbar
 * {@code <ReadMore>}, men skrives her ut som vanlig løpende tekst - et arkivert PDF-brev/varsel
 * har ingen sammenleggbar visning.
 */
@OppgaveTypeRef(OppgaveType.RAPPORTER_INNTEKT)
@ApplicationScoped
public class InntektsrapporteringOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;
    private String aktivitetspengerInnsynBaseUrl;

    InntektsrapporteringOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public InntektsrapporteringOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl,
        @KonfigVerdi(value = "AKTIVITETSPENGER_INNSYN_BASE_URL") String aktivitetspengerInnsynBaseUrl
    ) {
        this.mappere = mappere;
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
        this.aktivitetspengerInnsynBaseUrl = aktivitetspengerInnsynBaseUrl;
    }

    @Override
    public String tittel(BrukerdialogOppgaveEntitet oppgave) {
        InntektsrapporteringOppgavetypeDataDto dto = hentDto(oppgave);
        return "Inntekt i %s \u2013 %s".formatted(
            NorskDatoFormat.månedÅr(dto.fraOgMed()),
            OppgaveTekster.ytelsePreposisjonsfrase(oppgave.getYtelsetype()));
    }

    @Override
    public List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave) {
        InntektsrapporteringOppgavetypeDataDto dto = hentDto(oppgave);
        String måned = NorskDatoFormat.måned(dto.fraOgMed());
        String ytelseNavn = OppgaveTekster.ytelseNavn(oppgave.getYtelsetype());

        List<OppgaveTekst> tekster = new ArrayList<>();
        tekster.add(new OppgaveAvsnitt("Gi oss beskjed hvis du hadde inntekt i %s. Inntekt er lønn, men det kan også være for eksempel etterbetaling, feriepenger, overtid og tillegg for ubekvem arbeidstid."
            .formatted(måned)));
        if (dto.gjelderDelerAvMåned()) {
            tekster.add(new OppgaveAvsnitt("Du skal gi beskjed om hele inntekten du hadde i %s, selv om du ikke hadde %s hele måneden."
                .formatted(måned, ytelseNavn)));
        }
        tekster.add(new OppgaveAvsnitt("Inntekt er som regel lønnen du får fra en arbeidsgiver, men det kan være mange andre ting også. De vanligste formene for inntekt utenom lønn, er:"));
        tekster.add(new OppgaveListe(List.of(
            "etterbetaling",
            "feriepenger",
            "overtid",
            "tillegg for kveld, natt, helg og helligdag (ubekvem arbeidstid)",
            "tips",
            "frilansinntekt",
            "inntekt fra aksjeselskap (AS)"
        )));
        tekster.add(new OppgaveAvsnitt("Du kan lese mer om hva som regnes som inntekt i skatteloven §§ 5.10 til 5.15."));
        tekster.add(new OppgaveAvsnitt("Du svarer på Min side på nav.no."));
        tekster.add(new OppgaveAvsnitt("Hvis du hadde inntekt, krysser du av for Ja.", true));
        tekster.add(new OppgaveAvsnitt("Hvis du ikke hadde inntekt, krysser du av på Nei eller lar være å svare.", true));
        OppgaveTekster.leggTilSvarfrist(tekster, oppgave.getFristTid(), "svare", null);
        return tekster;
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return switch (oppgave.getYtelsetype()) {
            case AKTIVITETSPENGER -> aktivitetspengerInnsynBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
            case UNGDOMSYTELSE -> ungdomsprogramytelsenDeltakerBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
        };
    }

    private InntektsrapporteringOppgavetypeDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (InntektsrapporteringOppgavetypeDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
