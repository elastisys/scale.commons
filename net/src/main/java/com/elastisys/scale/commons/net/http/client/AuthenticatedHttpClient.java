package com.elastisys.scale.commons.net.http.client;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.net.http.HttpRequestResponse;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Range;

/**
 * Performs HTTP requests that may optionally authenticate using Basic
 * authentication, client certificate authentication or both.
 */
public class AuthenticatedHttpClient {
	private static Logger LOG = LoggerFactory
			.getLogger(AuthenticatedHttpClient.class);

	/** Default value for {@link #connectTimeout} in ms. */
	private final static int DEFAULT_CONNECTION_TIMEOUT = 10000;
	/** Default value for {@link #socketTimeout} in ms. */
	private final static int DEFAULT_SOCKET_TIMEOUT = 10000;

	/** The {@link Logger} instance to make use of. */
	private final Logger logger;
	/** Username/password credentials for basic authentication. */
	private final Optional<BasicCredentials> basicCredentials;
	/** Certificate credentials for certificate-based client authentication. */
	private final Optional<CertificateCredentials> certificateCredentials;

	/**
	 * The timeout in milliseconds until a connection is established. A timeout
	 * value of zero is interpreted as an infinite timeout. A negative value is
	 * interpreted as undefined (system default).
	 */
	private final int connectTimeout;

	/**
	 * The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the
	 * timeout for waiting for data or, put differently, a maximum period
	 * inactivity between two consecutive data packets). A timeout value of zero
	 * is interpreted as an infinite timeout. A negative value is interpreted as
	 * undefined (system default).
	 */
	private final int socketTimeout;

	/**
	 * Constructs an {@link AuthenticatedHttpClient} that doesn't attempt to do
	 * any authentication and that uses default socket and connection timeouts.
	 */
	public AuthenticatedHttpClient() {
		this(LOG, Optional.<BasicCredentials> absent(), Optional
				.<CertificateCredentials> absent(), DEFAULT_CONNECTION_TIMEOUT,
				DEFAULT_SOCKET_TIMEOUT);
	}

	/**
	 * Constructs an {@link AuthenticatedHttpClient} with default socket and
	 * connection timeouts.
	 *
	 * @param basicCredentials
	 *            Username/password credentials for basic authentication.
	 * @param certificateCredentials
	 *            Certificate credentials for certificate-based client
	 *            authentication.
	 */
	public AuthenticatedHttpClient(Optional<BasicCredentials> basicCredentials,
			Optional<CertificateCredentials> certificateCredentials) {
		this(LOG, basicCredentials, certificateCredentials,
				DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
	}

	/**
	 * Constructs a {@link AuthenticatedHttpClient}.
	 *
	 * @param logger
	 *            The {@link Logger} instance to make use of.
	 * @param basicCredentials
	 *            Username/password credentials for basic authentication.
	 * @param certificateCredentials
	 *            Certificate credentials for certificate-based client
	 *            authentication.
	 * @param connectTimeout
	 *            The timeout in milliseconds until a connection is established.
	 *            A timeout value of zero is interpreted as an infinite timeout.
	 *            A negative value is interpreted as undefined (system default).
	 * @param socketTimeout
	 *            The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which
	 *            is the timeout for waiting for data or, put differently, a
	 *            maximum period inactivity between two consecutive data
	 *            packets). A timeout value of zero is interpreted as an
	 *            infinite timeout. A negative value is interpreted as undefined
	 *            (system default).
	 */
	public AuthenticatedHttpClient(Logger logger,
			Optional<BasicCredentials> basicCredentials,
			Optional<CertificateCredentials> certificateCredentials,
			int connectTimeout, int socketTimeout) {
		checkNotNull(logger, "null logger provided");
		this.logger = logger;
		this.basicCredentials = basicCredentials;
		this.certificateCredentials = certificateCredentials;
		this.connectTimeout = connectTimeout;
		this.socketTimeout = socketTimeout;
	}

	/**
	 * Prepares a http(s) client configured with {@link CertificateCredentials}
	 * and/or {@link BasicCredentials}.
	 *
	 * @return
	 * @throws CloudAdapterException
	 */
	private CloseableHttpClient prepareAuthenticatingClient() throws Exception {
		// install host name verifier that always approves host names
		AllowAllHostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
		// for SSL requests we should accept self-signed host certificates
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		SSLContextBuilder sslContextBuilder = SSLContexts.custom()
				.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy());

		// first attempt to prepare a https client with certificate credentials
		if (this.certificateCredentials.isPresent()) {
			CertificateCredentials certCredentials = this.certificateCredentials
					.get();
			String keystorePath = certCredentials.getKeystorePath();
			String keystorePassword = certCredentials.getKeystorePassword();
			// fall back to keystore password if key password is missing
			String keyPassword = certCredentials.getKeyPassword().or(
					keystorePassword);
			this.logger.debug(
					"using client-side certificate from keystore '{}'",
					keystorePath);
			KeyStore keyStore = KeyStore.getInstance(certCredentials
					.getKeystoreType().name());
			keyStore.load(new FileInputStream(keystorePath),
					keystorePassword.toCharArray());
			sslContextBuilder.loadKeyMaterial(keyStore,
					keyPassword.toCharArray());
		}

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		Optional<BasicCredentials> basicCredentials = this.basicCredentials;
		if (basicCredentials.isPresent()) {
			String username = basicCredentials.get().getUsername();
			String password = basicCredentials.get().getPassword();
			this.logger
					.debug("passing Basic authentication credentials for username '{}'",
							username);
			credentialsProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(username, password));
		}

		CloseableHttpClient httpclient = HttpClients.custom()
				.setRedirectStrategy(new LaxRedirectStrategy())
				.setDefaultCredentialsProvider(credentialsProvider)
				.setSslcontext(sslContextBuilder.build())
				.setHostnameVerifier(hostnameVerifier).build();
		return httpclient;
	}

	/**
	 * Sends a HTTP request to a remote endpoint and returns a
	 * {@link HttpRequestResponse} object holding the response message status,
	 * body, and headers. If the response code is not a 2XX one, a
	 * {@link HttpResponseException} is raised.
	 *
	 *
	 * @param request
	 *            The request to send.
	 * @return The received response.
	 * @throws HttpResponseException
	 *             If a non-2XX response was received.
	 * @throws IOException
	 *             If anything went wrong.
	 */
	public HttpRequestResponse execute(HttpRequestBase request)
			throws HttpResponseException, IOException {
		CloseableHttpClient client;
		try {
			client = prepareAuthenticatingClient();
		} catch (Exception e) {
			throw new IOException(format(
					"failed to prepare http client for request (%s): %s",
					request, e.getMessage()), e);
		}

		RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
				.setSocketTimeout(this.socketTimeout)
				.setConnectTimeout(this.connectTimeout).build();
		request.setConfig(requestConfig);

		try {
			CloseableHttpResponse httpResponse = null;
			try {
				this.logger.info(format("sending request (%s)", request));
				httpResponse = client.execute(request);
			} catch (Exception e) {
				Throwables.propagateIfInstanceOf(e, IOException.class);
				throw new IOException(format("failed to send request (%s): %s",
						request, e.getMessage()), e);
			}

			HttpRequestResponse response = new HttpRequestResponse(httpResponse);
			int responseCode = response.getStatusCode();
			String responseBody = response.getResponseBody();
			// raise error if response code is not 2XX
			if (!Range.closed(200, 299).contains(responseCode)) {
				throw new HttpResponseException(responseCode, format(
						"error response received from remote endpoint "
								+ "on request (%s): %s: %s", request,
						responseCode, responseBody));
			}
			return response;
		} finally {
			client.close();
		}
	}
}
