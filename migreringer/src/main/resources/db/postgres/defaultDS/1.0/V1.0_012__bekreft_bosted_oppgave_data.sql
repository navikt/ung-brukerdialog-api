-- Migration: Table for BEKREFT_BOSTED oppgavedata
-- Stores the period and whether the user is registered as resident in Trondheim.

create table BD_OPPGAVE_DATA_BEKREFT_BOSTED
(
    id                      bigint      not null primary key,
    bd_oppgave_id           bigint      not null references BD_OPPGAVE (id),
    fom                     date        not null,
    tom                     date        not null,
    er_bosatt_i_trondheim   boolean     not null,
    opprettet_av            varchar(20) not null default 'VL',
    opprettet_tid           timestamp(3) not null default current_timestamp,
    endret_av               varchar(20),
    endret_tid              timestamp(3)
);

comment on table  BD_OPPGAVE_DATA_BEKREFT_BOSTED                           is 'Oppgavedata for type BEKREFT_BOSTED.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.id                        is 'Primary key.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.bd_oppgave_id             is 'FK til BD_OPPGAVE.id.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.fom                       is 'Startdato for perioden oppgaven gjelder.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.tom                       is 'Sluttdato for perioden oppgaven gjelder.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.er_bosatt_i_trondheim     is 'Om bruker er bosatt i Trondheim for den angitte perioden.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.opprettet_av              is 'Saksbehandler/system som opprettet raden.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.opprettet_tid             is 'Tidspunkt da raden ble opprettet.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.endret_av                 is 'Saksbehandler/system som sist endret raden.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.endret_tid                is 'Tidspunkt da raden sist ble endret.';
