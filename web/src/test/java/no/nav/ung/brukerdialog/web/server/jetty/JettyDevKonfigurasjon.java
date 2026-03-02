package no.nav.ung.brukerdialog.web.server.jetty;

public class JettyDevKonfigurasjon extends JettyWebKonfigurasjon {
    private static final int SSL_SERVER_PORT = 8444;
    private static final int DEV_SERVER_PORT = 8902;

    public JettyDevKonfigurasjon() {
        super(DEV_SERVER_PORT);
    }

    @Override
    public int getSslPort() {
        return SSL_SERVER_PORT;
    }

}
