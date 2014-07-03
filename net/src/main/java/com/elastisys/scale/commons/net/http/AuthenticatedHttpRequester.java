package com.elastisys.scale.commons.net.http;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.elastisys.scale.commons.net.retryable.Requester;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;

/**
 * A {@link Requester} that, when executed, performs a HTTP request that is
 * authenticated via Basic authentication and/or a client certificate.
 * 
 * 
 */
public class AuthenticatedHttpRequester implements
		Requester<HttpRequestResponse> {

	/** The type of key store used to store the client certificate key. */
	private static final String KEYSTORE_TYPE = "PKCS12";

	/** Username/password credentials for basic authentication. */
	private final BasicCredentials basicCredentials;
	/** Certificate credentials for certificate-based client authentication. */
	private final CertificateCredentials certificateCredentials;
	/** The HTTP request to send. */
	private final HttpUriRequest request;

	/**
	 * Constructs a {@link AuthenticatedHttpRequester} with
	 * {@link CertificateCredentials}.
	 * 
	 * @param certificateCredentials
	 *            Certificate credentials for certificate-based client
	 *            authentication.
	 * @param request
	 *            The HTTP request to send.
	 */
	public AuthenticatedHttpRequester(
			CertificateCredentials certificateCredentials,
			HttpUriRequest request) {
		this(null, certificateCredentials, request);
	}

	/**
	 * Constructs a {@link AuthenticatedHttpRequester} with
	 * {@link BasicCredentials}.
	 * 
	 * @param basicCredentials
	 *            Username/password credentials for basic authentication.
	 * @param request
	 *            The HTTP request to send.
	 */
	public AuthenticatedHttpRequester(BasicCredentials basicCredentials,
			HttpUriRequest request) {
		this(basicCredentials, null, request);
	}

	/**
	 * Constructs a {@link AuthenticatedHttpRequester} with
	 * {@link BasicCredentials} and/or {@link CertificateCredentials}.
	 * 
	 * @param basicCredentials
	 *            Username/password credentials for basic authentication. May be
	 *            <code>null</code> if {@link CertificateCredentials} are
	 *            provided.
	 * @param certificateCredentials
	 *            Certificate credentials for certificate-based client
	 *            authentication. May be <code>null</code> if
	 *            {@link BasicCredentials} are provided.
	 * @param request
	 *            The HTTP request to send.
	 */
	public AuthenticatedHttpRequester(BasicCredentials basicCredentials,
			CertificateCredentials certificateCredentials,
			HttpUriRequest request) {
		checkArgument((basicCredentials != null)
				|| (certificateCredentials != null),
				"neither basic credentials nor certificate "
						+ "credentials were provided");
		checkNotNull(request, "missing HTTP request");

		this.basicCredentials = basicCredentials;
		this.certificateCredentials = certificateCredentials;
		this.request = request;
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
		if (this.certificateCredentials != null) {
			String keystorePath = this.certificateCredentials.getKeystorePath();
			String keystorePassword = this.certificateCredentials
					.getKeystorePassword();
			// fall back to keystore password if key password is missing
			String keyPassword = this.certificateCredentials.getKeyPassword()
					.or(keystorePassword);
			KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
			keyStore.load(new FileInputStream(keystorePath),
					keystorePassword.toCharArray());
			sslContextBuilder.loadKeyMaterial(keyStore,
					keyPassword.toCharArray());
		}

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		if (this.basicCredentials != null) {
			String username = this.basicCredentials.getUsername();
			String password = this.basicCredentials.getPassword();
			credentialsProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(username, password));
		}
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCredentialsProvider(credentialsProvider)
				.setSslcontext(sslContextBuilder.build())
				.setHostnameVerifier(hostnameVerifier).build();
		return httpclient;
	}

	/**
	 * Sends the HTTP request to the remote end-point and returns a
	 * {@link HttpRequestResponse} object holding the response message status,
	 * body, and headers.
	 * 
	 * @return The received response.
	 * @throws IOException
	 *             If anything went wrong.
	 */
	@Override
	public HttpRequestResponse call() throws IOException {
		CloseableHttpClient client;
		try {
			client = prepareAuthenticatingClient();
		} catch (Exception e) {
			throw new IOException(format(
					"failed to prepare http client for request (%s): %s",
					this.request, e.getMessage()), e);
		}

		try {
			CloseableHttpResponse httpResponse = null;
			try {
				httpResponse = client.execute(this.request);
			} catch (Exception e) {
				throw new IOException(format("failed to send request (%s): %s",
						this.request, e.getMessage()), e);
			}

			HttpRequestResponse response = new HttpRequestResponse(httpResponse);
			int responseCode = response.getStatusCode();
			String responseBody = response.getResponseBody();
			if (responseCode != HttpStatus.SC_OK) {
				throw new IOException(format(
						"error response received from remote endpoint "
								+ "on request (%s): %s: %s", this.request,
						responseCode, responseBody));
			}
			return response;
		} finally {
			client.close();
		}
	}
}
