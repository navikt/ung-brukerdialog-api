package no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.livsopphold;

/**
 * AVKORTET finnes i ung-sak, men er bevisst utelatt her: den brukes kun når saksbehandler innvilger
 * en kortere periode enn systemet tillater, og skal aldri varsles om.
 */
public enum AndreLivsoppholdsytelserIkkeOppfyltÅrsak {
    MOTTAR_ARBEIDSAVKLARINGSPENGER,
    MOTTAR_TILTAKSPENGER,
    MOTTAR_KVALIFISERINGSSTØNAD,
    MOTTAR_DAGPENGER,
    MOTTAR_FORELDREPENGER,
    MOTTAR_SVANGERSKAPSPENGER,
    MOTTAR_UFØRETRYGD,
    MOTTAR_INTRODUKSJONSSTØNAD,
    MOTTAR_BARNEPENSJON,
    MOTTAR_ANNEN_YTELSE,
    UDEFINERT
}
