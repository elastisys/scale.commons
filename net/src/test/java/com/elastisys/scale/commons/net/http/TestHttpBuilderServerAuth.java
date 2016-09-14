package com.elastisys.scale.commons.net.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.SocketException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.jetty.server.Server;
import org.junit.BeforeClass;
import org.junit.Test;

import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.net.ssl.KeyStoreType;
import com.elastisys.scale.commons.server.ServletDefinition;
import com.elastisys.scale.commons.server.ServletServerBuilder;
import com.elastisys.scale.commons.server.SslKeyStoreType;

/**
 * Tests that exercise the {@link HttpBuilder} when it comes to creating
 * {@link Http} clients that verify the server's host certificate.
 */
public class TestHttpBuilderServerAuth {

    // Keystore for the trusted server
    private static final String TRUSTED_SERVER_KEYSTORE = "src/test/resources/security/server/server_keystore.p12";
    private static final String TRUSTED_SERVER_KEYSTORE_PASSWORD = "serverpassword";

    // Keystore for the untrusted server
    private static final String UNTRUSTED_SERVER_KEYSTORE = "src/test/resources/security/untrusted_server/server_keystore.p12";
    private static final String UNTRUSTED_SERVER_KEYSTORE_PASSWORD = "untrustedpass";

    // Trust store for for the client, which trusts the trusted server
    // certificate
    private static final String CLIENT_TRUST_STORE = "src/test/resources/security/client/client_truststore.jks";
    private static final String CLIENT_TRUST_STORE_PASSWORD = "clienttrust";

    /** HTTPS port to use for test servers. */
    private static Integer httpsPort;

    @BeforeClass
    public static void beforeClass() {
        httpsPort = HostUtils.findFreePorts(1).get(0);
    }

    /**
     * Build a {@link Http} client that authenticates server certificates with
     * the default (JVM-provided) trust store, and make sure that a client call
     * to a server with a self-signed certificate fails.
     */
    @Test
    public void verifyInvalidHostCertWithDefaultTrustStore() throws Exception {
        Server server = startHttpsServer(httpsPort, TRUSTED_SERVER_KEYSTORE, TRUSTED_SERVER_KEYSTORE_PASSWORD);

        try {
            Http http = Http.builder().verifyHostCert(true).verifyHostname(false).build();
            try {
                http.execute(new HttpGet(url("/")));
                fail("should not allow connection to server with self-signed cert");
            } catch (SocketException | SSLHandshakeException e) {
                // expected
            }
        } finally {
            server.stop();
        }
    }

    /**
     * Build a {@link Http} client that authenticates server certificates with a
     * custom trust store, and make sure that a client call to a trusted server
     * succeeds.
     */
    @Test
    public void verifyValidHostCertWithCustomTrustStore() throws Exception {
        Server server = startHttpsServer(httpsPort, TRUSTED_SERVER_KEYSTORE, TRUSTED_SERVER_KEYSTORE_PASSWORD);

        try {
            Http http = Http.builder().verifyHostCert(true).verifyHostname(false)
                    .serverAuthTrustStore(KeyStoreType.JKS, CLIENT_TRUST_STORE, CLIENT_TRUST_STORE_PASSWORD).build();
            HttpRequestResponse response = http.execute(new HttpGet(url("/")));
            assertThat(response.getStatusCode(), is(HttpStatus.SC_OK));
        } finally {
            server.stop();
        }
    }

    /**
     * Build a {@link Http} client that authenticates server certificates with a
     * custom trust store, and make sure that a client call to an untrusted
     * server fails.
     */
    @Test
    public void verifyInvalidHostCertWithCustomTrustStore() throws Exception {
        Server server = startHttpsServer(httpsPort, UNTRUSTED_SERVER_KEYSTORE, UNTRUSTED_SERVER_KEYSTORE_PASSWORD);

        try {
            Http http = Http.builder().verifyHostCert(true).verifyHostname(false)
                    .serverAuthTrustStore(KeyStoreType.JKS, CLIENT_TRUST_STORE, CLIENT_TRUST_STORE_PASSWORD).build();
            try {
                http.execute(new HttpGet(url("/")));
                fail("should not allow connection to untrusted server");
            } catch (SocketException | SSLHandshakeException e) {
                // expected
            }
        } finally {
            server.stop();
        }
    }

    /**
     * Build a {@link Http} client that verifies the hostname of the server,
     * which should fail for a server with a self-signed certificate.
     */
    @Test
    public void verifyHostName() throws Exception {
        Server server = startHttpsServer(httpsPort, TRUSTED_SERVER_KEYSTORE, TRUSTED_SERVER_KEYSTORE_PASSWORD);

        try {
            Http http = Http.builder().verifyHostCert(false).verifyHostname(true).build();
            try {
                http.execute(new HttpGet(url("/")));
                fail("should not allow connection to server when hostname verification is enabled");
            } catch (SSLPeerUnverifiedException e) {
                // expected
            }
        } finally {
            server.stop();
        }
    }

    /**
     * Creates a HTTPS server with a host certificate in a given key store.
     *
     * @param httpsPort
     * @return
     */
    private static Server startHttpsServer(int httpsPort, String keystorePath, String keystorePassword)
            throws Exception {
        ServletDefinition servlet = new ServletDefinition.Builder().servlet(new HelloWorldServlet()).servletPath("/")
                .build();
        Server server = ServletServerBuilder.create().httpsPort(httpsPort).sslKeyStoreType(SslKeyStoreType.PKCS12)
                .sslKeyStorePath(keystorePath).sslKeyStorePassword(keystorePassword).sslRequireClientCert(false)
                .addServlet(servlet).build();
        server.start();
        return server;
    }

    private String url(String path) {
        return String.format("https://localhost:%d%s", httpsPort, path);
    }

}
