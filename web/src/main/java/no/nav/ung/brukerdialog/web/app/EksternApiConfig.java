package no.nav.ung.brukerdialog.web.app;

import io.swagger.v3.core.jackson.TypeNameResolver;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.ApplicationPath;
import no.nav.openapi.spec.utils.openapi.OpenApiSetupHelper;
import no.nav.ung.brukerdialog.web.app.exceptions.KnownExceptionMappers;
import no.nav.ung.brukerdialog.web.app.jackson.ObjectMapperFactory;
import no.nav.ung.brukerdialog.web.app.tjenester.BrukerRestClasses;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import java.util.LinkedHashSet;

@ApplicationPath(EksternApiConfig.API_URI)
public class EksternApiConfig extends ResourceConfig {

    public static final String API_URI = "/ekstern/api";
    private static final String SECURITY_SCHEME_NAME = "Authorization";

    public OpenAPI resolveOpenAPI() {
        final var info = new Info()
            .title("Ung brukerdialog - Bruker-API")
            .version("1.0")
            .description("REST grensesnitt for data tilgjengelig for sluttbruker knyttet til vedtaksløsning for ungdomsprogramytelsen og aktivitetspenger.");

        final var tokenXScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .name(SECURITY_SCHEME_NAME)
            .scheme("bearer")
            .bearerFormat("JWT")

            .description("""
                    Eksempel på verdi som skal inn i Value-feltet (Bearer trengs altså ikke å oppgis): 'eyAidH...'
                    For nytt token -> https://tokenx-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:k9saksbehandling:ung-brukerdialog-api
                """.trim())
            .in(SecurityScheme.In.HEADER);

        final var server = new Server().url("/ung/brukerdialog");
        final var openapiSetupHelper = new OpenApiSetupHelper(this, info, server);
        new BrukerRestClasses().getRestClasses()
            .forEach(c -> openapiSetupHelper.addResourceClass(c.getName()));
        openapiSetupHelper.registerSubTypes(ObjectMapperFactory.allJsonTypeNameClasses(new BrukerRestClasses()));
        TypeNameResolver.std.setUseFqn(false);
        try {
            OpenAPI openAPI = openapiSetupHelper.resolveOpenAPI();
            openAPI.schemaRequirement(SECURITY_SCHEME_NAME, tokenXScheme);
            openAPI.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
            return openAPI;
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public EksternApiConfig() {
        register(no.nav.openapi.spec.utils.jackson.DynamicJacksonJsonProvider.class);
        final var resolvedOpenAPI = resolveOpenAPI();
        register(new no.nav.openapi.spec.utils.openapi.OpenApiResource(resolvedOpenAPI));

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        registerClasses(new LinkedHashSet<>(new BrukerRestClasses().getRestClasses()));
        register(no.nav.ung.brukerdialog.web.app.jackson.ObjectMapperResolver.class);
        register(no.nav.openapi.spec.utils.http.DynamicObjectMapperResolverVaryFilter.class);
        registerInstances(new LinkedHashSet<>(new KnownExceptionMappers().getExceptionMappers()));
        register(no.nav.ung.brukerdialog.web.server.caching.CacheControlFeature.class);
        register(no.nav.ung.brukerdialog.web.server.typedresponse.TypedResponseFeature.class);
        property(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
    }

}
