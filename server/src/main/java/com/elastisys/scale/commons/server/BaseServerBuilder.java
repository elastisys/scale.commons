package com.elastisys.scale.commons.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * A flexible Builder for creating embedded HTTP(S) {@link Server}s with a range
 * of possible configuration combinations.
 * <p/>
 * The created {@link Server} can be considered a "base server", without any
 * registered request {@link Handler}s. Any request handlers need to be added by
 * the client.
 */
public class BaseServerBuilder {

	/**
	 * The {@link Server}'s HTTP listen port. May be <code>null</code>, in which
	 * case the server won't listen to HTTP requests.
	 */
	private Integer httpPort = null;
	/**
	 * The {@link Server}'s HTTPS listen port. May be <code>null</code>, in
	 * which case the server won't listen to HTTPS requests. <i>Note: setting a
	 * HTTPS port requires an SSL key store to be set for server
	 * authentication.</i>
	 */
	private Integer httpsPort = null;

	/**
	 * File system path or URI to a SSL key store, which stores the server's SSL
	 * certificate that it will use to authenticate to clients. <i>Note: this
	 * option is only relevant for {@link Server}s with an HTTPS port, in which
	 * case it is required.</i>
	 */
	private String sslKeyStorePath = null;

	/**
	 * The type of the SSL key store. Defaults to {@link SslKeyStoreType#PKCS12}
	 * . <i>Note: this option is only relevant for {@link Server}s with an HTTPS
	 * port, in which case it is required.</i>
	 */
	private SslKeyStoreType sslKeyStoreType = SslKeyStoreType.PKCS12;

	/**
	 * Password used to protect SSL key store. <i>Note: this option is only
	 * relevant for HTTPS {@link Server}s with an HTTPS port, in which case it
	 * is required.</i>
	 */
	private String sslKeyStorePassword = null;

	/**
	 * The password (if any) used to protect the specific server key within the
	 * key store. <i>Note: this option is only relevant for {@link Server}s with
	 * an HTTPS port.</i>
	 */
	private String sslKeyPassword = null;

	/**
	 * File system path or URI to SSL trust store, which stores trusted client
	 * certificates. <i>Note: this option is only relevant for {@link Server}s
	 * with an HTTPS port that require client certificate authentication. If no
	 * trust store is set, the server will use its key store as trust store.</i>
	 */
	private String sslTrustStorePath = null;
	/**
	 * The type of the SSL trust store. Defaults to {@link SslKeyStoreType#JKS}.
	 * <i>Note: this option is only relevant for {@link Server}s with an HTTPS
	 * port that require client certificate authentication.</i>
	 */
	private SslKeyStoreType sslTrustStoreType = SslKeyStoreType.JKS;
	/**
	 * Password used to protect the SSL trust store. <i>Note: this option is
	 * only relevant for {@link Server}s with an HTTPS port that require client
	 * certificate authentication.</i>
	 */
	private String sslTrustStorePassword = null;
	/**
	 * If set to <code>true</code>, requires SSL clients to authenticate with a
	 * certificate. <i>Note: this option is only relevant for {@link Server}s
	 * with an HTTPS port. If set, a trust store with trusted client
	 * certificates should be configured for the {@link Server}.</i>
	 */
	private boolean sslRequireClientCert = false;

	protected BaseServerBuilder() {
	}

	/**
	 * Creates a new {@link BaseServerBuilder}.
	 *
	 * @return
	 */
	public static BaseServerBuilder create() {
		return new BaseServerBuilder();
	}

	/**
	 * Builds a {@link Server} object from the parameters passed to the
	 * {@link BaseServerBuilder}.
	 *
	 * @return
	 */
	public Server build() {
		Server server = new Server();

		// set up HTTP connector
		if (this.httpPort != null) {
			HttpConfiguration httpConfig = new HttpConfiguration();
			if (this.httpsPort != null) {
				httpConfig.setSecureScheme("https");
				// set redirect port for those pages requiring confidential
				// (SSL) access
				httpConfig.setSecurePort(this.httpsPort);
			}
			httpConfig.setOutputBufferSize(32768);
			ServerConnector http = new ServerConnector(server,
					new HttpConnectionFactory(httpConfig));
			http.setPort(this.httpPort);

			server.addConnector(http);
		}

		// set up HTTPS connector with SSL key store (and optionally a trust
		// store)
		if (this.httpsPort != null) {
			checkArgument(this.sslKeyStoreType != null,
					"https configuration missing key store type is null");
			checkArgument(this.sslKeyStorePath != null,
					"https configuration missing SSL key store path");
			checkArgument(this.sslKeyStorePassword != null,
					"https configuration missing SSL key store password");

			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStoreType(this.sslKeyStoreType.name());
			sslContextFactory.setKeyStorePath(this.sslKeyStorePath);
			sslContextFactory.setKeyStorePassword(this.sslKeyStorePassword);
			if (this.sslKeyPassword != null) {
				sslContextFactory.setKeyManagerPassword(this.sslKeyPassword);
			} else {
				sslContextFactory
						.setKeyManagerPassword(this.sslKeyStorePassword);
			}

			sslContextFactory.addExcludeProtocols("SSLv3"); // kill POODLE

			// if no trust store path is given, the key store will be used as
			// trust store
			sslContextFactory.setTrustStoreType(this.sslKeyStoreType.name());
			sslContextFactory.setTrustStorePath(this.sslKeyStorePath);
			sslContextFactory.setTrustStorePassword(this.sslKeyStorePassword);
			if (this.sslTrustStorePath != null) {
				checkArgument(this.sslTrustStoreType != null,
						"missing trust store type for trust store");
				checkArgument(this.sslTrustStorePassword != null,
						"missing password for trust store");

				sslContextFactory
						.setTrustStoreType(this.sslTrustStoreType.name());
				sslContextFactory.setTrustStorePath(this.sslTrustStorePath);
				sslContextFactory
						.setTrustStorePassword(this.sslTrustStorePassword);
			}
			if (this.sslRequireClientCert) {
				checkArgument(this.sslTrustStorePath != null,
						"Client certificate authentication specified without "
								+ "specifying a trust store");
				checkArgument(this.sslTrustStorePassword != null,
						"Client certificate authentication specified without "
								+ "specifying a trust store password");
			}
			// if true: requires client to authenticate with certificate
			sslContextFactory.setNeedClientAuth(this.sslRequireClientCert);
			// if true: authenticates client certificate if provided
			sslContextFactory.setWantClientAuth(false);

			// HTTPS config
			HttpConfiguration httpsConfig = new HttpConfiguration();
			httpsConfig.addCustomizer(new SecureRequestCustomizer());
			httpsConfig.setOutputBufferSize(32768);
			// HTTPS connector
			ServerConnector https = new ServerConnector(server,
					new SslConnectionFactory(sslContextFactory, "http/1.1"),
					new HttpConnectionFactory(httpsConfig));
			https.setPort(this.httpsPort);

			server.addConnector(https);
		}

		return server;
	}

	/**
	 * Set the {@link Server}'s HTTP listen port. May be <code>null</code>, in
	 * which case the server won't listen to HTTP requests.
	 *
	 * @param port
	 * @return
	 */
	public BaseServerBuilder httpPort(Integer port) {
		this.httpPort = port;
		return this;
	}

	/**
	 * Set the {@link Server}'s HTTPS listen port. May be <code>null</code>, in
	 * which case the server won't listen to HTTPS requests. <i>Note: setting a
	 * HTTPS port requires an SSL key store to be set for server
	 * authentication.</i>
	 *
	 * @param port
	 * @return
	 */
	public BaseServerBuilder httpsPort(Integer port) {
		this.httpsPort = port;
		return this;
	}

	/**
	 * Set a file system path or URI to a SSL key store, which stores the
	 * server's SSL certificate that it will use to authenticate to clients.
	 * <i>Note: this option is only relevant for {@link Server}s with an HTTPS
	 * port, in which case it is required.</i>
	 *
	 * @param pathOrUri
	 * @return
	 */
	public BaseServerBuilder sslKeyStorePath(String pathOrUri) {
		this.sslKeyStorePath = pathOrUri;
		return this;
	}

	/**
	 * Set the type of the SSL key store. Defaults to
	 * {@link SslKeyStoreType#PKCS12}. <i>Note: this option is only relevant for
	 * {@link Server}s with an HTTPS port, in which case it is required.</i>
	 *
	 * @param type
	 * @return
	 */
	public BaseServerBuilder sslKeyStoreType(SslKeyStoreType type) {
		this.sslKeyStoreType = type;
		return this;
	}

	/**
	 * Set the password used to protect SSL key store. <i>Note: this option is
	 * only relevant for HTTPS {@link Server}s with an HTTPS port, in which case
	 * it is required.</i>
	 *
	 * @param password
	 * @return
	 */
	public BaseServerBuilder sslKeyStorePassword(String password) {
		this.sslKeyStorePassword = password;
		return this;
	}

	/**
	 * Set the password (if any) used to protect the specific server key within
	 * the key store. <i>Note: this option is only relevant for {@link Server}s
	 * with an HTTPS port.</i>
	 *
	 * @param password
	 * @return
	 */
	public BaseServerBuilder sslKeyPassword(String password) {
		this.sslKeyPassword = password;
		return this;
	}

	/**
	 * Set the file system path or URI to the SSL trust store, which stores
	 * trusted client certificates. <i>Note: this option is only relevant for
	 * {@link Server}s with an HTTPS port that require client certificate
	 * authentication. If no trust store is set, the server will use its key
	 * store as trust store.</i>
	 *
	 * @param pathOrUri
	 * @return
	 */
	public BaseServerBuilder sslTrustStorePath(String pathOrUri) {
		this.sslTrustStorePath = pathOrUri;
		return this;
	}

	/**
	 * Set the type of the SSL trust store. Defaults to
	 * {@link SslKeyStoreType#JKS} . <i>Note: this option is only relevant for
	 * {@link Server}s with an HTTPS port that require client certificate
	 * authentication.</i>
	 *
	 * @param type
	 * @return
	 */
	public BaseServerBuilder sslTrustStoreType(SslKeyStoreType type) {
		this.sslTrustStoreType = type;
		return this;
	}

	/**
	 * Set the password used to protect the SSL trust store. <i>Note: this
	 * option is only relevant for {@link Server}s with an HTTPS port that
	 * require client certificate authentication.</i>
	 *
	 * @param password
	 * @return
	 */
	public BaseServerBuilder sslTrustStorePassword(String password) {
		this.sslTrustStorePassword = password;
		return this;
	}

	/**
	 * If set to <code>true</code>, requires SSL clients to authenticate with a
	 * certificate.
	 *
	 * @param requireCertAuthentication
	 * @return
	 */
	public BaseServerBuilder sslRequireClientCert(
			boolean requireCertAuthentication) {
		this.sslRequireClientCert = requireCertAuthentication;
		return this;
	}

	/**
	 * Ensures the truth of an expression involving one or more parameters to
	 * the calling method.
	 *
	 * @param expression
	 *            a boolean expression
	 * @param errorMessage
	 *            the exception message to use if the check fails; will be
	 *            converted to a string using {@link String#valueOf(Object)}
	 * @throws IllegalArgumentException
	 *             if {@code expression} is false
	 */
	static void checkArgument(boolean expression, Object errorMessage) {
		if (!expression) {
			throw new IllegalArgumentException(String.valueOf(errorMessage));
		}
	}
}
