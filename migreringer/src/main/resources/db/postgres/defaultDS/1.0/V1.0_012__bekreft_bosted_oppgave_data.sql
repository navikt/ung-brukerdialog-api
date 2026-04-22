-- Migration: Table for BEKREFT_BOSTED oppgavedata
-- Stores the period and the saksbehandler's suggested bosted-avklaring.

create table BD_OPPGAVE_DATA_BEKREFT_BOSTED
(
    id                      bigint      not null primary key,
    bd_oppgave_id           bigint      not null references BD_OPPGAVE (id),
    fra_og_med              date        not null,
    til_og_med              date        not null,
    er_bosatt_i_trondheim   boolean     not null,
    opprettet_av            varchar(20) not null default 'VL',
    opprettet_tid           timestamp   not null default current_timestamp,
    endret_av               varchar(20),
    endret_tid              timestamp
);

comment on table  BD_OPPGAVE_DATA_BEKREFT_BOSTED                           is 'Oppgavedata for type BEKREFT_BOSTED.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.id                        is 'Primary key.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.bd_oppgave_id             is 'FK til BD_OPPGAVE.id.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.fra_og_med                is 'Startdato for perioden oppgaven gjelder.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.til_og_med                is 'Sluttdato for perioden oppgaven gjelder.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.er_bosatt_i_trondheim     is 'Saksbehandlers foreslåtte bosattavklaring.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.opprettet_av              is 'Saksbehandler/system som opprettet raden.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.opprettet_tid             is 'Tidspunkt da raden ble opprettet.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.endret_av                 is 'Saksbehandler/system som sist endret raden.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.endret_tid                is 'Tidspunkt da raden sist ble endret.';
