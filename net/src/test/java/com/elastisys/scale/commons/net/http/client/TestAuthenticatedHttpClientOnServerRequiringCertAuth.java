package com.elastisys.scale.commons.net.http.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.client.methods.HttpGet;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
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
 * Tests that exercise the {@link AuthenticatedHttpClient} class against a HTTPS
 * server set up to require client certificate authentication.
 */
public class TestAuthenticatedHttpClientOnServerRequiringCertAuth {

	// Keystore for the client whose certificate is trusted by the test server
	// (that is, the client's certificate is in the server's trust store).
	private static final String TRUSTED_CLIENT_PKCS12_KEYSTORE = "src/test/resources/security/client/client_keystore.p12";
	private static final String TRUSTED_CLIENT_JKS_KEYSTORE = "src/test/resources/security/client/client_keystore.jks";
	private static final String TRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD = "clientpass";
	private static final String TRUSTED_CLIENT_JKS_KEYSTORE_PASSWORD = "jksclientpass";
	private static final CertificateCredentials trustedClientPkcs12Cert = new CertificateCredentials(
			KeyStoreType.PKCS12, TRUSTED_CLIENT_PKCS12_KEYSTORE,
			TRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD);
	private static final CertificateCredentials trustedClientJksCert = new CertificateCredentials(
			KeyStoreType.JKS, TRUSTED_CLIENT_JKS_KEYSTORE,
			TRUSTED_CLIENT_JKS_KEYSTORE_PASSWORD,
			TRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD);

	// Keystore for the client that is not trusted by the test server
	private static final String UNTRUSTED_CLIENT_PKCS12_KEYSTORE = "src/test/resources/security/untrusted/client_keystore.p12";
	private static final String UNTRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD = "untrustedpass";
	private static final String UNTRUSTED_CLIENT_JKS_KEYSTORE = "src/test/resources/security/untrusted/client_keystore.jks";
	private static final String UNTRUSTED_CLIENT_JKS_KEYSTORE_PASSWORD = "untrustedjkspass";
	private static final CertificateCredentials untrustedClientPkcs12Cert = new CertificateCredentials(
			KeyStoreType.PKCS12, UNTRUSTED_CLIENT_PKCS12_KEYSTORE,
			UNTRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD);
	private static final CertificateCredentials untrustedClientJksCert = new CertificateCredentials(
			KeyStoreType.JKS, UNTRUSTED_CLIENT_JKS_KEYSTORE,
			UNTRUSTED_CLIENT_JKS_KEYSTORE_PASSWORD,
			UNTRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD);

	// Server keystore set up to only trust the trusted client's certificate
	private static final String SERVER_PKCS12_KEYSTORE = "src/test/resources/security/server/server_keystore.p12";
	private static final String SERVER_TRUSTSTORE = "src/test/resources/security/server/server_truststore.jks";
	private static final String SERVER_PKCS12_KEYSTORE_PASSWORD = "serverpassword";
	private static final String SERVER_TRUSTSTORE_PASSWORD = "servertrustpass";

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
	 * requires client cert authentication and is set up to only trust the
	 * certificate of the "trusted client".
	 *
	 * @param httpsPort
	 * @return
	 */
	private static Server createHttpsServer(int httpsPort) {
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new HelloWorldServlet()).servletPath("/")
				.requireBasicAuth(false).build();
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
		AuthenticatedHttpClient client = new AuthenticatedHttpClient();

		try {
			client.execute(new HttpGet(url("/")));
			fail("unauthenticated client should not have access");
		} catch (SocketException | SSLException e) {
			// expected
		}
	}

	/**
	 * Make sure the trusted client can connect with a certificate read from a
	 * PKCS12 key store.
	 */
	@Test
	public void callWithTrustedClientPkcs12Cert() throws IOException {
		Optional<BasicCredentials> absent = Optional.absent();
		AuthenticatedHttpClient client = new AuthenticatedHttpClient(absent,
				Optional.of(trustedClientPkcs12Cert));

		HttpRequestResponse response = client.execute(new HttpGet(url("/")));
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.getResponseBody(), is("Hello World!"));
	}

	/**
	 * Make sure the trusted client can connect with a certificate read from a
	 * JKS key store.
	 */
	@Test
	public void callWithTrustedClientJksCert() throws IOException {
		Optional<BasicCredentials> absent = Optional.absent();
		AuthenticatedHttpClient client = new AuthenticatedHttpClient(absent,
				Optional.of(trustedClientJksCert));

		HttpRequestResponse response = client.execute(new HttpGet(url("/")));
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.getResponseBody(), is("Hello World!"));
	}

	/**
	 * Make sure the untrusted client can <b>not</b> connect with a certificate
	 * read from a PKCS12 key store.
	 */
	@Test(expected = SocketException.class)
	public void callWithUntrustedClientPkcs12Cert() throws IOException {
		Optional<BasicCredentials> absent = Optional.absent();
		AuthenticatedHttpClient client = new AuthenticatedHttpClient(absent,
				Optional.of(untrustedClientPkcs12Cert));

		client.execute(new HttpGet(url("/")));
	}

	/**
	 * Make sure the untrusted client can <b>not</b> connect with a certificate
	 * read from a JKS key store.
	 */
	@Test(expected = SocketException.class)
	public void callWithUntrustedClientJksCert() throws IOException {
		Optional<BasicCredentials> absent = Optional.absent();
		AuthenticatedHttpClient client = new AuthenticatedHttpClient(absent,
				Optional.of(untrustedClientJksCert));

		client.execute(new HttpGet(url("/")));
	}

	/**
	 * Make sure a client that doesn't include a certificate can <b>not</b>
	 * connect.
	 */
	@Test(expected = SSLHandshakeException.class)
	public void callWithoutClientCertificate() throws IOException {
		BasicCredentials basicCredentials = new BasicCredentials("johndoe",
				"secret");
		Optional<CertificateCredentials> absent = Optional.absent();
		AuthenticatedHttpClient client = new AuthenticatedHttpClient(
				Optional.of(basicCredentials), absent);

		client.execute(new HttpGet(url("/")));
	}

	private String url(String path) {
		return String.format("https://localhost:%d%s", httpsPort, path);
	}
}
