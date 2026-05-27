-- Migration: Rename oppgavedata objects from BEKREFT_AUTOMATISK_OPPHOR to BEKREFT_OPPHOR_VED_MAKSDATO

alter table BD_OPPGAVE_DATA_BEKREFT_AUTOMATISK_OPPHOR
    rename to BD_OPPGAVE_DATA_BEKREFT_OPPHOR_VED_MAKSDATO;

alter index idx_bd_oppgave_data_bekreft_auto_opphor_oppgave_id
    rename to idx_bd_oppgave_data_bekreft_opphor_ved_maksdato_oppgave_id;

comment on table BD_OPPGAVE_DATA_BEKREFT_OPPHOR_VED_MAKSDATO
    is 'Oppgavedata for type BEKREFT_OPPHOR_VED_MAKSDATO.';

