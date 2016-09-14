package com.elastisys.scale.commons.net.alerter.http;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.AlertSeverity;
import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.server.ServletDefinition;
import com.elastisys.scale.commons.server.ServletServerBuilder;
import com.elastisys.scale.commons.server.SslKeyStoreType;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Verifies the behavior of the {@link HttpAlerter} when no authentication has
 * been configured.
 */
public class TestHttpAlerterWithBasicAuth {
    // Server keystore
    private static final String SERVER_PKCS12_KEYSTORE = "src/test/resources/security/server/server_keystore.p12";
    private static final String SERVER_PKCS12_KEYSTORE_PASSWORD = "serverpassword";
    private static final String SERVER_REALM_FILE = "src/test/resources/security/server/security-realm.properties";

    /** The port where a HTTPS server is set up. */
    private static Integer httpsPort;
    /** Dummy HTTPS server. */
    private static Server server;
    /** The web hook that receives all alerts. */
    private static WebhookServlet webhook;

    @BeforeClass
    public static void beforeTests() throws Exception {
        // find a free port for test server
        List<Integer> freePorts = HostUtils.findFreePorts(1);
        httpsPort = freePorts.get(0);
        // server instances are created by each individual test method
        server = createBasicAuthServer(httpsPort);
        server.start();
    }

    @Before
    public void beforeTestMethod() {
        webhook.clear();
    }

    /**
     * Creates a HTTPS server to be used during the test methods. The server
     * requires BASIC client authentication and is set up to only trust the
     * users in the security realm file.
     *
     * @param httpsPort
     * @return
     */
    private static Server createBasicAuthServer(int httpsPort) {
        webhook = new WebhookServlet(200);
        ServletDefinition servlet = new ServletDefinition.Builder().servlet(webhook).servletPath("/")
                .requireBasicAuth(true).requireRole("USER").realmFile(SERVER_REALM_FILE).build();
        return ServletServerBuilder.create().httpsPort(httpsPort).sslKeyStoreType(SslKeyStoreType.PKCS12)
                .sslKeyStorePath(SERVER_PKCS12_KEYSTORE).sslKeyStorePassword(SERVER_PKCS12_KEYSTORE_PASSWORD)
                .sslRequireClientCert(false).addServlet(servlet).build();
    }

    @Test
    public void deliverAlert() {

        Alert alert = new Alert("topic1", AlertSeverity.DEBUG, UtcTime.now(), "debug message", null);

        // first make sure that we cannot deliver unauthenticated
        HttpAuthConfig noAuth = new HttpAuthConfig(null, null);
        HttpAlerter noAuthAlerter = new HttpAlerter(new HttpAlerterConfig(asList(webhookUrl()), ".*", noAuth), null);
        noAuthAlerter.handleAlert(alert);
        assertThat(webhook.getReceivedMessages().isEmpty(), is(true));

        // ... also verify that we cannot deliver with wrong credentials
        HttpAuthConfig wrongCredsAuth = new HttpAuthConfig(new BasicCredentials("baduser", "badpass"), null);
        HttpAlerter wrongCredsAlerter = new HttpAlerter(
                new HttpAlerterConfig(asList(webhookUrl()), ".*", wrongCredsAuth), null);
        wrongCredsAlerter.handleAlert(alert);
        assertThat(webhook.getReceivedMessages().isEmpty(), is(true));

        // ... then verify that we can deliver with proper credentials
        HttpAuthConfig rightCredsAuth = new HttpAuthConfig(new BasicCredentials("user", "secret"), null);
        HttpAlerter rightCredsAlerter = new HttpAlerter(
                new HttpAlerterConfig(asList(webhookUrl()), ".*", rightCredsAuth), null);
        rightCredsAlerter.handleAlert(alert);
        assertThat(webhook.getReceivedMessages().size(), is(1));
        assertThat(webhook.getReceivedMessages().get(0), is(alert));
    }

    private String webhookUrl() {
        return String.format("https://localhost:%d/", httpsPort);
    }
}
