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

comment on table BD_FAGSAK is 'Speiling av saken i ung-sak, meldt inn ved iverksetting. Én rad per saksnummer, oppdatert i stedet for erstattet, slik at raden er en stabil identitet søknadshendelser kan peke på. At raden finnes betyr at ung-sak har fattet vedtak - en sak uten innvilgede perioder er noe annet enn en sak vi aldri har hørt om. Inneholder kun rådata; regelen for søknadsvindu utledes i TilgjengeligSøknadUtleder.';
comment on column BD_FAGSAK.saksnummer is 'Saksnummeret i ung-sak. Unikt - en deltaker kan ha flere saker per ytelse.';

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

create index idx_BD_VEDTAK_PERIODE_fagsak on BD_VEDTAK_PERIODE (fagsak_id) where aktiv;

comment on table BD_VEDTAK_PERIODE is 'Perioder ung-sak har vurdert vilkårene for. Skrives om i sin helhet ved hver melding: de forrige deaktiveres og beholdes, slik at det i ettertid går an å se hva saken så ut som da en søknad ble sluppet gjennom eller stoppet.';
comment on column BD_VEDTAK_PERIODE.resultat is 'INNVILGET eller AVSLÅTT. Kun innvilgede perioder teller når søknadsvinduet utledes.';
comment on column BD_VEDTAK_PERIODE.aktiv is 'False for perioder som er erstattet av en nyere melding fra ung-sak. Kun aktive perioder skal brukes til utledning.';

alter table BD_SOEKNAD_HENDELSE
    add column mottatt_i_fagsak bigint references BD_FAGSAK (id);

create index idx_BD_SOEKNAD_HENDELSE_fagsak on BD_SOEKNAD_HENDELSE (mottatt_i_fagsak);

comment on column BD_SOEKNAD_HENDELSE.mottatt_i_fagsak is 'Fagsaken ung-sak meldte at søknaden er mottatt i. NULL betyr at søknaden fortsatt ligger til behandling og sperrer for ny søknad. Ortogonal til aktiv - en behandlet søknad er fortsatt aktiv.';
