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
import com.elastisys.scale.commons.server.ServletDefinition;
import com.elastisys.scale.commons.server.ServletServerBuilder;
import com.elastisys.scale.commons.server.SslKeyStoreType;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Verifies the behavior of the {@link HttpAlerter} when no authentication has
 * been configured.
 */
public class TestHttpAlerterWithBasicAndCertAuth {
	// Keystore for the client whose certificate is trusted by the test server
	// (that is, the client's certificate is in the server's trust store).
	private static final String TRUSTED_CLIENT_PKCS12_KEYSTORE = "src/test/resources/security/client/client_keystore.p12";
	private static final String TRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD = "clientpass";
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
	/** The web hook that receives all alerts. */
	private static WebhookServlet webhook;

	@BeforeClass
	public static void beforeTests() throws Exception {
		// find a free port for test server
		List<Integer> freePorts = HostUtils.findFreePorts(1);
		httpsPort = freePorts.get(0);
		// server instances are created by each individual test method
		server = createCertAndBasicAuthServer(httpsPort);
		server.start();
	}

	@Before
	public void beforeTestMethod() {
		webhook.clear();
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
	private static Server createCertAndBasicAuthServer(int httpsPort) {
		webhook = new WebhookServlet(200);
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(webhook).servletPath("/").requireBasicAuth(true)
				.realmFile(SERVER_REALM_FILE).requireRole("USER").build();
		return ServletServerBuilder.create().httpsPort(httpsPort)
				.sslKeyStoreType(SslKeyStoreType.PKCS12)
				.sslKeyStorePath(SERVER_PKCS12_KEYSTORE)
				.sslKeyStorePassword(SERVER_PKCS12_KEYSTORE_PASSWORD)
				.sslTrustStorePath(SERVER_TRUSTSTORE)
				.sslTrustStorePassword(SERVER_TRUSTSTORE_PASSWORD)
				.sslTrustStoreType(SslKeyStoreType.JKS)
				.sslRequireClientCert(true).addServlet(servlet).build();
	}

	@Test
	public void deliverAlert() {

		Alert alert = new Alert("topic1", AlertSeverity.DEBUG, UtcTime.now(),
				"debug message");

		// first make sure that we cannot deliver unauthenticated
		HttpAuthConfig noAuth = new HttpAuthConfig(null, null);
		HttpAlerter noAuthAlerter = new HttpAlerter(new HttpAlerterConfig(
				asList(webhookUrl()), ".*", noAuth), null);
		noAuthAlerter.handleAlert(alert);
		assertThat(webhook.getReceivedMessages().isEmpty(), is(true));

		// ... also verify that we cannot deliver with only basic auth
		// credentials
		HttpAuthConfig basicAuth = new HttpAuthConfig(new BasicCredentials("user",
				"secret"), null);
		HttpAlerter basicAuthAlerter = new HttpAlerter(new HttpAlerterConfig(
				asList(webhookUrl()), ".*", basicAuth), null);
		basicAuthAlerter.handleAlert(alert);
		assertThat(webhook.getReceivedMessages().isEmpty(), is(true));

		// ... also verify that we cannot deliver with only cert credentials
		HttpAuthConfig certAuth = new HttpAuthConfig(null, new CertificateCredentials(
				TRUSTED_CLIENT_PKCS12_KEYSTORE,
				TRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD));
		HttpAlerter certAlerter = new HttpAlerter(new HttpAlerterConfig(
				asList(webhookUrl()), ".*", certAuth), null);
		certAlerter.handleAlert(alert);
		assertThat(webhook.getReceivedMessages().isEmpty(), is(true));

		// ... then verify that we can deliver with both basic and cert
		// credentials
		HttpAuthConfig basicAndCertAuth = new HttpAuthConfig(new BasicCredentials(
				"user", "secret"), new CertificateCredentials(
				TRUSTED_CLIENT_PKCS12_KEYSTORE,
				TRUSTED_CLIENT_PKCS12_KEYSTORE_PASSWORD));
		HttpAlerter basicAndCertAlerter = new HttpAlerter(
				new HttpAlerterConfig(asList(webhookUrl()), ".*",
						basicAndCertAuth), null);
		basicAndCertAlerter.handleAlert(alert);
		assertThat(webhook.getReceivedMessages().size(), is(1));
		assertThat(webhook.getReceivedMessages().get(0), is(alert));
	}

	private String webhookUrl() {
		return String.format("https://localhost:%d/", httpsPort);
	}
}
