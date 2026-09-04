alter table BD_SOKNAD_HENDELSE
    add column behandlet_tidspunkt timestamp(3);

create index idx_bd_soknad_hendelse_ubehandlet
    on BD_SOKNAD_HENDELSE (aktoer_id, ytelse_type)
    where behandlet_tidspunkt is null and aktiv;

comment on column BD_SOKNAD_HENDELSE.behandlet_tidspunkt is 'Vedtakstidspunktet fra ung-sak, satt når ung-sak melder at den har behandlet søknaden. NULL betyr at søknaden fortsatt ligger til behandling. Ortogonal til aktiv - en behandlet søknad er fortsatt aktiv.';
