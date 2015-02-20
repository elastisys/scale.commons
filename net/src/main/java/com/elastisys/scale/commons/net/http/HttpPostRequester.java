package com.elastisys.scale.commons.net.http;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * A {@link Callable} that performs a HTTP POST.
 */
public class HttpPostRequester implements Callable<HttpRequestResponse> {

	/** Default connection timeout (in ms). */
	private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
	/** Default socket timeout (in ms). */
	private static final int DEFAULT_SOCKET_TIMEOUT = 10000;

	/** The HTTP POST request to submit. */
	private final HttpPost httpPost;

	/** Configuration parameters to use for the request. */
	private final RequestConfig requestConfig;

	/**
	 * Constructs a new {@link HttpGetRequester} that requests a certain HTTP
	 * URL with default request configuration.
	 *
	 * @param httpPost
	 *            The HTTP POST request to submit.
	 */
	public HttpPostRequester(HttpPost httpPost) {
		this(httpPost, defaultRequestConfig());
	}

	/**
	 * Constructs a new {@link HttpGetRequester} that requests a certain HTTP
	 * URL with.
	 *
	 * @param httpPost
	 *            The HTTP POST request to submit.
	 * @param requestConfig
	 *            Configuration parameters to use for the request.
	 */
	public HttpPostRequester(HttpPost httpPost, RequestConfig requestConfig) {
		checkNotNull(httpPost, "POST request cannot be null");
		checkNotNull(requestConfig, "request configuration cannot be null");
		this.httpPost = httpPost;
		this.requestConfig = requestConfig;
		limitDnsCacheTtl(30);
	}

	private static RequestConfig defaultRequestConfig() {
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
				.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT).build();
		return requestConfig;
	}

	/**
	 * Limits the time-to-live of the JVM's DNS cache (in seconds) to, for
	 * example, prevent IP address resolution problems when resolving
	 * dynamically created IP addresses (such as those associated with Amazon
	 * resources).
	 * <p/>
	 * See <a href=
	 * "http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/java-dg-jvm-ttl.html"
	 * >the AWS developer guide</a> for more details.
	 */
	private void limitDnsCacheTtl(int ttlInSeconds) {
		java.security.Security.setProperty("networkaddress.cache.ttl", ""
				+ ttlInSeconds);
	}

	@Override
	public HttpRequestResponse call() throws Exception {
		CloseableHttpClient httpclient = prepareClient();
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(this.httpPost);
			// fully consume the HttpResponse stream and return the read
			// contents. this allows the connection to be closed before
			// returning to the client.
			return toHttpGetResponse(response);
		} finally {
			if (response != null) {
				response.close();
			}
			httpclient.close();
		}
	}

	private CloseableHttpClient prepareClient() throws KeyStoreException,
			NoSuchAlgorithmException, KeyManagementException {
		// install host name verifier that always approves host names
		AllowAllHostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
		// for SSL requests we should accept self-signed host certificates
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		SSLContext sslContext = SSLContexts.custom()
				.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
				.build();
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultRequestConfig(this.requestConfig)
				.setSslcontext(sslContext)
				.setHostnameVerifier(hostnameVerifier).build();
		return httpclient;
	}

	/**
	 * Converts a {@link HttpResponse} to a {@link HttpRequestResponse} by fully
	 * consuming the {@link HttpResponse}'s {@link InputStream}.
	 *
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private HttpRequestResponse toHttpGetResponse(HttpResponse response)
			throws IOException {
		return new HttpRequestResponse(response);
	}
}
