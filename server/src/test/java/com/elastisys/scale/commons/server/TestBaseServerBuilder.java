package com.elastisys.scale.commons.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Test;

/**
 * Exercises the {@link BaseServerBuilder} class.
 * 
 * 
 * 
 */
public class TestBaseServerBuilder {

	private static final String ETC_SECURITY = "/etc/security";

	@Test
	public void noListenPorts() throws Exception {
		Server server = BaseServerBuilder.create().build();
		assertTrue(Arrays.asList(server.getConnectors()).isEmpty());
	}

	@Test
	public void http() throws Exception {
		Server server = BaseServerBuilder.create().httpPort(8080).build();

		List<Connector> connectors = Arrays.asList(server.getConnectors());
		assertThat(server.getConnectors().length, is(1));
		// should have a server connector on port 8080
		ServerConnector connector = (ServerConnector) connectors.get(0);
		assertThat(connector.getPort(), is(8080));

		// should have a http connection factory
		HttpConnectionFactory httpConnectionFactory = connector
				.getConnectionFactory(HttpConnectionFactory.class);
		assertThat(httpConnectionFactory, is(notNullValue()));

		// no https redirect port should be set
		assertThat(
				httpConnectionFactory.getHttpConfiguration().getSecurePort(),
				is(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void httpsMissingKeystore() throws Exception {
		BaseServerBuilder.create().httpsPort(8443).build();
	}

	@Test
	public void https() throws Exception {
		Server server = BaseServerBuilder.create().httpsPort(8443)
				.sslKeyStoreType(SslKeyStoreType.PKCS12)
				.sslKeyStorePath(ETC_SECURITY + "/server_keystore.p12")
				.sslKeyStorePassword("pkcs12password").build();

		List<Connector> connectors = Arrays.asList(server.getConnectors());
		assertThat(server.getConnectors().length, is(1));
		// should have a server connector on port 8443
		ServerConnector connector = (ServerConnector) connectors.get(0);
		assertThat(connector.getPort(), is(8443));

		// should have a https connection factory
		SslConnectionFactory httpsConnectionFactory = connector
				.getConnectionFactory(SslConnectionFactory.class);
		assertThat(httpsConnectionFactory, is(notNullValue()));

		// should have a key store configured
		SslContextFactory sslContextFactory = httpsConnectionFactory
				.getSslContextFactory();
		assertThat(sslContextFactory, is(notNullValue()));
		assertThat(sslContextFactory.getKeyStoreType(),
				is(SslKeyStoreType.PKCS12.name()));
		assertThat(sslContextFactory.getKeyStorePath(), is(ETC_SECURITY
				+ "/server_keystore.p12"));

		// should not require client cert authentication
		assertThat(sslContextFactory.getNeedClientAuth(), is(false));
		// should not *want* client cert authentication (if offered)
		assertThat(sslContextFactory.getWantClientAuth(), is(false));
	}

	@Test(expected = IllegalArgumentException.class)
	public void httpsMissingKeystorePassword() throws Exception {
		BaseServerBuilder.create().httpsPort(8443)
				.sslKeyStoreType(SslKeyStoreType.PKCS12)
				.sslKeyStorePath(ETC_SECURITY + "/server_keystore.p12").build();
	}

	@Test
	public void httpsWithTrustStore() throws Exception {
		Server server = BaseServerBuilder.create().httpsPort(8443)
				.sslKeyStoreType(SslKeyStoreType.PKCS12)
				.sslKeyStorePath(ETC_SECURITY + "/server_keystore.p12")
				.sslKeyStorePassword("pkcs12password")
				.sslTrustStoreType(SslKeyStoreType.JKS)
				.sslTrustStorePath(ETC_SECURITY + "/server_truststore.jks")
				.sslTrustStorePassword("truststorepassword").build();

		List<Connector> connectors = Arrays.asList(server.getConnectors());
		assertThat(server.getConnectors().length, is(1));
		// should have a server connector on port 8443
		ServerConnector connector = (ServerConnector) connectors.get(0);
		assertThat(connector.getPort(), is(8443));

		// should have a https connection factory
		SslConnectionFactory httpsConnectionFactory = connector
				.getConnectionFactory(SslConnectionFactory.class);
		assertThat(httpsConnectionFactory, is(notNullValue()));

		// should have a key store configured
		SslContextFactory sslContextFactory = httpsConnectionFactory
				.getSslContextFactory();
		assertThat(sslContextFactory, is(notNullValue()));
		assertThat(sslContextFactory.getTrustStoreType(),
				is(SslKeyStoreType.JKS.name()));
		assertThat(sslContextFactory.getTrustStore(), is(ETC_SECURITY
				+ "/server_truststore.jks"));

		// should not require client cert authentication
		assertThat(sslContextFactory.getNeedClientAuth(), is(false));
		// should not *want* client cert authentication (if offered)
		assertThat(sslContextFactory.getWantClientAuth(), is(false));
	}

	@Test
	public void httpsRequireClientCert() throws Exception {
		Server server = BaseServerBuilder.create().httpsPort(8443)
				.sslKeyStoreType(SslKeyStoreType.PKCS12)
				.sslKeyStorePath(ETC_SECURITY + "/server_keystore.p12")
				.sslKeyStorePassword("pkcs12password")
				.sslTrustStoreType(SslKeyStoreType.JKS).sslRequireClientCert(true)
				.sslTrustStorePath(ETC_SECURITY + "/server_truststore.jks")
				.sslTrustStorePassword("truststorepassword")
				.sslRequireClientCert(true).build();

		List<Connector> connectors = Arrays.asList(server.getConnectors());
		assertThat(server.getConnectors().length, is(1));
		// should have a server connector on port 8443
		ServerConnector connector = (ServerConnector) connectors.get(0);
		assertThat(connector.getPort(), is(8443));

		// should have a https connection factory
		SslConnectionFactory httpsConnectionFactory = connector
				.getConnectionFactory(SslConnectionFactory.class);
		assertThat(httpsConnectionFactory, is(notNullValue()));

		SslContextFactory sslContextFactory = httpsConnectionFactory
				.getSslContextFactory();

		// should require client cert authentication
		assertThat(sslContextFactory.getNeedClientAuth(), is(true));
	}

	@Test
	public void httpAndHttps() {
		Server server = BaseServerBuilder.create().httpPort(8080)
				.httpsPort(8443).sslKeyStoreType(SslKeyStoreType.PKCS12)
				.sslKeyStorePath(ETC_SECURITY + "/server_keystore.p12")
				.sslKeyStorePassword("pkcs12password")
				.sslTrustStoreType(SslKeyStoreType.JKS).sslRequireClientCert(true)
				.sslTrustStorePath(ETC_SECURITY + "/server_truststore.jks")
				.sslTrustStorePassword("truststorepassword")
				.sslRequireClientCert(true).build();

		List<Connector> connectors = Arrays.asList(server.getConnectors());
		assertThat(server.getConnectors().length, is(2));
		// should have a server connector on port 8080
		ServerConnector httpConnector = (ServerConnector) connectors.get(0);
		assertThat(httpConnector.getPort(), is(8080));
		// should have a server connector on port 8443
		ServerConnector httpsConnector = (ServerConnector) connectors.get(1);
		assertThat(httpsConnector.getPort(), is(8443));

		// should redirect secure connections to https port
		HttpConnectionFactory httpFactory = httpConnector
				.getConnectionFactory(HttpConnectionFactory.class);
		assertThat(httpFactory, is(notNullValue()));
		assertThat(httpFactory.getHttpConfiguration().getSecurePort(), is(8443));
		assertThat(httpFactory.getHttpConfiguration().getSecureScheme(),
				is("https"));
	}
}
