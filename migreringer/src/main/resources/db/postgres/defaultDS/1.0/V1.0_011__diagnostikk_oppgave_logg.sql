-- Loggfører aksess og operasjoner på oppgave for diagnostikk-formål

CREATE SEQUENCE IF NOT EXISTS SEQ_DIAGNOSTIKK_OPPGAVE_LOGG INCREMENT BY 50 MINVALUE 1000000;

CREATE TABLE DIAGNOSTIKK_OPPGAVE_LOGG
(
    id               bigint       NOT NULL PRIMARY KEY,
    oppgavereferanse uuid         NOT NULL,
    tjeneste         varchar(200),
    begrunnelse      varchar(4000),
    opprettet_av     varchar(20)  NOT NULL DEFAULT 'VL',
    opprettet_tid    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    endret_av        varchar(20),
    endret_tid       timestamp
);

CREATE INDEX idx_diagnostikk_oppgave_logg_ref ON DIAGNOSTIKK_OPPGAVE_LOGG (oppgavereferanse);

COMMENT ON TABLE DIAGNOSTIKK_OPPGAVE_LOGG IS 'Logger aksess og operasjoner på oppgave for diagnostikk- og revisjonsformål.';
COMMENT ON COLUMN DIAGNOSTIKK_OPPGAVE_LOGG.id IS 'Primary Key.';
COMMENT ON COLUMN DIAGNOSTIKK_OPPGAVE_LOGG.oppgavereferanse IS 'Ekstern referanse (UUID) til oppgaven som ble aksessert.';
COMMENT ON COLUMN DIAGNOSTIKK_OPPGAVE_LOGG.tjeneste IS 'Tjeneste/endepunkt som utførte operasjonen.';
COMMENT ON COLUMN DIAGNOSTIKK_OPPGAVE_LOGG.begrunnelse IS 'Begrunnelse for aksess, oppgitt av den som utfører operasjonen.';
COMMENT ON COLUMN DIAGNOSTIKK_OPPGAVE_LOGG.opprettet_av IS 'Bruker/system som opprettet logginnslaget.';
COMMENT ON COLUMN DIAGNOSTIKK_OPPGAVE_LOGG.opprettet_tid IS 'Tidspunkt for opprettelse av logginnslaget.';
COMMENT ON COLUMN DIAGNOSTIKK_OPPGAVE_LOGG.endret_av IS 'Bruker/system som sist endret logginnslaget.';
COMMENT ON COLUMN DIAGNOSTIKK_OPPGAVE_LOGG.endret_tid IS 'Tidspunkt for siste endring av logginnslaget.';
