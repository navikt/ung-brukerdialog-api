create sequence if not exists SEQ_BD_FAGSAK increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BD_VEDTAK_PERIODE increment by 50 minvalue 1000000;

create table BD_FAGSAK
(
    id            bigint       not null primary key,
    aktoer_id     varchar(20)  not null,
    ytelse_type   varchar(50)  not null,
    saksnummer    varchar(19)  not null,
    versjon       bigint       not null default 0,
    opprettet_av  varchar(20)  not null default 'VL',
    opprettet_tid timestamp(3) not null default current_timestamp,
    endret_av     varchar(20),
    endret_tid    timestamp(3),
    constraint uq_BD_FAGSAK_saksnummer unique (saksnummer)
);

create index idx_BD_FAGSAK_aktoer_ytelse on BD_FAGSAK (aktoer_id, ytelse_type);

create table BD_VEDTAK_PERIODE
(
    id            bigint       not null primary key,
    fagsak_id     bigint       not null references BD_FAGSAK (id) on delete cascade,
    fom           date         not null,
    tom           date         not null,
    versjon       bigint       not null default 0,
    opprettet_av  varchar(20)  not null default 'VL',
    opprettet_tid timestamp(3) not null default current_timestamp,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

create index idx_BD_VEDTAK_PERIODE on BD_VEDTAK_PERIODE (vedtaksstatus_id, tom desc);

comment on table BD_FAGSAK is 'Speiling av vedtaksresultatet i ung-sak for en deltaker, meldt inn ved iverksetting. At det finnes en rad betyr at ung-sak har fattet vedtak - en rad uten perioder betyr sak uten innvilgelse, som er noe annet enn at ung-sak aldri har meldt inn noe. Inneholder kun rådata; regelen for søknadsvindu utledes i TilgjengeligSøknadUtleder.';
comment on column BD_FAGSAK.saksnummer is 'Saksnummeret i ung-sak. Unikt - en deltaker kan ha flere saker per ytelse, og periodene aggregeres på tvers av dem.';
comment on column BD_FAGSAK.kilde_behandling_id is 'Versjonsnøkkel. Behandlingsid er monotont voksende, så et kall med lavere verdi enn lagret er en forsinket melding og ignoreres.';
comment on column BD_FAGSAK.vedtakstidspunkt is 'Vedtakstidspunktet fra ung-sak. Brukes som behandlet_tidspunkt på søknadshendelsene vedtaket omfatter.';

comment on table BD_VEDTAK_PERIODE is 'Perioder der aktivitetspenger-vilkårene er oppfylt. Skrives om i sin helhet ved hver melding fra ung-sak.';
