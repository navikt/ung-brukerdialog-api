package no.nav.ung.brukerdialog.kontrakt;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.ung.brukerdialog.abac.StandardAbacAttributt;
import no.nav.ung.brukerdialog.typer.AktørId;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class AktørIdDto {

    @JsonProperty("aktørId")
    @NotNull
    @Valid
    private AktørId aktørId;

    public AktørIdDto() {
        //
    }

    @JsonCreator
    public AktørIdDto(@JsonProperty("aktørId") @NotNull @Valid String aktørId) {
        this.aktørId = new AktørId(aktørId);
    }

    @StandardAbacAttributt(StandardAbacAttributtType.AKTØR_ID)
    public String getAktorId() {
        return aktørId.getId();
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }
}
