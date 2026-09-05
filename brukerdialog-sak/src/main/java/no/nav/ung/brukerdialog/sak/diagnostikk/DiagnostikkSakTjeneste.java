package no.nav.ung.brukerdialog.sak.diagnostikk;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.ung.brukerdialog.kontrakt.sak.diagnostikk.DiagnostikkSakResponse;
import no.nav.ung.brukerdialog.kontrakt.sak.diagnostikk.FagsakDumpDto;
import no.nav.ung.brukerdialog.kontrakt.sak.diagnostikk.SøknadHendelseDumpDto;
import no.nav.ung.brukerdialog.kontrakt.sak.diagnostikk.VedtakPeriodeDumpDto;
import no.nav.ung.brukerdialog.sak.fagsak.FagSakEntitet;
import no.nav.ung.brukerdialog.sak.fagsak.FagsakRepository;
import no.nav.ung.brukerdialog.sak.fagsak.VedtakPeriodeEntitet;
import no.nav.ung.brukerdialog.sak.soknad.SøknadHendelseEntitet;
import no.nav.ung.brukerdialog.typer.AktørId;
import no.nav.ung.brukerdialog.typer.Saksnummer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class DiagnostikkSakTjeneste {

    private DiagnostikkSakRepository diagnostikkSakRepository;
    private FagsakRepository fagsakRepository;

    public DiagnostikkSakTjeneste() {
        // CDI proxy
    }

    @Inject
    public DiagnostikkSakTjeneste(DiagnostikkSakRepository diagnostikkSakRepository, FagsakRepository fagsakRepository) {
        this.diagnostikkSakRepository = diagnostikkSakRepository;
        this.fagsakRepository = fagsakRepository;
    }

    public Optional<AktørId> finnAktørForSaksnummer(Saksnummer saksnummer) {
        return fagsakRepository.hentForSaksnummer(saksnummer).map(FagSakEntitet::getAktørId);
    }

    public DiagnostikkSakResponse dump(AktørId aktørId) {
        List<FagSakEntitet> fagsaker = diagnostikkSakRepository.hentAlleFagsaker(aktørId);
        Map<Saksnummer, List<VedtakPeriodeEntitet>> perioder =
            diagnostikkSakRepository.hentAllePerioderPerSaksnummer(fagsaker.stream().map(FagSakEntitet::getSaksnummer).toList());

        return new DiagnostikkSakResponse(
            aktørId.getId(),
            fagsaker.stream().map(f -> tilDto(f, perioder.getOrDefault(f.getSaksnummer(), List.of()))).toList(),
            diagnostikkSakRepository.hentAlleSøknadHendelser(aktørId).stream().map(DiagnostikkSakTjeneste::tilDto).toList());
    }

    private static FagsakDumpDto tilDto(FagSakEntitet fagsak, List<VedtakPeriodeEntitet> perioder) {
        return new FagsakDumpDto(
            fagsak.getId(),
            fagsak.getSaksnummer().getVerdi(),
            fagsak.getYtelseType().name(),
            fagsak.getOpprettetAv(),
            fagsak.getOpprettetTidspunkt(),
            fagsak.getEndretAv(),
            fagsak.getEndretTidspunkt(),
            perioder.stream().map(DiagnostikkSakTjeneste::tilDto).toList());
    }

    private static VedtakPeriodeDumpDto tilDto(VedtakPeriodeEntitet periode) {
        return new VedtakPeriodeDumpDto(
            periode.getId(),
            periode.getPeriode().getFomDato(),
            periode.getPeriode().getTomDato(),
            periode.getResultat().name(),
            periode.isAktiv(),
            periode.getOpprettetAv(),
            periode.getOpprettetTidspunkt(),
            periode.getEndretAv(),
            periode.getEndretTidspunkt());
    }

    private static SøknadHendelseDumpDto tilDto(SøknadHendelseEntitet hendelse) {
        return new SøknadHendelseDumpDto(
            hendelse.getId(),
            hendelse.getSøknadId(),
            hendelse.getYtelseType().name(),
            hendelse.getMottatt(),
            hendelse.isAktiv(),
            hendelse.getMottattIFagsak() != null ? hendelse.getMottattIFagsak().getSaksnummer().getVerdi() : null,
            hendelse.getOpprettetAv(),
            hendelse.getOpprettetTidspunkt(),
            hendelse.getEndretAv(),
            hendelse.getEndretTidspunkt());
    }
}
