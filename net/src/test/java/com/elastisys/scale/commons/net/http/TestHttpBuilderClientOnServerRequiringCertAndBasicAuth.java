package com.elastisys.scale.commons.net.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;
import com.elastisys.scale.commons.net.ssl.KeyStoreType;
import com.elastisys.scale.commons.server.ServletDefinition;
import com.elastisys.scale.commons.server.ServletServerBuilder;
import com.elastisys.scale.commons.server.SslKeyStoreType;

/**
 * Tests that exercise the {@link Http} class against a HTTPS server set up to
 * require both client certificate authentication <b>and</b> BASIC
 * authentication.
 */
public class TestHttpBuilderClientOnServerRequiringCertAndBasicAuth {

	// Keystore for the client whose certificate is trusted by the test server
	// (that is, the client's certificate is in the server's trust store).
	private static final String TRUSTED_CLIENT_JKS_KEYSTORE = "src/test/resources/security/client/client_keystore.jks";
	private static final String TRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD = "clientpass";
	private static final String TRUSTED_CLIENT_JKS_KEYSTORE_PASSWORD = "jksclientpass";
	private static final CertificateCredentials trustedClientJksCert = new CertificateCredentials(
			KeyStoreType.JKS, TRUSTED_CLIENT_JKS_KEYSTORE,
			TRUSTED_CLIENT_JKS_KEYSTORE_PASSWORD,
			TRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD);

	// Keystore for the client that is not trusted by the test server
	private static final String UNTRUSTED_CLIENT_PKCS12_KEYSTORE = "src/test/resources/security/untrusted/client_keystore.p12";
	private static final String UNTRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD = "untrustedpass";
	private static final CertificateCredentials untrustedClientPkcs12Cert = new CertificateCredentials(
			UNTRUSTED_CLIENT_PKCS12_KEYSTORE,
			UNTRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD,
			UNTRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD);

	/** Correct password credentials. */
	private static final BasicCredentials trustedPasswordCredentials = new BasicCredentials(
			"user", "secret");
	/** Incorrect password credentials. */
	private static final BasicCredentials untrustedPasswordCredentials = new BasicCredentials(
			"user", "badpassword");

	// Server keystore set up to only trust the trusted client's certificate
	private static final String SERVER_PKCS12_KEYSTORE = "src/test/resources/security/server/server_keystore.p12";
	private static final String SERVER_TRUSTSTORE = "src/test/resources/security/server/server_truststore.jks";
	private static final String SERVER_PKCS12_KEYSTORE_PASSWORD = "serverpassword";
	private static final String SERVER_TRUSTSTORE_PASSWORD = "servertrustpass";
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
	 * requires client cert authentication <b>and</b> BASIC authentication. It
	 * is set up to only trust the certificate of the "trusted client" and
	 * requires BASIC authentication credentials that match those in the
	 * security realm file.
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
				.sslTrustStorePath(SERVER_TRUSTSTORE)
				.sslTrustStorePassword(SERVER_TRUSTSTORE_PASSWORD)
				.sslTrustStoreType(SslKeyStoreType.JKS)
				.sslRequireClientCert(true).addServlet(servlet).build();
	}

	@AfterClass
	public static void afterTests() throws Exception {
		if (server != null) {
			server.stop();
			server.join();
		}
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
		} catch (SocketException | SSLException e) {
			// expected
		}
	}

	/**
	 * Connect with a trusted client certificate and the right
	 * username/password. Should succeed.
	 */
	@Test
	public void correctCertAndPassword() throws IOException {
		Http http = Http.builder().clientBasicAuth(trustedPasswordCredentials)
				.clientCertAuth(trustedClientJksCert).build();

		HttpRequestResponse response = http.execute(new HttpGet(url("/")));
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.getResponseBody(), is("Hello World!"));
	}

	/**
	 * Connect with an untrusted client certificate and correct
	 * username/password. Should fail to establish an SSL connection.
	 */
	@Test
	public void badCertAndCorrectPassword() throws IOException {
		Http http = Http.builder().clientBasicAuth(trustedPasswordCredentials)
				.clientCertAuth(untrustedClientPkcs12Cert).build();

		try {
			http.execute(new HttpGet(url("/")));
			fail("call with untrusted cert should not succeed");
		} catch (SocketException | SSLException e) {
			// expected
		}
	}

	/**
	 * Connect with a trusted client certificate and incorrect
	 * username/password. Should fail to access servlet resource.
	 */
	@Test
	public void correctCertAndBadPassword() throws IOException {
		Http http = Http.builder().clientBasicAuth(untrustedPasswordCredentials)
				.clientCertAuth(trustedClientJksCert).build();

		try {
			http.execute(new HttpGet(url("/")));
		} catch (HttpResponseException e) {
			assertThat(e.getStatusCode(), is(401));
		}
	}

	/**
	 * Connect with an untrusted client certificate and incorrect
	 * username/password. Should fail to establish an SSL connection.
	 */
	@Test
	public void badCertAndBadPassword() throws IOException {
		Http http = Http.builder().clientBasicAuth(untrustedPasswordCredentials)
				.clientCertAuth(untrustedClientPkcs12Cert).build();

		try {
			http.execute(new HttpGet(url("/")));
			fail("call with untrusted cert should not succeed");
		} catch (SocketException | SSLException e) {
			// expected
		}
	}

	private String url(String path) {
		return String.format("https://localhost:%d%s", httpsPort, path);
	}
}
