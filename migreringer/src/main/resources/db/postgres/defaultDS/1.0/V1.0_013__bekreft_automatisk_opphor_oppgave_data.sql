-- Migration: Table for BEKREFT_AUTOMATISK_OPPHOR oppgavedata
-- Stores the end date and max date for automatic expiry tasks.

create table BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR
(
    id              bigint      not null primary key,
    bd_oppgave_id   bigint      not null references BD_OPPGAVE (id),
    sluttdato       date        not null,
    max_dato        date        not null,
    opprettet_av    varchar(20) not null default 'VL',
    opprettet_tid   timestamp(3) not null default current_timestamp,
    endret_av       varchar(20),
    endret_tid      timestamp(3)
);

create index idx_bd_oppgave_data_bekreft_auto_opphor_oppgave_id on BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR (bd_oppgave_id);

comment on table  BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR               is 'Oppgavedata for type BEKREFT_AUTOMATISK_OPPHOR.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR.id            is 'Primary key.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR.bd_oppgave_id is 'FK til BD_OPPGAVE.id.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR.sluttdato     is 'Dato ytelsen opphører.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR.max_dato      is 'Maks-dato beregnet fra antall virkedager.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR.opprettet_av  is 'Saksbehandler/system som opprettet raden.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR.opprettet_tid is 'Tidspunkt da raden ble opprettet.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR.endret_av     is 'Saksbehandler/system som sist endret raden.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR.endret_tid    is 'Tidspunkt da raden sist ble endret.';
