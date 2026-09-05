create sequence if not exists SEQ_BD_FAGSAK increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BD_VEDTAK_PERIODE increment by 50 minvalue 1000000;

create table BD_FAGSAK
(
    id            bigint       not null primary key,
    aktoer_id     varchar(20)  not null,
    ytelse_type   varchar(50)  not null,
    saksnummer    varchar(19)  not null unique,
    versjon       bigint       not null default 0,
    opprettet_av  varchar(20)  not null default 'VL',
    opprettet_tid timestamp(3) not null default current_timestamp,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

create index idx_bd_fagsak_aktoer_ytelse on BD_FAGSAK (aktoer_id, ytelse_type);

comment on table BD_FAGSAK is 'Speiling av saken i ung-sak';
comment on column BD_FAGSAK.saksnummer is 'Saksnummeret fra ung-sak';

create table BD_VEDTAK_PERIODE
(
    id            bigint       not null primary key,
    fagsak_id     bigint       not null references BD_FAGSAK (id),
    periode       daterange    not null,
    resultat      varchar(20)  not null,
    aktiv         boolean      not null default true,
    opprettet_av  varchar(20)  not null default 'VL',
    opprettet_tid timestamp(3) not null default current_timestamp,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

create index idx_bd_vedtak_periode_fagsak on BD_VEDTAK_PERIODE (fagsak_id) where aktiv;

comment on table BD_VEDTAK_PERIODE is 'Perioder med vedtak.';
comment on column BD_VEDTAK_PERIODE.resultat is 'INNVILGET eller AVSLÅTT.';

alter table BD_SOEKNAD_HENDELSE
    add column mottatt_i_fagsak bigint references BD_FAGSAK (id);

create index idx_bd_soeknad_hendelse_fagsak on BD_SOEKNAD_HENDELSE (mottatt_i_fagsak);

comment on column BD_SOEKNAD_HENDELSE.mottatt_i_fagsak is 'Fagsaken ung-sak meldte at søknaden er mottatt i. NULL betyr at ung-sak har ikke behandlet saken.';
