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
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;
import com.elastisys.scale.commons.net.ssl.KeyStoreType;
import com.elastisys.scale.commons.server.ServletDefinition;
import com.elastisys.scale.commons.server.ServletServerBuilder;
import com.elastisys.scale.commons.server.SslKeyStoreType;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Verifies the behavior of the {@link HttpAlerter} when no authentication has
 * been configured.
 */
public class TestHttpAlerterWithCertAuth {
    // Keystore for the client whose certificate is trusted by the test server
    // (that is, the client's certificate is in the server's trust store).
    private static final String TRUSTED_CLIENT_PKCS12_KEYSTORE = "src/test/resources/security/client/client_keystore.p12";
    private static final String TRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD = "clientpass";
    // Keystore for the client that is not trusted by the test server
    private static final String UNTRUSTED_CLIENT_PKCS12_KEYSTORE = "src/test/resources/security/untrusted/client_keystore.p12";
    private static final String UNTRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD = "untrustedpass";
    // Server keystore set up to only trust the trusted client's certificate
    private static final String SERVER_PKCS12_KEYSTORE = "src/test/resources/security/server/server_keystore.p12";
    private static final String SERVER_TRUSTSTORE = "src/test/resources/security/server/server_truststore.jks";
    private static final String SERVER_PKCS12_KEYSTORE_PASSWORD = "serverpassword";
    private static final String SERVER_TRUSTSTORE_PASSWORD = "servertrustpass";

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
        server = createCertAuthServer(httpsPort);
        server.start();
    }

    @Before
    public void beforeTestMethod() {
        webhook.clear();
    }

    /**
     * Creates a HTTPS server to be used during the test methods. The server
     * requires client cert authentication and is set up to only trust the
     * certificate of the "trusted client".
     *
     * @param httpsPort
     * @return
     */
    private static Server createCertAuthServer(int httpsPort) {
        webhook = new WebhookServlet(200);
        ServletDefinition servlet = new ServletDefinition.Builder().servlet(webhook).servletPath("/")
                .requireBasicAuth(false).build();
        return ServletServerBuilder.create().httpsPort(httpsPort).sslKeyStoreType(SslKeyStoreType.PKCS12)
                .sslKeyStorePath(SERVER_PKCS12_KEYSTORE).sslKeyStorePassword(SERVER_PKCS12_KEYSTORE_PASSWORD)
                .sslTrustStorePath(SERVER_TRUSTSTORE).sslTrustStorePassword(SERVER_TRUSTSTORE_PASSWORD)
                .sslTrustStoreType(SslKeyStoreType.JKS).sslRequireClientCert(true).addServlet(servlet).build();
    }

    @Test
    public void deliverAlert() {

        Alert alert = new Alert("topic1", AlertSeverity.DEBUG, UtcTime.now(), "debug message", null);

        // first make sure that we cannot deliver unauthenticated
        HttpAuthConfig noAuth = new HttpAuthConfig(null, null);
        HttpAlerter noAuthAlerter = new HttpAlerter(new HttpAlerterConfig(asList(webhookUrl()), ".*", noAuth), null);
        noAuthAlerter.handleAlert(alert);
        assertThat(webhook.getReceivedMessages().isEmpty(), is(true));

        // ... also verify that we cannot deliver with basic auth credentials
        HttpAuthConfig basicAuth = new HttpAuthConfig(new BasicCredentials("user", "secret"), null);
        HttpAlerter basicAuthAlerter = new HttpAlerter(new HttpAlerterConfig(asList(webhookUrl()), ".*", basicAuth),
                null);
        basicAuthAlerter.handleAlert(alert);
        assertThat(webhook.getReceivedMessages().isEmpty(), is(true));

        // ... also verify that we cannot deliver with wrong cert credentials
        HttpAuthConfig wrongCertAuth = new HttpAuthConfig(null,
                new CertificateCredentials(KeyStoreType.PKCS12, UNTRUSTED_CLIENT_PKCS12_KEYSTORE,
                        UNTRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD, UNTRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD));
        HttpAlerter wrongCertAlerter = new HttpAlerter(new HttpAlerterConfig(asList(webhookUrl()), ".*", wrongCertAuth),
                null);
        wrongCertAlerter.handleAlert(alert);
        assertThat(webhook.getReceivedMessages().isEmpty(), is(true));

        // ... then verify that we can deliver with proper credentials
        HttpAuthConfig rightCertAuth = new HttpAuthConfig(null,
                new CertificateCredentials(KeyStoreType.PKCS12, TRUSTED_CLIENT_PKCS12_KEYSTORE,
                        TRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD, TRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD));
        HttpAlerter rightCertAlerter = new HttpAlerter(new HttpAlerterConfig(asList(webhookUrl()), ".*", rightCertAuth),
                null);
        rightCertAlerter.handleAlert(alert);
        assertThat(webhook.getReceivedMessages().size(), is(1));
        assertThat(webhook.getReceivedMessages().get(0), is(alert));
    }

    private String webhookUrl() {
        return String.format("https://localhost:%d/", httpsPort);
    }
}
