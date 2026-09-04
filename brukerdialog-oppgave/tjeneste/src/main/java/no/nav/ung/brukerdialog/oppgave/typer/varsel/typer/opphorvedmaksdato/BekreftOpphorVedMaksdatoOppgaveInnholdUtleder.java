package no.nav.ung.brukerdialog.oppgave.typer.varsel.typer.opphorvedmaksdato;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveAvsnitt;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.tekst.OppgaveTekst;
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.opphorvedmaksdato.BekreftOpphorVedMaksdatoOppgavetypeDataDto;
import no.nav.ung.brukerdialog.oppgave.BrukerdialogOppgaveEntitet;
import no.nav.ung.brukerdialog.oppgave.OppgaveDataMapperFraEntitetTilDto;
import no.nav.ung.brukerdialog.oppgave.OppgaveInnholdUtleder;
import no.nav.ung.brukerdialog.oppgave.OppgaveTekster;
import no.nav.ung.brukerdialog.oppgave.OppgaveTypeRef;
import no.nav.ung.brukerdialog.pdf.NorskDatoFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Kilde: {@code sif-brukerdialog/.../oppgavepaneler/opphor-ved-maksdato/i18n/nb.ts} og
 * {@code OpphorVedMaksdatoOppgavetekst.tsx}. {@code maxDato} er bevisst utelatt - kilden bruker
 * kun {@code sluttdato}/«sisteDag».
 * <p>
 * Bruker {@link OppgaveTekster#ytelseNavn} direkte («med ungdomsprogramytelsen»/«med
 * aktivitetspenger» passer begge grammatisk) - ingen ytelseskvalifikator-metode trengs her.
 */
@OppgaveTypeRef(OppgaveType.BEKREFT_OPPHOR_VED_MAKSDATO)
@ApplicationScoped
public class BekreftOpphorVedMaksdatoOppgaveInnholdUtleder implements OppgaveInnholdUtleder {

    private Instance<OppgaveDataMapperFraEntitetTilDto> mappere;
    private String ungdomsprogramytelsenDeltakerBaseUrl;

    BekreftOpphorVedMaksdatoOppgaveInnholdUtleder() {
        // for CDI proxy
    }

    @Inject
    public BekreftOpphorVedMaksdatoOppgaveInnholdUtleder(
        @Any Instance<OppgaveDataMapperFraEntitetTilDto> mappere,
        @KonfigVerdi(value = "UNGDOMPROGRAMSYTELSEN_DELTAKER_BASE_URL") String ungdomsprogramytelsenDeltakerBaseUrl
    ) {
        this.mappere = mappere;
        this.ungdomsprogramytelsenDeltakerBaseUrl = ungdomsprogramytelsenDeltakerBaseUrl;
    }

    @Override
    public String tittel(BrukerdialogOppgaveEntitet oppgave) {
        return "Tilbakemelding på sluttdato " + OppgaveTekster.ytelsePreposisjonsfrase(oppgave.getYtelsetype());
    }

    @Override
    public List<OppgaveTekst> tekster(BrukerdialogOppgaveEntitet oppgave) {
        BekreftOpphorVedMaksdatoOppgavetypeDataDto dto = hentDto(oppgave);
        String ytelseNavn = OppgaveTekster.ytelseNavn(oppgave.getYtelsetype());
        String sluttdato = NorskDatoFormat.datoLang(dto.sluttdato());

        List<OppgaveTekst> tekster = new ArrayList<>();
        tekster.add(new OppgaveAvsnitt("Din siste dag med %s er %s. Det er fordi du har brukt opp dagene du kan motta %s."
            .formatted(ytelseNavn, sluttdato, ytelseNavn), true));
        tekster.add(new OppgaveAvsnitt("Du får denne meldingen slik at du kan komme med en tilbakemelding på datoen. Du svarer på Min side på nav.no."));
        tekster.add(new OppgaveAvsnitt("Ingen tilbakemelding? Kryss av på \"Nei\" med en gang og send inn svaret ditt."));
        OppgaveTekster.leggTilSvarfrist(tekster, oppgave.getFristTid(), "svare",
            "Hvis vi ikke hører fra deg innen svarfristen har gått ut, bruker vi %s som siste dag med ytelsen når vi behandler saken din."
                .formatted(sluttdato));
        return tekster;
    }

    @Override
    public String varselLenke(BrukerdialogOppgaveEntitet oppgave) {
        return ungdomsprogramytelsenDeltakerBaseUrl + "/oppgave" + oppgave.getOppgavereferanse();
    }

    private BekreftOpphorVedMaksdatoOppgavetypeDataDto hentDto(BrukerdialogOppgaveEntitet oppgave) {
        return (BekreftOpphorVedMaksdatoOppgavetypeDataDto) OppgaveDataMapperFraEntitetTilDto
            .finnTjeneste(mappere, oppgave.getOppgaveType())
            .tilDto(oppgave.getOppgaveData());
    }
}
