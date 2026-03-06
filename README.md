# ung-brukerdialog-api
Tilbyr funksjonalitet for innsyn og brukervarsling for ungdomsprogramytelsen og aktivitetspenger


# Utvikling

## Enhetstester
Start postgres først for å kjøre alle enhetstester. Bruker schema ung_brukerdialog_unit i
[Verdikjede](https://github.com/navikt/k9-verdikjede/tree/master/saksbehandling)
`git clone git@github.com:navikt/k9-verdikjede.git; cd k9-verdikjede/saksbehandling; ./update-versions.sh; docker-compose up postgres`

Kjør `no.nav.ung.brukerdialog.db.util.Databaseskjemainitialisering` for å få med skjemaendringer

## Lokal utvikling
1. Start postgres først. Bruker schema ung_sak lokalt
   `cd dev; docker-compose up postgres`

2. Start webserver fra f.eks. IDE
   Start `JettyDevServer --vtp`

Swagger: http://localhost:8902/ung/brukerdialog/swagger


## Tilkobling til database
For å koble til databasen i dev-gcp kan man kjøre denne hjelpe-scriptet:

```shell script
./scripts/nais-postgres.sh --context dev-gcp --namespace k9saksbehandling --app ung-brukerdialog-api
```

## Kode generert av GitHub Copilot

Dette repoet bruker GitHub Copilot til å generere kode.
