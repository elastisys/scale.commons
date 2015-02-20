package com.elastisys.scale.commons.net.http.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.eclipse.jetty.server.Server;
import org.junit.BeforeClass;
import org.junit.Test;

import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.net.http.HttpRequestResponse;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;
import com.elastisys.scale.commons.net.ssl.KeyStoreType;
import com.elastisys.scale.commons.server.ServletDefinition;
import com.elastisys.scale.commons.server.ServletServerBuilder;
import com.elastisys.scale.commons.server.SslKeyStoreType;
import com.google.common.base.Optional;

/**
 * Tests the {@link AuthenticatedHttpClient} class against a HTTPS server set up
 * to require no client authentication.
 */
public class TestAuthenticatedHttpClientOnServerRequiringNoAuth {

	// Keystore for a client certificate: even if used, this should be ignored
	// by the test server, which isn't configured to check client certificates
	private static final String CLIENT_PKCS12_KEYSTORE = "src/test/resources/security/untrusted/client_keystore.p12";
	private static final String CLIENT_PKCS12_KEYSTORE_PASSWORD = "untrustedpass";
	private static final CertificateCredentials certCredentials = new CertificateCredentials(
			KeyStoreType.PKCS12, CLIENT_PKCS12_KEYSTORE,
			CLIENT_PKCS12_KEYSTORE_PASSWORD);

	private static final BasicCredentials trustedPasswordCredentials = new BasicCredentials(
			"user", "secret");

	// Server keystore set up to only trust the trusted client's certificate
	private static final String SERVER_PKCS12_KEYSTORE = "src/test/resources/security/server/server_keystore.p12";
	private static final String SERVER_PKCS12_KEYSTORE_PASSWORD = "serverpassword";

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
	 * requires no client authentication.
	 *
	 * @param httpsPort
	 * @return
	 */
	private static Server createHttpsServer(int httpsPort) {
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new HelloWorldServlet()).servletPath("/")
				.requireBasicAuth(false).requireRole("USER").build();
		return ServletServerBuilder.create().httpsPort(httpsPort)
				.sslKeyStoreType(SslKeyStoreType.PKCS12)
				.sslKeyStorePath(SERVER_PKCS12_KEYSTORE)
				.sslKeyStorePassword(SERVER_PKCS12_KEYSTORE_PASSWORD)
				.sslRequireClientCert(false).addServlet(servlet).build();
	}

	/**
	 * Access with no authentication.
	 */
	@Test
	public void nonAuthenticatedClient() throws IOException {
		AuthenticatedHttpClient client = new AuthenticatedHttpClient();

		HttpRequestResponse response = client.execute(new HttpGet(url("/")));
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.getResponseBody(), is("Hello World!"));
	}

	/**
	 * Authenticating with basic authentication (should work, server doesn't
	 * care).
	 */
	@Test
	public void basicCredentialsClient() throws IOException {
		Optional<CertificateCredentials> absent = Optional.absent();
		AuthenticatedHttpClient client = new AuthenticatedHttpClient(
				Optional.of(trustedPasswordCredentials), absent);

		HttpRequestResponse response = client.execute(new HttpGet(url("/")));
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.getResponseBody(), is("Hello World!"));
	}

	/**
	 * Authenticating with cert authentication (should work, server doesn't
	 * care).
	 */
	@Test
	public void certCredentialsClient() throws IOException {
		Optional<BasicCredentials> absent = Optional.absent();
		AuthenticatedHttpClient client = new AuthenticatedHttpClient(absent,
				Optional.of(certCredentials));

		HttpRequestResponse response = client.execute(new HttpGet(url("/")));
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.getResponseBody(), is("Hello World!"));
	}

	/**
	 * Make sure the client can connect when supplying *both* BASIC and
	 * certificate credentials. The server should simply ignore all
	 * authentication credentials.
	 */
	@Test
	public void authenticateWithBasicAndCertCredentials() throws IOException {
		AuthenticatedHttpClient client = new AuthenticatedHttpClient(
				Optional.of(trustedPasswordCredentials),
				Optional.of(certCredentials));

		HttpRequestResponse response = client.execute(new HttpGet(url("/")));
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.getResponseBody(), is("Hello World!"));
	}

	private String url(String path) {
		return String.format("https://localhost:%d%s", httpsPort, path);
	}
}
