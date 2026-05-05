create table bd_oppgave_data_bekreft_automatisk_opphor
(
    id              bigint      not null primary key,
    bd_oppgave_id   bigint      not null references bd_oppgave (id),
    sluttdato       date        not null,
    max_dato        date        not null,
    opprettet_av    varchar(20) not null default 'VL',
    opprettet_tid   timestamp   not null default current_timestamp,
    endret_av       varchar(20),
    endret_tid      timestamp
);

create index idx_bd_oppgave_data_bekreft_auto_opphor_oppgave_id on bd_oppgave_data_bekreft_automatisk_opphor (bd_oppgave_id);

comment on table  bd_oppgave_data_bekreft_automatisk_opphor               is 'Oppgavedata for type BEKREFT_AUTOMATISK_OPPHOR.';
comment on column bd_oppgave_data_bekreft_automatisk_opphor.id            is 'Primary key.';
comment on column bd_oppgave_data_bekreft_automatisk_opphor.bd_oppgave_id is 'FK til BD_OPPGAVE.id.';
comment on column bd_oppgave_data_bekreft_automatisk_opphor.sluttdato     is 'Dato ytelsen opph\u00f8rer.';
comment on column bd_oppgave_data_bekreft_automatisk_opphor.max_dato      is 'Maks-dato beregnet fra antall virkedager.';
