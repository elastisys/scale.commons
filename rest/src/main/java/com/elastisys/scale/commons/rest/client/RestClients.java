package com.elastisys.scale.commons.rest.client;

import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.elastisys.scale.commons.net.ssl.SslContextBuilder;
import com.elastisys.scale.commons.net.ssl.SslUtils;
import com.elastisys.scale.commons.rest.converters.GsonMessageBodyReader;
import com.elastisys.scale.commons.rest.converters.GsonMessageBodyWriter;
import com.google.common.base.Throwables;
import com.google.gson.JsonObject;

/**
 * A utility class for creating different forms of REST clients.
 * <p/>
 * The created {@link Client}s support JSON serialization/deserialization of
 * Java classes via {@link GsonMessageBodyReader} and
 * {@link GsonMessageBodyWriter}, as well as native support for
 * {@link JsonObject} parameters/responses.
 */
public class RestClients {

	/**
	 * Creates a HTTPS Jersey REST {@link Client} configured to not authenticate
	 * to the server.
	 * <p/>
	 * The created {@link Client} is configured to trust all server certificates
	 * and approve all host names. (This is similar to using the
	 * <code>--insecure</code> flag in <code>curl</code>.)
	 *
	 * @return The created {@link Client}.
	 */
	public static Client httpsNoAuth() {
		Client client = ClientBuilder.newBuilder()
				.sslContext(SslUtils.trustAllCertsSslContext())
				.hostnameVerifier(SslUtils.allowAllHostNames())
				.register(GsonMessageBodyReader.class)
				.register(GsonMessageBodyWriter.class).build();

		return client;
	}

	/**
	 * Creates a HTTPS Jersey REST {@link Client} configured to authenticate
	 * (via <a
	 * href="http://en.wikipedia.org/wiki/Basic_access_authentication">Basic
	 * authentication<a/>) with a given user name and password.
	 * <p/>
	 * The created {@link Client} is configured to trust all server certificates
	 * and approve all host names. (This is similar to using the
	 * <code>--insecure</code> flag in <code>curl</code>.)
	 *
	 * @param userName
	 *            The user name used to authenticate.
	 * @param password
	 *            The password used to authenticate.
	 * @return The created {@link Client}.
	 */
	public static Client httpsBasicAuth(String userName, String password) {
		Client client = ClientBuilder.newBuilder()
				.sslContext(SslUtils.trustAllCertsSslContext())
				.hostnameVerifier(SslUtils.allowAllHostNames())
				.register(GsonMessageBodyReader.class)
				.register(GsonMessageBodyWriter.class).build();
		client.register(HttpAuthenticationFeature.basic(userName, password));
		return client;
	}

	/**
	 * Creates a HTTPS Jersey REST {@link Client} configured to authenticate
	 * with a certificate.
	 * <p/>
	 * The created {@link Client} is configured to trust all server certificates
	 * and approve all host names. (This is similar to using the
	 * <code>--insecure</code> flag in <code>curl</code>.)
	 *
	 * @param keystore
	 *            The key store that contains the client's certificate and
	 *            private key.
	 * @param password
	 *            The password used to protect the client key.
	 * @return The created {@link Client}.
	 *
	 * @throws RuntimeException
	 */
	public static Client httpsCertAuth(KeyStore keystore, String password)
			throws RuntimeException {
		try {
			SSLContext clientCertSslContext = SslContextBuilder.newBuilder()
					.clientAuthentication(keystore, password)
					.noServerAuthentication().build();
			Client client = ClientBuilder.newBuilder()
					.sslContext(clientCertSslContext)
					.hostnameVerifier(SslUtils.allowAllHostNames())
					.register(GsonMessageBodyReader.class)
					.register(GsonMessageBodyWriter.class).build();
			return client;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Creates a HTTP Jersey REST {@link Client} configured to authenticate (via
	 * <a href="http://en.wikipedia.org/wiki/Basic_access_authentication">Basic
	 * authentication<a/>) with a given user name and password.
	 *
	 * @param userName
	 *            The user name used to authenticate.
	 * @param password
	 *            The password used to authenticate.
	 * @return The created {@link Client}.
	 */
	public static Client httpBasicAuth(String userName, String password) {
		Client client = ClientBuilder.newBuilder()
				.register(GsonMessageBodyReader.class)
				.register(GsonMessageBodyWriter.class).build();
		client.register(HttpAuthenticationFeature.basic(userName, password));
		return client;
	}

	/**
	 * Creates a HTTP Jersey REST {@link Client} configured to not authenticate
	 * to the server.
	 *
	 * @return The created {@link Client}.
	 */
	public static Client httpNoAuth() {
		Client client = ClientBuilder.newBuilder()
				.register(GsonMessageBodyReader.class)
				.register(GsonMessageBodyWriter.class).build();
		return client;
	}
}
