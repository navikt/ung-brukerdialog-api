alter table BD_OPPGAVE_DATA_BEKREFT_BOSTED
    add column kilde varchar(100),
    add column kilde_fritekst varchar(1000);

-- Eksisterende oppgaver er opprettet før kilde ble innført. De er alle basert på
-- opplysninger fra bruker selv, så BRUKER er riktig verdi historisk.
update BD_OPPGAVE_DATA_BEKREFT_BOSTED set kilde = 'BRUKER' where kilde is null;

alter table BD_OPPGAVE_DATA_BEKREFT_BOSTED
    alter column kilde set not null;

comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.kilde is 'Hvor saksbehandler har fått opplysningene fra, jf. BostedsavklaringKildeType.';
comment on column BD_OPPGAVE_DATA_BEKREFT_BOSTED.kilde_fritekst is 'Fritekstbeskrivelse av kilden, utfylt kun når kilde = ANNET.';
