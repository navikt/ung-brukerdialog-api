-- Lagrer perioden det varsles om, hvorfor bistandsvilkåret ikke er oppfylt og hvor
-- opplysningene kommer fra. tom er nullable: opphør varsles med åpen periode.
create table BD_OPPGAVE_DATA_BEKREFT_BISTAND
(
    id                                     bigint       not null primary key,
    bd_oppgave_id                          bigint       not null references BD_OPPGAVE (id),
    fom                                    date         not null,
    tom                                    date,
    ikke_oppfylt_arsak                     varchar(100) not null,
    ikke_oppfylt_arsak_fritekstbeskrivelse text,
    kilde                                  varchar(100) not null,
    kilde_fritekst                         varchar(1000),
    opprettet_av                           varchar(20)  not null default 'VL',
    opprettet_tid                          timestamp(3) not null default current_timestamp,
    endret_av                              varchar(20),
    endret_tid                             timestamp(3)
);

create index idx_bd_oppgave_data_bekreft_bistand_oppgave_id on BD_OPPGAVE_DATA_BEKREFT_BISTAND (bd_oppgave_id);

comment on table  BD_OPPGAVE_DATA_BEKREFT_BISTAND                                        is 'Oppgavedata for type BEKREFT_BISTAND.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BISTAND.id                                     is 'Primary key.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BISTAND.bd_oppgave_id                          is 'FK til BD_OPPGAVE.id.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BISTAND.fom                                    is 'Startdato for perioden oppgaven gjelder.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BISTAND.tom                                    is 'Sluttdato for perioden oppgaven gjelder. Null ved opphør, der perioden er åpen.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BISTAND.ikke_oppfylt_arsak                     is 'Årsak til at bistandsvilkåret tas opp til vurdering, jf. BistandsvilkårIkkeOppfyltÅrsak.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BISTAND.ikke_oppfylt_arsak_fritekstbeskrivelse is 'Saksbehandlers fritekstbeskrivelse av årsaken.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BISTAND.kilde                                  is 'Hvor Nav har fått opplysningene fra, jf. BistandsavklaringKildeType.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BISTAND.kilde_fritekst                         is 'Fritekstbeskrivelse av kilden, utfylt kun når kilde = ANNET.';


-- Lagrer perioden det varsles om, hvilken livsoppholdsytelse søker mottar og hvor opplysningene
-- kommer fra. tom er nullable: opphør varsles med åpen periode.
create table BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER
(
    id                                     bigint       not null primary key,
    bd_oppgave_id                          bigint       not null references BD_OPPGAVE (id),
    fom                                    date         not null,
    tom                                    date,
    ikke_oppfylt_arsak                     varchar(100) not null,
    ikke_oppfylt_arsak_fritekstbeskrivelse text,
    kilde                                  varchar(100) not null,
    kilde_fritekst                         varchar(1000),
    opprettet_av                           varchar(20)  not null default 'VL',
    opprettet_tid                          timestamp(3) not null default current_timestamp,
    endret_av                              varchar(20),
    endret_tid                             timestamp(3)
);

create index idx_bd_oppgave_data_bekreft_livsoppholdsytelser_oppgave_id on BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER (bd_oppgave_id);

comment on table  BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER                                        is 'Oppgavedata for type BEKREFT_ANDRE_LIVSOPPHOLDSYTELSER.';
comment on column BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER.id                                     is 'Primary key.';
comment on column BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER.bd_oppgave_id                          is 'FK til BD_OPPGAVE.id.';
comment on column BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER.fom                                    is 'Startdato for perioden oppgaven gjelder.';
comment on column BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER.tom                                    is 'Sluttdato for perioden oppgaven gjelder. Null ved opphør, der perioden er åpen.';
comment on column BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER.ikke_oppfylt_arsak                     is 'Årsak til at vilkåret om andre livsoppholdsytelser tas opp til vurdering, jf. AndreLivsoppholdsytelserIkkeOppfyltÅrsak.';
comment on column BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER.ikke_oppfylt_arsak_fritekstbeskrivelse is 'Saksbehandlers fritekstbeskrivelse av årsaken. Påkrevd i ung-sak når årsaken er MOTTAR_ANNEN_YTELSE.';
comment on column BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER.kilde                                  is 'Hvor Nav har fått opplysningene fra, jf. AndreLivsoppholdsytelserAvklaringKildeType.';
comment on column BD_OPPGAVE_DATA_BEKREFT_LIVSOPPHOLDSYTELSER.kilde_fritekst                         is 'Fritekstbeskrivelse av kilden, utfylt kun når kilde = ANNET.';


-- Lagrer perioden det varsles om, hvorfor aktivitetsvilkåret ikke er oppfylt og hvor
-- opplysningene kommer fra. tom er nullable: opphør varsles med åpen periode.
create table BD_OPPGAVE_DATA_BEKREFT_AKTIVITET
(
    id                                     bigint       not null primary key,
    bd_oppgave_id                          bigint       not null references BD_OPPGAVE (id),
    fom                                    date         not null,
    tom                                    date,
    ikke_oppfylt_arsak                     varchar(100) not null,
    ikke_oppfylt_arsak_fritekstbeskrivelse text,
    kilde                                  varchar(100) not null,
    kilde_fritekst                         varchar(1000),
    opprettet_av                           varchar(20)  not null default 'VL',
    opprettet_tid                          timestamp(3) not null default current_timestamp,
    endret_av                              varchar(20),
    endret_tid                             timestamp(3)
);

create index idx_bd_oppgave_data_bekreft_aktivitet_oppgave_id on BD_OPPGAVE_DATA_BEKREFT_AKTIVITET (bd_oppgave_id);

comment on table  BD_OPPGAVE_DATA_BEKREFT_AKTIVITET                                        is 'Oppgavedata for type BEKREFT_AKTIVITET.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AKTIVITET.id                                     is 'Primary key.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AKTIVITET.bd_oppgave_id                          is 'FK til BD_OPPGAVE.id.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AKTIVITET.fom                                    is 'Startdato for perioden oppgaven gjelder.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AKTIVITET.tom                                    is 'Sluttdato for perioden oppgaven gjelder. Null ved opphør, der perioden er åpen.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AKTIVITET.ikke_oppfylt_arsak                     is 'Årsak til at aktivitetsvilkåret tas opp til vurdering, jf. AktivitetsvilkåretIkkeOppfyltÅrsak.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AKTIVITET.ikke_oppfylt_arsak_fritekstbeskrivelse is 'Saksbehandlers fritekstbeskrivelse av årsaken. Påkrevd i ung-sak når årsaken er ANNET.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AKTIVITET.kilde                                  is 'Hvor Nav har fått opplysningene fra, jf. AktivitetsavklaringKildeType.';
comment on column BD_OPPGAVE_DATA_BEKREFT_AKTIVITET.kilde_fritekst                         is 'Fritekstbeskrivelse av kilden, utfylt kun når kilde = ANNET.';


-- Manglende indeks fra V1.0_012 for BD_OPPGAVE_DATA_BEKREFT_BOSTED.
create index idx_bd_oppgave_data_bekreft_bosted_oppgave_id on BD_OPPGAVE_DATA_BEKREFT_BOSTED (bd_oppgave_id);
