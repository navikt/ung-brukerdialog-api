alter table BD_OPPGAVE_DATA_BEKREFT_BOSTED
    add column ikke_oppfylt_arsak_fritekstbeskrivelse text,
    add column ikke_oppfylt_arsak varchar(100) not null default 'UDEFINERT';

comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.ikke_oppfylt_arsak_fritekstbeskrivelse is 'Fritekstbeskrivelse som er utfylt kun når årsak = ANNET.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.ikke_oppfylt_arsak is 'Årsak til at bostedsvilkåret tas opp til vurdering.';
