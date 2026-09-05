---
applyTo: "**"
---

# Lokal kjøring av ung-brukerdialog-api

Gjelder når du skal kjøre applikasjonen lokalt — typisk for å teste egne endringer mot
verdikjedetestene i `k9-verdikjede`.

## To måter

**Docker:** bygg image (`mvn clean install -DskipTests && docker build -t ung-brukerdialog-api:latest .`)
og pek `.env` i `k9-verdikjede/saksbehandling` dit med `./local-versions.sh ung-brukerdialog-api`.
Tester det byggede imaget.

**På host (raskere iterasjon):** kjør `JettyDevServer` mot støttetjenester i Docker. Tester
arbeidstreet direkte. Se `.github/skills/run-tests/SKILL.md` i `k9-verdikjede` for stack-oppsettet.

## JettyDevServer

Port **8902** — den samme som containeren bruker, så verdikjeden trenger ingen omkonfigurering.

```
Main class:        no.nav.ung.brukerdialog.web.server.jetty.JettyDevServer
Working directory: <repo>/web
```

`--vtp`-argumentet i README **gjør ingenting** — `main` ignorerer argumentene. Konfigurasjonen
kommer fra `web/app-vtp.properties`, som `PropertiesUtils` laster fra **working directory**.
Er working dir feil, starter serveren med tomme properties og feiler på en måte som ikke peker
mot årsaken.

### Forutsetninger

- Postgres fra `k9-verdikjede/saksbehandling` (`docker compose up -d postgres`).
  Databasen `ung_brukerdialog_api` opprettes av `postgres-init/`.
- `~/.modig/{keystore,truststore}.jks` — lages av `k9-verdikjede/keystore/make-dummy-keystore.sh`.
  Kan overstyres med `-Djavax.net.ssl.keyStore` / `-Djavax.net.ssl.trustStore` (passord `vtpvtp`).
  Sertifikatet må være **det samme som vtp bruker**, ellers feiler TLS mot `https://localhost:8063`.

### Kjøring utenfor IDE

Bygg klassesti med `dependency:build-classpath`, men **kjør full `mvn install` først**:
`-pl web` alene resolverer søskenmoduler fra `~/.m2`, ikke fra reaktoren, så du risikerer å kjøre
gammel kode uten å merke det.

## To API-røtter

Applikasjonen eksponerer både et internt API (kalt av ung-sak) og et eksternt (kalt av deltaker).
De har **ulike ruter for samme funksjonalitet**. `k9-verdikjede` simulerer deltakeren og bruker
det eksterne. En `UNG_BRUKERDIALOG_API_URL` satt i skallet overstyrer klientene på begge sider —
la den være usatt lokalt.

## Kontrakten

`kontrakt`-modulen konsumeres av både `ung-sak` og `k9-verdikjede`. Endrer du en klasse der, må
den installeres lokalt før de to kompilerer mot den:

```bash
mvn install -DskipTests -Drevision=<versjon> -Dchangelist=-SNAPSHOT
```

Bruk versjonen `k9-verdikjede/verdikjede/pom.xml` og `ung-sak/pom.xml` peker på. En stale jar i
`~/.m2` gir BUILD SUCCESS lokalt og brekker i CI — verifiser innholdet:

```bash
unzip -l ~/.m2/repository/no/nav/ung/brukerdialog/kontrakt/<v>/kontrakt-<v>.jar | grep <Klasse>
```

**Kontrakten må releases før ung-sak kan bygge i CI**, mens denne applikasjonen gjerne deployes
**sist** hvis ung-sak har begynt å sende nye påkrevde felter. Avklar rekkefølgen eksplisitt før
merge.
