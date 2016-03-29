package com.elastisys.scale.commons.net.http;

import static com.google.common.base.Preconditions.checkArgument;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;

import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;
import com.elastisys.scale.commons.net.ssl.KeyStoreType;
import com.elastisys.scale.commons.net.ssl.SslContextBuilder;
import com.elastisys.scale.commons.net.ssl.SslUtils;

/**
 * A builder of HTTP(S) clients.
 *
 * @see Http
 */
public class HttpBuilder {
	/**
	 * The default timeout in milliseconds until a connection is established. A
	 * timeout value of zero is interpreted as an infinite timeout. A negative
	 * value is interpreted as undefined (system default).
	 */
	public static final int DEFAULT_CONNECTION_TIMEOUT = 20000;
	/**
	 * The default socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is
	 * the timeout for waiting for data or, put differently, a maximum period
	 * inactivity between two consecutive data packets). A timeout value of zero
	 * is interpreted as an infinite timeout. A negative value is interpreted as
	 * undefined (system default).
	 */
	public static final int DEFAULT_SOCKET_TIMEOUT = 20000;

	/**
	 * The {@link HttpClientBuilder} that is used to collect settings for the
	 * {@link Http} instance being built.
	 */
	private final HttpClientBuilder clientBuilder;

	/**
	 * Builder that collects SSL-related settings for the {@link Http} instance
	 * being built.
	 */
	private final SslContextBuilder sslContextBuilder;
	/**
	 * Builder that collects values to set for the default {@link RequestConfig}
	 * for the {@link Http} instance being built.
	 */
	private final RequestConfig.Builder requestConfigBuilder;

	/**
	 * Collects default headers to use for the {@link Http} instance being
	 * built.
	 */
	private final Map<String, String> defaultHeaders;

	private Logger logger;

	/**
	 * Creates a new {@link HttpBuilder}. Without additional input, the builder
	 * is set up to build {@link Http} instances with the following properties:
	 * <ul>
	 * <li>default connection timeout and socket timeout: 20 seconds</li>
	 * <li>server authentication/verification (on SSL): none</li>
	 * <li>client authentication: none</li>
	 * <li>default request content type: application/json</li>
	 * </ul>
	 * All of these settings can be modified through the builder's methods.
	 */
	public HttpBuilder() {
		this.clientBuilder = HttpClients.custom();
		this.clientBuilder.setRedirectStrategy(new LaxRedirectStrategy());

		this.requestConfigBuilder = RequestConfig.copy(RequestConfig.DEFAULT);
		this.requestConfigBuilder.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
		this.requestConfigBuilder.setSocketTimeout(DEFAULT_SOCKET_TIMEOUT);

		this.sslContextBuilder = SslContextBuilder.newBuilder();
		verifyHostCert(false);
		verifyHostname(false);

		this.defaultHeaders = new HashMap<>();
		contentType(ContentType.APPLICATION_JSON);

		this.logger = Http.LOG;
	}

	/**
	 * Constructs a {@link Http} instance from the parameters supplied to the
	 * {@link HttpBuilder}.
	 *
	 * @return
	 * @throws HttpBuilderException
	 */
	public Http build() throws HttpBuilderException {
		try {
			this.clientBuilder.setSSLContext(this.sslContextBuilder.build());
		} catch (Exception e) {
			throw new HttpBuilderException(
					"failed to set SSL context when building HTTP client: "
							+ e.getMessage(),
					e);
		}
		this.clientBuilder
				.setDefaultRequestConfig(this.requestConfigBuilder.build());

		List<Header> headers = new ArrayList<>();
		this.defaultHeaders.entrySet().stream().forEach(header -> {
			headers.add(new BasicHeader(header.getKey(), header.getValue()));
		});
		this.clientBuilder.setDefaultHeaders(headers);

		return new Http(this.clientBuilder, this.logger);
	}

	/**
	 * Sets the {@link Logger} to use for built {@link Http} instances.
	 *
	 * @param logger
	 * @return
	 */
	public HttpBuilder logger(Logger logger) {
		this.logger = logger;
		return this;
	}

	/**
	 * The default timeout in milliseconds until a connection is established. A
	 * timeout value of zero is interpreted as an infinite timeout. A negative
	 * value is interpreted as undefined (system default).
	 * <p/>
	 * This value can be overridden on a per-request basis in the created
	 * {@link Http} client.
	 *
	 * @param connectTimeout
	 * @return
	 */
	public HttpBuilder connectionTimeout(int connectTimeout) {
		this.requestConfigBuilder.setConnectTimeout(connectTimeout);
		return this;
	}

	/**
	 * The default socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is
	 * the timeout for waiting for data or, put differently, a maximum period
	 * inactivity between two consecutive data packets). A timeout value of zero
	 * is interpreted as an infinite timeout. A negative value is interpreted as
	 * undefined (system default).
	 * <p/>
	 * This value can be overridden on a per-request basis in the created
	 * {@link Http} client.
	 *
	 * @param socketTimeout
	 * @return
	 */
	public HttpBuilder socketTimeout(int socketTimeout) {
		this.requestConfigBuilder.setSocketTimeout(socketTimeout);
		return this;
	}

	/**
	 * Sets a default header for the {@link Http} client being built. *
	 * <p/>
	 * Headers can be overridden on a per-request basis in the created
	 * {@link Http} client.
	 *
	 * @param name
	 *            The header name. For example, {@code Content-Type}.
	 * @param value
	 *            The header value. For example, {@code application/json}.
	 * @return
	 */
	public HttpBuilder header(String name, String value) {
		this.defaultHeaders.put(name, value);
		return this;
	}

	/**
	 * Sets a default {@code Content-Type} header to use for the {@link Http}
	 * client being built.
	 * <p/>
	 * Headers can be overridden on a per-request basis in the created
	 * {@link Http} client.
	 *
	 * @param contentType
	 *            Content type to set. For example,
	 *            {@link ContentType#TEXT_PLAIN}.
	 * @return
	 */
	public HttpBuilder contentType(ContentType contentType) {
		header(HttpHeaders.CONTENT_TYPE, contentType.toString());
		return this;
	}

	/**
	 * Set to enable basic (username/password) client authentication. See
	 * <a href="http://en.wikipedia.org/wiki/Basic_access_authentication">Basic
	 * autentication</a>.
	 *
	 * @param clientBasicCredentials
	 * @return
	 */
	public HttpBuilder clientBasicAuth(
			BasicCredentials clientBasicCredentials) {
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(
						clientBasicCredentials.getUsername(),
						clientBasicCredentials.getPassword()));
		this.clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		return this;
	}

	/**
	 * Set to enable client certificate authentication for SSL connections. If
	 * set, the client certificate will be included in SSL connections, and the
	 * server may choose to authenticate the client via the provided
	 * certificate. Client certificate authentication is further described
	 * <a href="http://docs.oracle.com/javaee/6/tutorial/doc/glien.html">here
	 * </a>.
	 *
	 * @param clientCertCredentials
	 *            The certificate credentials to be used for authenticating the
	 *            client.
	 * @return
	 */
	public HttpBuilder clientCertAuth(
			CertificateCredentials clientCertCredentials) {
		String keystorePath = clientCertCredentials.getKeystorePath();
		String keystorePassword = clientCertCredentials.getKeystorePassword();
		String keyPassword = clientCertCredentials.getKeyPassword();
		KeyStore keyStore = null;
		try {
			keyStore = SslUtils.loadKeyStore(
					clientCertCredentials.getKeystoreType(), keystorePath,
					keystorePassword);
		} catch (Exception e) {
			throw new HttpBuilderException(
					"failed to set client certificate credentials: "
							+ e.getMessage(),
					e);
		}
		return clientCertAuth(keyStore, keyPassword);
	}

	/**
	 * Set to enable client certificate authentication for SSL connections. If
	 * set, a client certificate from the supplied {@link KeyStore} will be
	 * included in SSL connections, and the server may choose to authenticate
	 * the client via the provided certificate. Client certificate
	 * authentication is further described
	 * <a href="http://docs.oracle.com/javaee/6/tutorial/doc/glien.html">here
	 * </a>.
	 *
	 * @param clientCertKeystore
	 *            The {@link KeyStore} containing the client certificate and
	 *            private key to be used for authenticating the client.
	 * @param keyPassword
	 *            The password used to recover the client key from the key
	 *            store.
	 * @return
	 */
	public HttpBuilder clientCertAuth(KeyStore clientCertKeystore,
			String keyPassword) {
		checkArgument(clientCertKeystore != null, "null keystore given");
		checkArgument(keyPassword != null,
				"null keyPassword given (keystore keys cannot "
						+ "be recovered without a password)");

		try {
			this.sslContextBuilder.clientAuthentication(clientCertKeystore,
					keyPassword);
		} catch (Exception e) {
			throw new HttpBuilderException(
					"failed to set client certificate credentials: "
							+ e.getMessage(),
					e);
		}
		return this;
	}

	/**
	 * Adds a default header used to carry a signed authentication token
	 *
	 * <pre>
	 * {@code Authorization: Bearer <token>}
	 * </pre>
	 *
	 * Refer to the
	 * <a href="http://tools.ietf.org/html/draft-ietf-oauth-json-web-token-15">
	 * JSON Web Token specification</a> and the
	 * <a href="http://tools.ietf.org/html/rfc6750">bearer token
	 * specification</a>.
	 *
	 * @param signedAuthToken
	 *            A signed auth token to be included in a
	 *            {@code Authorization: Bearer <token>} header.
	 * @return
	 */
	public HttpBuilder clientJwtAuth(String signedAuthToken) {
		return header(HttpHeaders.AUTHORIZATION,
				String.format("Bearer %s", signedAuthToken));
	}

	/**
	 * Set to <code>true</code> to enable server certificate verification on SSL
	 * connections. If disabled, the server peer will not be verified, which is
	 * similar to using the {@code --insecure} flag in {@code curl}.
	 * <p/>
	 * If enabled, the host certificate is verified against either the
	 * configured trust store (if one has been set via
	 * {@link #serverAuthTrustStore(KeyStore)}) or a against a default trust
	 * store configured with the JVM (see the guide on <a href=
	 * "http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html#CustomizingStores">
	 * JSSE</a>) in case no trust store has been explicitly set.
	 *
	 * @return
	 */
	public HttpBuilder verifyHostCert(boolean shouldVerify) {
		try {
			this.sslContextBuilder.setVerifyHostCert(shouldVerify);
			return this;
		} catch (Exception e) {
			throw new HttpBuilderException(
					String.format("failed to set verifyHostCert to %s: %s",
							shouldVerify, e.getMessage()),
					e);
		}
	}

	/**
	 * Sets a custom trust store to use when server authentication is requested
	 * (via {@link #verifyHostCert}). If no custom trust store has been
	 * specified and server authentication is requested via
	 * {@link #verifyHostCert(boolean)}, the server certificate according to the
	 * default trust store configured with the JVM (see the guide on <a href=
	 * "http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html#CustomizingStores">
	 * JSSE</a>).
	 *
	 * @param type
	 *            The type of the trust store.
	 * @param trustStorePath
	 *            The file system path to a trust store that contains trusted
	 *            CA/server certificates.
	 * @param storePassword
	 *            The password used to protect the integrity of the trust store.
	 *            Can be <code>null</code>.
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	public HttpBuilder serverAuthTrustStore(KeyStoreType type,
			String trustStorePath, String storePassword)
					throws HttpBuilderException {
		KeyStore trustStore = null;
		try {
			trustStore = SslUtils.loadKeyStore(type, trustStorePath,
					storePassword);
		} catch (Exception e) {
			throw new HttpBuilderException(
					"failed to set server auth trust store: " + e.getMessage(),
					e);
		}
		return serverAuthTrustStore(trustStore);
	}

	/**
	 * Sets a custom trust store to use when server authentication is requested
	 * (via {@link #verifyHostCert}). If no custom trust store has been
	 * specified and server authentication is requested via
	 * {@link #verifyHostCert(boolean)}, the server certificate according to the
	 * default trust store configured with the JVM (see the guide on <a href=
	 * "http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html#CustomizingStores">
	 * JSSE</a>).
	 *
	 * @param trustStore
	 *            The {@link KeyStore} that contains trusted CA/server
	 *            certificates.
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	public HttpBuilder serverAuthTrustStore(KeyStore trustStore)
			throws HttpBuilderException {
		try {
			this.sslContextBuilder.serverAuthTrustStore(trustStore);
		} catch (Exception e) {
			throw new HttpBuilderException(
					"failed to set server auth trust store: " + e.getMessage(),
					e);
		}
		return this;
	}

	/**
	 * Enables/disables hostname verification during SSL handshakes.
	 * <p/>
	 * If verification is enabled, the SSL handshake will only succeed if the
	 * URL's hostname and the server's identification hostname match.
	 *
	 * @param shouldVerify
	 *            Enable (<code>true</code>) or disable (<code>false</code>).
	 * @return
	 */
	public HttpBuilder verifyHostname(boolean shouldVerify) {
		HostnameVerifier sslHostnameVerifier = shouldVerify
				? new DefaultHostnameVerifier() : NoopHostnameVerifier.INSTANCE;
		this.clientBuilder.setSSLHostnameVerifier(sslHostnameVerifier);
		return this;
	}

}