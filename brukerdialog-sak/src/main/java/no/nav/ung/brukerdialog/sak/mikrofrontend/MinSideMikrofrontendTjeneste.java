package no.nav.ung.brukerdialog.sak.mikrofrontend;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;
import no.nav.ung.brukerdialog.typer.AktørId;

import java.util.Optional;

@Dependent
public class MinSideMikrofrontendTjeneste {

    private final MinSideMikrofrontendRepository repository;
    private final ProsessTaskTjeneste prosessTaskTjeneste;

    @Inject
    public MinSideMikrofrontendTjeneste(MinSideMikrofrontendRepository repository, ProsessTaskTjeneste prosessTaskTjeneste) {
        this.repository = repository;
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    /**
     * @return true dersom mikrofrontenden ble aktivert nå, false dersom den allerede var aktivert.
     */
    public boolean aktiver(AktørId aktørId, MikrofrontendId mikrofrontendId) {
        Optional<MinSideMikrofrontendEntitet> eksisterende = repository.hent(aktørId, mikrofrontendId);
        if (eksisterende.isPresent() && eksisterende.get().getStatus() == MikrofrontendStatus.AKTIVERT) {
            return false;
        }
        MinSideMikrofrontendEntitet entitet = eksisterende.orElseGet(
            () -> new MinSideMikrofrontendEntitet(aktørId, mikrofrontendId, MikrofrontendStatus.AKTIVERT));
        entitet.setStatus(MikrofrontendStatus.AKTIVERT);
        repository.lagre(entitet);
        opprettPubliseringstask(aktørId, mikrofrontendId);
        return true;
    }

    /**
     * @return true dersom mikrofrontenden ble deaktivert nå, false dersom den ikke var aktivert.
     */
    public boolean deaktiver(AktørId aktørId, MikrofrontendId mikrofrontendId) {
        Optional<MinSideMikrofrontendEntitet> eksisterende = repository.hent(aktørId, mikrofrontendId);
        if (eksisterende.isEmpty() || eksisterende.get().getStatus() == MikrofrontendStatus.DEAKTIVERT) {
            return false;
        }
        MinSideMikrofrontendEntitet entitet = eksisterende.get();
        entitet.setStatus(MikrofrontendStatus.DEAKTIVERT);
        repository.lagre(entitet);
        opprettPubliseringstask(aktørId, mikrofrontendId);
        return true;
    }

    private void opprettPubliseringstask(AktørId aktørId, MikrofrontendId mikrofrontendId) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(PubliserMinSideMikrofrontendTask.class);
        prosessTaskData.setAktørId(aktørId.getId());
        prosessTaskData.setProperty(PubliserMinSideMikrofrontendTask.MIKROFRONTEND_ID, mikrofrontendId.name());
        prosessTaskTjeneste.lagre(prosessTaskData);
    }
}
