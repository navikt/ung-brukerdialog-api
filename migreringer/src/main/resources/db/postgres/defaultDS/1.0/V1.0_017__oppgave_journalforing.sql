-- Migration: Journalføring av brukerdialogoppgaver mot Dokarkiv.

create sequence if not exists SEQ_BD_OPPGAVE_JOURNALFORING increment by 50 minvalue 1000000;

create table BD_OPPGAVE_JOURNALFORING
(
    id              bigint       not null primary key,
    bd_oppgave_id   bigint       not null unique references BD_OPPGAVE (id),
    tema            varchar(10)  not null,
    fagsaksystem    varchar(20)  not null,
    sakstype        varchar(20)  not null,
    fagsak_id       varchar(20),
    status          varchar(20)  not null default 'PLANLAGT',
    journalpost_id  varchar(20),
    journalfort_tid timestamp(3),
    versjon         bigint       not null default 0,
    opprettet_av    varchar(20)  not null default 'VL',
    opprettet_tid   timestamp(3) not null default current_timestamp,
    endret_av       varchar(20),
    endret_tid      timestamp(3),

    constraint chk_bd_oppgave_journalforing_status
        check (status in ('PLANLAGT', 'JOURNALFORT')),
    constraint chk_bd_oppgave_journalforing_journalpost
        check ((status = 'JOURNALFORT') = (journalpost_id is not null)),
    constraint chk_bd_oppgave_journalforing_sak
        check ((sakstype = 'FAGSAK') = (fagsak_id is not null))
);

create index idx_bd_oppgave_journalforing_etterslep
    on BD_OPPGAVE_JOURNALFORING (opprettet_tid) where status = 'PLANLAGT';

comment on table  BD_OPPGAVE_JOURNALFORING                 is 'Journalføring av en brukerdialogoppgave mot Dokarkiv. Én rad per oppgave.';
comment on column BD_OPPGAVE_JOURNALFORING.bd_oppgave_id   is 'FK til BD_OPPGAVE.id. Unik – én journalpost per oppgave.';
comment on column BD_OPPGAVE_JOURNALFORING.tema            is 'Dokarkiv-tema, utledet fra oppgavens ytelsetype ved opprettelse.';
comment on column BD_OPPGAVE_JOURNALFORING.fagsaksystem    is 'Fagsaksystem, utledet fra oppgavens ytelsetype ved opprettelse.';
comment on column BD_OPPGAVE_JOURNALFORING.sakstype        is 'Dokarkiv-sakstype: FAGSAK eller GENERELL_SAK.';
comment on column BD_OPPGAVE_JOURNALFORING.fagsak_id       is 'Saksnummer i fagsaksystemet. Satt når og bare når sakstype er FAGSAK.';
comment on column BD_OPPGAVE_JOURNALFORING.status          is 'PLANLAGT (venter på journalføring) eller JOURNALFORT.';
comment on column BD_OPPGAVE_JOURNALFORING.journalpost_id  is 'Journalpost-ID fra Dokarkiv. Satt når og bare når status er JOURNALFORT.';
comment on column BD_OPPGAVE_JOURNALFORING.journalfort_tid is 'Tidspunkt for vellykket journalføring.';
