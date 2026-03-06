window.onload = function() {
  // Begin Swagger UI call region
  const ui = SwaggerUIBundle({
    urls: [
      { url: "/ung/brukerdialog/ekstern/api/openapi.json", name: "ekstern-API" },
      { url: "/ung/brukerdialog/intern/api/openapi.json", name: "intern-API" }
    ],
    "urls.primaryName": "Bruker-API",
    dom_id: '#swagger-ui',
    deepLinking: true,
    oauth2RedirectUrl: window.location.origin + "/ung/brukerdialog/swagger/oauth2-redirect.html",
    persistAuthorization: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout",
  });
  // End Swagger UI call region

  window.ui = ui
}
