create sequence if not exists SEQ_BD_SOKNAD_HENDELSE increment by 50 minvalue 1000000;

create table BD_SOKNAD_HENDELSE
(
    id            bigint       not null primary key,
    soknad_id     uuid         not null unique,
    aktoer_id     varchar(20)  not null,
    ytelse_type   varchar(50)  not null,
    mottatt       timestamp(3) not null,
    aktiv         boolean      not null default true,
    versjon       bigint       not null default 0,
    opprettet_av  varchar(20)  not null default 'VL',
    opprettet_tid timestamp(3) not null default current_timestamp,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

create index idx_bd_soknad_hendelse_aktoer_ytelse on BD_SOKNAD_HENDELSE (aktoer_id, ytelse_type, mottatt desc);

comment on table BD_SOKNAD_HENDELSE is 'Registrering av at en deltaker har sendt inn søknad. Opprettes synkront av k9-brukerdialog-prosessering ved innsending, og brukes til å avgjøre om deltakeren kan sende ny søknad.';
comment on column BD_SOKNAD_HENDELSE.soknad_id is 'Søknadens id fra brukerdialogen.';
comment on column BD_SOKNAD_HENDELSE.aktoer_id is 'AktørId til deltakeren som sendte inn søknaden.';
comment on column BD_SOKNAD_HENDELSE.ytelse_type is 'Ytelsen søknaden gjelder. En deltaker kan ha flere søknader per ytelse.';
comment on column BD_SOKNAD_HENDELSE.mottatt is 'Tidspunktet søknaden ble mottatt i brukerdialogen.';
comment on column BD_SOKNAD_HENDELSE.aktiv is 'Om søknaden skal telle med når det utledes hva deltakeren kan søke om. Settes til false for søknader som er sendt inn ved en feil - raden beholdes for sporbarhet.';
