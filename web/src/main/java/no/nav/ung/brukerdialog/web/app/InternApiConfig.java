package no.nav.ung.brukerdialog.web.app;


import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.ApplicationPath;
import no.nav.k9.felles.konfigurasjon.env.Environment;
import no.nav.openapi.spec.utils.http.DynamicObjectMapperResolverVaryFilter;
import no.nav.openapi.spec.utils.jackson.DynamicJacksonJsonProvider;
import no.nav.openapi.spec.utils.openapi.OpenApiSetupHelper;
import no.nav.openapi.spec.utils.openapi.PrefixStrippingFQNTypeNameResolver;
import no.nav.ung.brukerdialog.web.app.exceptions.KnownExceptionMappers;
import no.nav.ung.brukerdialog.web.app.jackson.ObjectMapperFactory;
import no.nav.ung.brukerdialog.web.app.jackson.ObjectMapperResolver;
import no.nav.ung.brukerdialog.web.app.tjenester.InternRestClasses;
import no.nav.ung.brukerdialog.web.server.caching.CacheControlFeature;
import no.nav.ung.brukerdialog.web.server.typedresponse.TypedResponseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import java.util.LinkedHashSet;

@ApplicationPath(InternApiConfig.API_URI)
public class InternApiConfig extends ResourceConfig {

    public static final String API_URI = "/intern/api";
    private static final String SECURITY_SCHEME_NAME = "azure-ad";
    private static final Environment ENV = Environment.current();

    public OpenAPI resolveOpenAPI() {
        final var info = new Info()
            .title("Ung brukerdialog API - Intern-API for funkasjonalitet rundt brukerdialog for ungdomsprogramytelsen og aktivitetspenger")
            .version("1.0")
            .description("REST grensesnitt for operasjoner fra veileder, saksbehandler og interne systemer knyttet til vedtaksløsning for ungdomsprogramytelsen og aktivitetspenger.");

        final var scope = ENV.getProperty("CLIENT_SCOPE");

        String azureLoginUrl = System.getenv("AZURE_LOGIN_URL");
        final var azureAdScheme = new SecurityScheme()
            .type(SecurityScheme.Type.OAUTH2)
            .name("oauth2")
            .description("Azure AD - logg inn med NAV-ident")
            .flows(new OAuthFlows()
                .authorizationCode(new OAuthFlow()
                    .authorizationUrl(azureLoginUrl + "/authorize")
                    .tokenUrl(azureLoginUrl + "/token")
                    .scopes(new Scopes().addString(scope, "Tilgang til Ung brukerdialog intern-API"))));

        final var server = new Server().url("/ung/brukerdialog");
        final var openapiSetupHelper = new OpenApiSetupHelper(this, info, server);
        new InternRestClasses().getRestClasses()
            .forEach(c -> openapiSetupHelper.addResourceClass(c.getName()));
        // The same classes registered as subtypes in object mapper are registered as subtypes in openapi setup helper:
        openapiSetupHelper.registerSubTypes(ObjectMapperFactory.allJsonTypeNameClasses(new InternRestClasses()));
        openapiSetupHelper.setTypeNameResolver(new PrefixStrippingFQNTypeNameResolver("no.nav."));
        try {
            OpenAPI openAPI = openapiSetupHelper.resolveOpenAPI();
            openAPI.schemaRequirement(SECURITY_SCHEME_NAME, azureAdScheme);
            openAPI.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
            return openAPI;
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public InternApiConfig() {
        register(DynamicJacksonJsonProvider.class); // Denne må registrerast før anna OpenAPI oppsett for å fungere.
        final var resolvedOpenAPI = resolveOpenAPI();
        register(new no.nav.openapi.spec.utils.openapi.OpenApiResource(resolvedOpenAPI));

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        registerClasses(new LinkedHashSet<>(new InternRestClasses().getRestClasses()));

        register(ObjectMapperResolver.class);
        register(DynamicObjectMapperResolverVaryFilter.class);

        registerInstances(new LinkedHashSet<>(new KnownExceptionMappers().getExceptionMappers()));
        register(CacheControlFeature.class);
        register(TypedResponseFeature.class);

        property(org.glassfish.jersey.server.ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);

    }

}
