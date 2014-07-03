package com.elastisys.scale.commons.server;

import java.net.ConnectException;
import java.net.SocketException;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.net.ssl.KeyStoreType;
import com.google.common.io.Resources;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.CoreMatchers.is;

/**
 * Exercises the {@link ServletServerBuilder}.
 * 
 * 
 * 
 */
public class TestServletServerBuilder {
	static Logger logger = LoggerFactory
			.getLogger(TestServletServerBuilder.class);

	private static final String SERVER_KEYSTORE = Resources.getResource(
			"security/server/server_keystore.p12").toString();
	private static final String SERVER_KEYSTORE_PASSWORD = "serverpass";

	private static final String SERVER_TRUSTSTORE = Resources.getResource(
			"security/server/server_truststore.jks").toString();
	private static final String SERVER_TRUSTSTORE_PASSWORD = "trustpass";

	private static final String CLIENT_KEYSTORE = "src/test/resources/security/client/client_keystore.p12";
	private static final String CLIENT_KEYSTORE_PASSWORD = "clientpass";

	private static final String UNTRUSTED_CLIENT_KEYSTORE = "src/test/resources/security/untrusted/untrusted_keystore.p12";
	private static final String UNTRUSTED_CLIENT_KEYSTORE_PASSWORD = "untrustedpass";

	private static final String SECURITY_REALM_FILE = "src/test/resources/security/server/security-realm.properties";

	/** The {@link Server} instance under test. */
	private Server server;
	/** HTTP port to use for the server in a test. */
	private int httpPort;
	/** HTTPS port to use for the server in a test. */
	private int httpsPort;

	@Before
	public void onSetup() {
		List<Integer> freePorts = HostUtils.findFreePorts(2);
		this.httpPort = freePorts.get(0);
		this.httpsPort = freePorts.get(1);

		// server instances are created by each individual test method
		this.server = null;
	}

	/**
	 * Tears down the {@link Server} instance (if any) created by the test.
	 */
	@After
	public void onTeardown() throws Exception {
		if (this.server != null) {
			this.server.stop();
			this.server.join();
		}
	}

	/**
	 * test deploying on servlet path other than {@code /}
	 * 
	 * @throws Exception
	 */
	@Test
	public void specifyServletPath() throws Exception {
		String contextPath = "/servlet";
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new EchoServlet()).servletPath(contextPath).build();
		this.server = ServletServerBuilder.create().httpPort(this.httpPort)
				.addServlet(servlet).build();
		this.server.start();

		// try accessing servlet at path "/": should fail
		String rootUrl = httpUrl("/");
		Client noAuthClient = RestTestUtils.httpsNoAuth();
		Response response = noAuthClient.target(rootUrl).request().get();
		assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

		// access servlet where it's "mounted" ("/servlet"): should succeed
		String url = rootUrl + "servlet";
		response = noAuthClient.target(url).request().get();
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

		url = rootUrl + "servlet/sub/resource";
		response = noAuthClient.target(url).request().get();
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
	}

	@Test
	public void httpNoSecurityServer() throws Exception {
		ServletDefinition servlet = new ServletDefinition.Builder().servlet(
				new EchoServlet()).build();
		this.server = ServletServerBuilder.create().httpPort(this.httpPort)
				.addServlet(servlet).build();
		this.server.start();

		// test connecting on http port
		assertThat(httpNoAuth().getStatus(), is(Status.OK.getStatusCode()));
		// test connecting on https port: should fail
		try {
			httpsNoAuth();
		} catch (ProcessingException e) {
			assertThat(e.getCause(), is(ConnectException.class));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void httpsServerMissingKeystore() throws Exception {
		ServletDefinition servlet = new ServletDefinition.Builder().servlet(
				new EchoServlet()).build();
		ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).addServlet(servlet).build();
	}

	@Test
	public void httpHttpsNoSecurityServer() throws Exception {
		ServletDefinition servlet = new ServletDefinition.Builder().servlet(
				new EchoServlet()).build();
		this.server = ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).sslKeyStorePath(SERVER_KEYSTORE)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.addServlet(servlet).build();
		this.server.start();
		// test connecting on http port
		assertThat(httpNoAuth().getStatus(), is(Status.OK.getStatusCode()));
		// test connecting on https port
		assertThat(httpsNoAuth().getStatus(), is(Status.OK.getStatusCode()));
	}

	@Test
	public void httpHttpsThatRequiresHttpsServer() throws Exception {
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new EchoServlet()).requireHttps(true).build();
		this.server = ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).sslKeyStorePath(SERVER_KEYSTORE)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.addServlet(servlet).build();
		this.server.start();
		// connecting on http should redirect to https
		assertThat(httpNoAuth().getStatus(), is(Status.FOUND.getStatusCode()));
		// test connecting on http port
		assertThat(httpsNoAuth().getStatus(), is(Status.OK.getStatusCode()));
	}

	@Test
	public void httpHttpsBasicAuthNotRequiringHttpsServer() throws Exception {
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new EchoServlet()).requireHttps(false)
				.requireBasicAuth(true).requireRole("USER")
				.realmFile(SECURITY_REALM_FILE).build();

		this.server = ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).sslKeyStorePath(SERVER_KEYSTORE)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.addServlet(servlet).build();
		this.server.start();
		// test connecting on without credentials: should not be allowed
		assertThat(httpNoAuth().getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));
		assertThat(httpsNoAuth().getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));

		// test connecting on http port with right credentials
		assertThat(httpBasicAuth("admin", "adminpassword").getStatus(),
				is(Status.OK.getStatusCode()));
		// test connecting on https port with right credentials
		assertThat(httpsBasicAuth("admin", "adminpassword").getStatus(),
				is(Status.OK.getStatusCode()));

		// test connecting on http port with wrong user/password
		assertThat(httpBasicAuth("admin", "wrongpassword").getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));
		// test connecting on https port with wrong user/password
		assertThat(httpsBasicAuth("admin", "wrongpassword").getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));

		// test connecting on http port with wrong role
		assertThat(httpBasicAuth("guest", "guestpassword").getStatus(),
				is(Status.FORBIDDEN.getStatusCode()));
		// test connecting on https port with wrong role
		assertThat(httpsBasicAuth("guest", "guestpassword").getStatus(),
				is(Status.FORBIDDEN.getStatusCode()));
	}

	@Test
	public void httpHttpsBasicAuthRequiringHttpsServer() throws Exception {
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new EchoServlet()).requireHttps(true)
				.requireBasicAuth(true).requireRole("USER")
				.realmFile(SECURITY_REALM_FILE).build();
		this.server = ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).sslKeyStorePath(SERVER_KEYSTORE)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.addServlet(servlet).build();
		this.server.start();

		// test connecting on http port with right credentials: should
		// redirect (302 Found)
		assertThat(httpBasicAuth("admin", "adminpassword").getStatus(),
				is(Status.FOUND.getStatusCode()));
		// test connecting on http port with wrong credentials: should
		// redirect (302 Found)
		assertThat(httpBasicAuth("admin", "wrongpassword").getStatus(),
				is(Status.FOUND.getStatusCode()));

		// test connecting on https port with right credentials: OK
		assertThat(httpsBasicAuth("admin", "adminpassword").getStatus(),
				is(Status.OK.getStatusCode()));
		// test connecting on https port with wrong user/password: UNAUTHORIZED
		assertThat(httpsBasicAuth("admin", "wrongpassword").getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));
		// test connecting on https port with wrong role: FORBIDDEN
		assertThat(httpsBasicAuth("guest", "guestpassword").getStatus(),
				is(Status.FORBIDDEN.getStatusCode()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void httpHttpsBasicAuthServerMissingSecurityRealm() {
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new EchoServlet()).requireHttps(true)
				.requireBasicAuth(true).build();
		ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).sslKeyStorePath(SERVER_KEYSTORE)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.addServlet(servlet).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void httpHttpsBasicAuthServerMissingRequiredRole() {
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new EchoServlet()).requireHttps(true)
				.requireBasicAuth(true).realmFile(SECURITY_REALM_FILE).build();

		ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).sslKeyStorePath(SERVER_KEYSTORE)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.addServlet(servlet).build();
	}

	@Test
	public void httpsCertAuthServer() throws Exception {
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new EchoServlet()).requireHttps(true).build();
		this.server = ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).sslKeyStorePath(SERVER_KEYSTORE)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.sslRequireClientCert(true)
				.sslTrustStorePath(SERVER_TRUSTSTORE)
				.sslTrustStorePassword(SERVER_TRUSTSTORE_PASSWORD)
				.addServlet(servlet).build();
		this.server.start();

		// test connecting on http port without credentials: should redirect
		// (302 Found)
		assertThat(httpNoAuth().getStatus(), is(Status.FOUND.getStatusCode()));

		// connect on https port without credentials: should fail SSL connect
		try {
			httpsNoAuth();
		} catch (ProcessingException e) {
			logger.error("error was: " + e.getMessage(), e);
			assertSslHandshakeFailure(e);
		}
		// connect on https port with trusted cert: should be OK
		assertThat(httpsCertAuth(CLIENT_KEYSTORE, CLIENT_KEYSTORE_PASSWORD)
				.getStatus(), is(Status.OK.getStatusCode()));
		// connect on https port with untrusted cert: should fail SSL connect
		try {
			httpsCertAuth(UNTRUSTED_CLIENT_KEYSTORE,
					UNTRUSTED_CLIENT_KEYSTORE_PASSWORD);
		} catch (ProcessingException e) {
			logger.error("error was: " + e.getMessage(), e);
			assertSslHandshakeFailure(e);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void httpsCertAuthServerMissingTruststore() {
		ServletDefinition servlet = new ServletDefinition.Builder().servlet(
				new EchoServlet()).build();
		ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).sslKeyStorePath(SERVER_KEYSTORE)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.sslRequireClientCert(true).addServlet(servlet).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void httpsCertAuthServerMissingTruststorePassword() {
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new EchoServlet()).requireHttps(true).build();

		ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).sslKeyStorePath(SERVER_KEYSTORE)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.sslRequireClientCert(true)
				.sslTrustStorePath(SERVER_TRUSTSTORE).addServlet(servlet)
				.build();
	}

	@Test
	public void httpsCertAuthAndBasicAuthServer() throws Exception {
		ServletDefinition servlet = new ServletDefinition.Builder()
				.servlet(new EchoServlet()).requireHttps(true)
				.requireBasicAuth(true).requireRole("USER")
				.realmFile(SECURITY_REALM_FILE).build();

		this.server = ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).sslKeyStorePath(SERVER_KEYSTORE)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.sslRequireClientCert(true)
				.sslTrustStorePath(SERVER_TRUSTSTORE)
				.sslTrustStorePassword(SERVER_TRUSTSTORE_PASSWORD)
				.addServlet(servlet).build();
		this.server.start();
		// connect on http port: should redirect (302 Found)
		assertThat(httpNoAuth().getStatus(), is(Status.FOUND.getStatusCode()));

		// connecting with trusted cert and right username/password: OK
		assertThat(
				httpsCertAndBasicAuth(CLIENT_KEYSTORE,
						CLIENT_KEYSTORE_PASSWORD, "admin", "adminpassword")
						.getStatus(), is(Status.OK.getStatusCode()));
		// connecting with trusted cert and wrong password: UNAUTHORIZED
		assertThat(
				httpsCertAndBasicAuth(CLIENT_KEYSTORE,
						CLIENT_KEYSTORE_PASSWORD, "admin", "wrongpassword")
						.getStatus(), is(Status.UNAUTHORIZED.getStatusCode()));
		// connecting with trusted cert and wrong role: FORBIDDEN
		assertThat(
				httpsCertAndBasicAuth(CLIENT_KEYSTORE,
						CLIENT_KEYSTORE_PASSWORD, "guest", "guestpassword")
						.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

		// connecting with untrusted cert: SSL connection should fail
		try {
			httpsCertAndBasicAuth(UNTRUSTED_CLIENT_KEYSTORE,
					UNTRUSTED_CLIENT_KEYSTORE_PASSWORD, "admin",
					"adminpassword");
		} catch (ProcessingException e) {
			assertSslHandshakeFailure(e);
		}
	}

	/**
	 * Verify that an exception is due to a failure to establish an SSL
	 * connection.
	 * 
	 * @param cause
	 */
	private void assertSslHandshakeFailure(Throwable cause) {
		if (cause instanceof SSLHandshakeException) {
			return;
		}
		// in some cases it seems that a "connection reset" error can also be
		// seen on the client side on failure to authenticate with a cert.
		if (cause instanceof SocketException) {
			assertTrue(cause.getMessage().contains("Connection reset"));
		}
	}

	/**
	 * Verify that two servlets can be added and accessed side-by-side.
	 */
	@Test
	public void deployTwoServlets() throws Exception {
		ServletDefinition servlet1 = new ServletDefinition.Builder()
				.servlet(new MessageServlet("1")).servletPath("/servlet1")
				.build();
		ServletDefinition servlet2 = new ServletDefinition.Builder()
				.servlet(new MessageServlet("2")).servletPath("/servlet2")
				.build();
		this.server = ServletServerBuilder.create().httpPort(this.httpPort)
				.addServlet(servlet1).addServlet(servlet2).build();
		this.server.start();

		String servlet1Url = httpUrl("/servlet1");
		String servlet2Url = httpUrl("/servlet2");

		Client noAuthClient = RestTestUtils.httpNoAuth();
		Response response = noAuthClient.target(servlet1Url).request().get();
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
		assertThat(response.readEntity(String.class), is("1"));

		response = noAuthClient.target(servlet2Url).request().get();
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
		assertThat(response.readEntity(String.class), is("2"));

		response = noAuthClient.target(httpUrl("/")).request().get();
		assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));
	}

	/**
	 * Deploy servlets with different security requirements and verify that
	 * security settings are really applied on a per-servlet basis.
	 */
	@Test
	public void deployServletsWithDifferentSecuritySettings() throws Exception {
		// servlet1 accepts http requests
		ServletDefinition servlet1 = new ServletDefinition.Builder()
				.servlet(new MessageServlet("UNSECURED"))
				.servletPath("/servlet1").build();
		// servlet2 requires https, but no authentication
		ServletDefinition servlet2 = new ServletDefinition.Builder()
				.servlet(new MessageServlet("SECURED_NOAUTH"))
				.servletPath("/servlet2").requireHttps(true).build();
		// servlet3 requires https, but no authentication
		ServletDefinition servlet3 = new ServletDefinition.Builder()
				.servlet(new MessageServlet("SECURED_AUTH"))
				.servletPath("/servlet3").requireHttps(true)
				.requireBasicAuth(true).realmFile(SECURITY_REALM_FILE)
				.requireRole("USER").build();

		this.server = ServletServerBuilder.create().httpPort(this.httpPort)
				.httpsPort(this.httpsPort).sslKeyStorePath(SERVER_KEYSTORE)
				.sslKeyStorePassword(SERVER_KEYSTORE_PASSWORD)
				.sslRequireClientCert(false)
				.sslTrustStorePath(SERVER_TRUSTSTORE)
				.sslTrustStorePassword(SERVER_TRUSTSTORE_PASSWORD)
				.addServlet(servlet1).addServlet(servlet2).addServlet(servlet3)
				.build();
		this.server.start();

		// TODO: remove
		// Uninterruptibles.sleepUninterruptibly(500, TimeUnit.SECONDS);

		// http access to servlet1: allowed
		Client client = RestTestUtils.httpNoAuth();
		Response response = client.target(httpUrl("/servlet1")).request().get();
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
		assertThat(response.readEntity(String.class), is("UNSECURED"));
		// http access to servlet2: not allowed (should be redirected)
		response = client.target(httpUrl("/servlet2")).request().get();
		assertThat(response.getStatus(), is(Status.FOUND.getStatusCode()));
		// http access to servlet3: not allowed (should be redirected)
		response = client.target(httpUrl("/servlet3")).request().get();
		assertThat(response.getStatus(), is(Status.FOUND.getStatusCode()));

		// unauthenticated https access to servlet1: allowed
		client = RestTestUtils.httpsNoAuth();
		response = client.target(httpsUrl("/servlet1")).request().get();
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
		assertThat(response.readEntity(String.class), is("UNSECURED"));
		// unauthenticated https access to servlet2: allowed
		response = client.target(httpsUrl("/servlet2")).request().get();
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
		assertThat(response.readEntity(String.class), is("SECURED_NOAUTH"));
		// unauthenticated https access to servlet3: not allowed
		response = client.target(httpsUrl("/servlet3")).request().get();
		assertThat(response.getStatus(),
				is(Status.UNAUTHORIZED.getStatusCode()));

		// authenticated https access to servlet1: allowed
		client = RestTestUtils.httpsBasicAuth("admin", "adminpassword");
		response = client.target(httpsUrl("/servlet1")).request().get();
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
		assertThat(response.readEntity(String.class), is("UNSECURED"));
		// authenticated https access to servlet2: allowed
		response = client.target(httpsUrl("/servlet2")).request().get();
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
		assertThat(response.readEntity(String.class), is("SECURED_NOAUTH"));
		// authenticated https access to servlet3: allowed
		response = client.target(httpsUrl("/servlet3")).request().get();
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
		assertThat(response.readEntity(String.class), is("SECURED_AUTH"));
	}

	private Response httpNoAuth() {
		Client noAuthClient = RestTestUtils.httpNoAuth();
		return noAuthClient.target(httpUrl("/")).request().get();
	}

	private Response httpsNoAuth() {
		Client noAuthClient = RestTestUtils.httpsNoAuth();
		return noAuthClient.target(httpsUrl("/")).request().get();
	}

	private Response httpBasicAuth(String username, String password) {
		Client client = RestTestUtils.httpBasicAuth(username, password);
		return client.target(httpUrl("/")).request().get();
	}

	private Response httpsBasicAuth(String username, String password) {
		Client client = RestTestUtils.httpsBasicAuth(username, password);
		return client.target(httpsUrl("/")).request().get();
	}

	private Response httpsCertAuth(String keyStorePath, String keyStorePassword) {
		Client client = RestTestUtils.httpsCertAuth(keyStorePath,
				keyStorePassword, KeyStoreType.PKCS12);
		return client.target(httpsUrl("/")).request().get();
	}

	private Response httpsCertAndBasicAuth(String keyStorePath,
			String keyStorePassword, String username, String password) {
		Client client = RestTestUtils.httpsCertAuth(keyStorePath,
				keyStorePassword, KeyStoreType.PKCS12);
		client.register(new HttpBasicAuthFilter(username, password));
		return client.target(httpsUrl("/")).request().get();
	}

	private String httpUrl(String path) {
		path = path.startsWith("/") ? path : "/" + path;
		return String.format("http://localhost:%d%s", this.httpPort, path);
	}

	private String httpsUrl(String path) {
		path = path.startsWith("/") ? path : "/" + path;
		return String.format("https://localhost:%d%s", this.httpsPort, path);
	}

}
