package no.nav.ung.brukerdialog.web.server.jetty;

/** Dummy konfig for lokal testing. */
public class JettyDevDbKonfigurasjon {

    private String datasource = "defaultDS";
    private String url = "jdbc:postgresql://127.0.0.1:5432/ung_brukerdialog_api?reWriteBatchedInserts=true";
    private String user = "ung_brukerdialog_api";
    private String password = user;

    JettyDevDbKonfigurasjon() {
    }

    public String getDatasource() {
        return datasource;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

}
