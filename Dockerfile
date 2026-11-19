# syntax=docker/dockerfile:1.7.0-labs

FROM ghcr.io/navikt/k9-felles/felles-java-chainguard-25:12.2.10 AS duplikatfjerner

COPY --link --exclude=no.nav.ung.brukerdialog* web/target/lib/ /build/lib/
USER root
RUN ["java", "scripts/RyddBiblioteker", "DUPLIKAT", "/app/lib", "/build/lib"]



FROM ghcr.io/navikt/k9-felles/felles-java-chainguard-25:12.2.10
LABEL org.opencontainers.image.source=https://github.com/navikt/ung-brukerdialog-api

COPY --link --from=duplikatfjerner /build/lib/ /app/lib/
USER root
RUN ["java", "scripts/RyddBiblioteker", "UBRUKT", "/app/lib"]
USER 65532

COPY --link web/target/classes/logback.xml /app/conf/

##kopier prosjektets moduler
COPY --link web/target/lib/no.nav.ung.brukerdialog* /app/lib/
COPY --link web/target/app.jar /app/

EXPOSE 8902
# Chainguard-imaget bruker ikke JAVA_OPTS
CMD ["-Djdk.virtualThreadScheduler.parallelism=8", "-jar", "app.jar"]
