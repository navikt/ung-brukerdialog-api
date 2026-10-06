create sequence if not exists SEQ_BD_MIN_SIDE_MIKROFRONTEND increment by 50 minvalue 1000000;

create table BD_MIN_SIDE_MIKROFRONTEND
(
    id               bigint       not null primary key,
    aktoer_id        varchar(20)  not null,
    mikrofrontend_id varchar(100) not null,
    status           varchar(20)  not null,
    versjon          bigint       not null default 0,
    opprettet_av     varchar(20)  not null default 'VL',
    opprettet_tid    timestamp(3) not null default current_timestamp,
    endret_av        varchar(20),
    endret_tid       timestamp(3),
    constraint uk_bd_min_side_mikrofrontend_aktoer unique (aktoer_id, mikrofrontend_id)
);

comment on table BD_MIN_SIDE_MIKROFRONTEND is 'Status for mikrofrontender (innganger) på Min side som er aktivert eller deaktivert for en bruker.';
comment on column BD_MIN_SIDE_MIKROFRONTEND.aktoer_id is 'AktørId til brukeren mikrofrontenden gjelder.';
comment on column BD_MIN_SIDE_MIKROFRONTEND.mikrofrontend_id is 'Hvilken mikrofrontend det gjelder, f.eks. AKTIVITETSPENGER_INNSYN.';
comment on column BD_MIN_SIDE_MIKROFRONTEND.status is 'Siste status sendt til Min side: AKTIVERT eller DEAKTIVERT.';
