package com.elastisys.scale.commons.net.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.jetty.server.Server;
import org.junit.BeforeClass;
import org.junit.Test;

import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;
import com.elastisys.scale.commons.server.ServletDefinition;
import com.elastisys.scale.commons.server.ServletServerBuilder;
import com.elastisys.scale.commons.server.SslKeyStoreType;

/**
 * Tests that exercise the {@link Http} class against a HTTPS server set up to
 * require BASIC client authentication.
 */
public class TestHttpBuilderClientOnServerRequiringBasicAuth {

	// Keystore for a client certificate: even if used, this should be ignored
	// by the test server, which isn't configured to check client certificates
	private static final String CLIENT_PKCS12_KEYSTORE = "src/test/resources/security/untrusted/client_keystore.p12";
	private static final String CLIENT_PKCS12_KEYSTORE_PASSWORD = "untrustedpass";
	private static final CertificateCredentials certCredentials = new CertificateCredentials(
			CLIENT_PKCS12_KEYSTORE, CLIENT_PKCS12_KEYSTORE_PASSWORD,
			CLIENT_PKCS12_KEYSTORE_PASSWORD);

	private static final BasicCredentials trustedPasswordCredentials = new BasicCredentials(
			"user", "secret");

	private static final BasicCredentials untrustedPasswordCredentials = new BasicCredentials(
			"molly", "secretpassword");

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
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new HelloWorldServlet()).servletPath("/")
				.requireBasicAuth(true).realmFile(SERVER_REALM_FILE)
				.requireRole("USER").build();
		return ServletServerBuilder.create().httpsPort(httpsPort)
				.sslKeyStoreType(SslKeyStoreType.PKCS12)
				.sslKeyStorePath(SERVER_PKCS12_KEYSTORE)
				.sslKeyStorePassword(SERVER_PKCS12_KEYSTORE_PASSWORD)
				.sslRequireClientCert(false).addServlet(servlet).build();
	}

	/**
	 * Access with no authentication shoud fail.
	 */
	@Test
	public void nonAuthenticatedClient() throws IOException {
		Http http = Http.builder().build();

		try {
			http.execute(new HttpGet(url("/")));
			fail("unauthenticated client should not have access");
		} catch (HttpResponseException e) {
			assertThat(e.getStatusCode(), is(401));
		}
	}

	/**
	 * Authenticating with an authorized user.
	 */
	@Test
	public void authenticateWithTrustedUser() throws IOException {
		Http http = Http.builder().clientBasicAuth(trustedPasswordCredentials)
				.build();

		HttpRequestResponse response = http.execute(new HttpGet(url("/")));
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.getResponseBody(), is("Hello World!"));
	}

	/**
	 * An unauthorized user should get a {@code 401} (unauthorized) response.
	 */
	@Test
	public void authenticateWithUntrustedUser() throws IOException {
		Http http = Http.builder().clientBasicAuth(untrustedPasswordCredentials)
				.build();
		try {
			http.execute(new HttpGet(url("/")));
		} catch (HttpResponseException e) {
			assertThat(e.getStatusCode(), is(401));
		}
	}

	/**
	 * Make sure the client can connect when supplying *both* BASIC and
	 * certificate credentials. The server, which isn't configured to check the
	 * certificate, should simply ignore the certificate.
	 */
	@Test
	public void authenticateWithBasicAndCertCredentials() throws IOException {
		Http http = Http.builder().clientBasicAuth(trustedPasswordCredentials)
				.clientCertAuth(certCredentials).build();

		HttpRequestResponse response = http.execute(new HttpGet(url("/")));
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.getResponseBody(), is("Hello World!"));

	}

	private String url(String path) {
		return String.format("https://localhost:%d%s", httpsPort, path);
	}
}
