package com.elastisys.scale.commons.net.http.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.eclipse.jetty.server.Server;
import org.junit.BeforeClass;
import org.junit.Test;

import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.net.http.HttpRequestResponse;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;
import com.elastisys.scale.commons.server.ServletDefinition;
import com.elastisys.scale.commons.server.ServletServerBuilder;
import com.elastisys.scale.commons.server.SslKeyStoreType;
import com.google.common.base.Optional;

/**
 * Exercises the response handling of the {@link AuthenticatedHttpClient} class.
 * Requests with 2XX responses should be accepted whereas requests with other
 * response codes should raise {@link HttpResponseException}s.
 */
public class TestAuthenticatedClientResponseHandling {

    // Server keystore set up to only trust the trusted client's certificate
    private static final String SERVER_PKCS12_KEYSTORE = "src/test/resources/security/server/server_keystore.p12";
    private static final String SERVER_PKCS12_KEYSTORE_PASSWORD = "serverpassword";
    private static final String SERVER_REALM_FILE = "src/test/resources/security/server/security-realm.properties";

    /** The port where a HTTPS server is set up. */
    private static Integer httpsPort;
    /** Dummy HTTPS server. */
    private static Server server;

    @BeforeClass
    public static void beforeTests() throws Exception {
        // find a free port for test server
        List<Integer> freePorts = HostUtils.findFreePorts(1);
        httpsPort = freePorts.get(0);
        // server instances are created by each individual test method
        server = createHttpsServer(httpsPort);
        server.start();
    }

    /**
     * Creates a HTTPS server to be used during the test methods. The server
     * requires BASIC client authentication and is set up to only trust the
     * users in the security realm file.
     *
     * @param httpsPort
     * @return
     */
    private static Server createHttpsServer(int httpsPort) {
        ServletDefinition helloServlet = new ServletDefinition.Builder().servlet(new HelloWorldServlet())
                .servletPath("/hello").requireBasicAuth(true).realmFile(SERVER_REALM_FILE).requireRole("USER").build();
        ServletDefinition silentServlet = new ServletDefinition.Builder().servlet(new SilentServlet())
                .servletPath("/silent").requireBasicAuth(true).realmFile(SERVER_REALM_FILE).requireRole("USER").build();

        return ServletServerBuilder.create().httpsPort(httpsPort).sslKeyStoreType(SslKeyStoreType.PKCS12)
                .sslKeyStorePath(SERVER_PKCS12_KEYSTORE).sslKeyStorePassword(SERVER_PKCS12_KEYSTORE_PASSWORD)
                .sslRequireClientCert(false).addServlet(helloServlet).addServlet(silentServlet).build();
    }

    /**
     * Verify that a valid GET request gives a 200 response.
     */
    @Test
    public void getRequestWith200Response() throws IOException {
        HttpRequestResponse response = client("user", "secret").execute(new HttpGet(url("/hello")));
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.getResponseBody(), is("Hello World!"));
    }

    /**
     * Verify that a valid POST request gives a 200 response.
     */
    @Test
    public void postRequestWith200Response() throws IOException {
        HttpPost postRequest = new HttpPost(url("/hello"));
        postRequest.setEntity(new StringEntity("{\"a\": 1}", ContentType.APPLICATION_JSON));
        HttpRequestResponse response = client("user", "secret").execute(postRequest);
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.getResponseBody(), is("Hello World!"));
    }

    /**
     * The client should be able to handle 204 (No Content) responses without a
     * response body.
     */
    @Test
    public void getRequestWith204ResponseAndNoMessageBody() throws IOException {
        HttpRequestResponse response = client("user", "secret").execute(new HttpGet(url("/silent")));
        assertThat(response.getStatusCode(), is(204));
        assertThat(response.getResponseBody(), is(nullValue()));
    }

    /**
     * The client should be able to handle 204 (No Content) responses without a
     * response body.
     */
    @Test
    public void postRequestWith204ResponseAndNoMessageBody() throws IOException {
        HttpPost postRequest = new HttpPost(url("/silent"));
        postRequest.setEntity(new StringEntity("{\"a\": 1}", ContentType.APPLICATION_JSON));
        HttpRequestResponse response = client("user", "secret").execute(postRequest);
        assertThat(response.getStatusCode(), is(204));
        assertThat(response.getResponseBody(), is(nullValue()));
    }

    /**
     * Verify that a {@link HttpResponseException} is raised when user
     * authentication fails (401 - Unauthorized).
     */
    @Test
    public void getRequestWith401Response() throws IOException {
        try {
            client("user", "wrongpassword").execute(new HttpGet(url("/hello")));
        } catch (HttpResponseException e) {
            assertThat(e.getStatusCode(), is(401));
        }

        try {
            client("wronguser", "secret").execute(new HttpGet(url("/hello")));
        } catch (HttpResponseException e) {
            assertThat(e.getStatusCode(), is(401));
        }
    }

    /**
     * Verify that a {@link HttpResponseException} is raised when requesting a
     * resource that doesn't exist (404 - Not Found).
     */
    @Test
    public void getRequestWith404Response() throws IOException {
        try {
            client("user", "secret").execute(new HttpGet(url("/illegal/path")));
        } catch (HttpResponseException e) {
            assertThat(e.getStatusCode(), is(404));
        }
    }

    private String url(String path) {
        return String.format("https://localhost:%d%s", httpsPort, path);
    }

    private AuthenticatedHttpClient client(String username, String password) {
        Optional<CertificateCredentials> absent = Optional.absent();
        AuthenticatedHttpClient client = new AuthenticatedHttpClient(
                Optional.of(new BasicCredentials(username, password)), absent);
        return client;
    }
}
