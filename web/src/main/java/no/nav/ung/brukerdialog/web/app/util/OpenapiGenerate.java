package no.nav.ung.brukerdialog.web.app.util;

import io.swagger.v3.oas.models.OpenAPI;
import no.nav.openapi.spec.utils.openapi.FileOutputter;
import no.nav.ung.brukerdialog.web.app.InternApiConfig;

import java.io.IOException;

public class OpenapiGenerate {

    public static void main(String[] args) throws IOException {
        final InternApiConfig internApiConfig = new InternApiConfig();
        final OpenAPI resolved = internApiConfig.resolveOpenAPI();
        final var outputPath = args.length > 0 ? args[0] : "";
        FileOutputter.writeJsonFile(resolved, outputPath);
    }
}
