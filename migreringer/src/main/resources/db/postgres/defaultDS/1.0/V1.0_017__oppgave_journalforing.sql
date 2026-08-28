-- Migration: Journalføring av brukerdialogoppgaver mot Dokarkiv.

create sequence if not exists SEQ_BD_OPPGAVE_JOURNALFORING increment by 50 minvalue 1000000;

create table BD_OPPGAVE_JOURNALFORING
(
    id              bigint       not null primary key,
    bd_oppgave_id   bigint       not null unique references BD_OPPGAVE (id),
    fagsaksystem    varchar(20)  not null,
    saksnummer      varchar(20),
    journalpost_id  varchar(20)  not null,
    journalfort_tid timestamp(3) not null,
    versjon         bigint       not null default 0,
    opprettet_av    varchar(20)  not null default 'VL',
    opprettet_tid   timestamp(3) not null default current_timestamp,
    endret_av       varchar(20),
    endret_tid      timestamp(3)
);

comment on table  BD_OPPGAVE_JOURNALFORING                 is 'Journalføring av en brukerdialogoppgave mot Dokarkiv. Én rad per oppgave - opprettes kun når oppgaven faktisk er journalført.';
comment on column BD_OPPGAVE_JOURNALFORING.bd_oppgave_id   is 'FK til BD_OPPGAVE.id. Unik – én journalpost per oppgave.';
comment on column BD_OPPGAVE_JOURNALFORING.fagsaksystem    is 'Fagsaksystem, utledet fra oppgavens ytelsetype.';
comment on column BD_OPPGAVE_JOURNALFORING.saksnummer      is 'Saksnummer i fagsaksystemet. Satt når og bare når Dokarkiv-sakstype er FAGSAK (avledet: FAGSAK ⟺ saksnummer er satt, ellers GENERELL_SAK - lagres ikke som egen kolonne).';
comment on column BD_OPPGAVE_JOURNALFORING.journalpost_id  is 'Journalpost-ID fra Dokarkiv.';
comment on column BD_OPPGAVE_JOURNALFORING.journalfort_tid is 'Tidspunkt for vellykket journalføring.';
