package com.elastisys.scale.commons.net.http;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.elastisys.scale.commons.net.retryable.Requester;
import com.elastisys.scale.commons.net.retryable.RetryableRequest;

/**
 * A {@link Requester} that performs a HTTP GET against a certain URL.
 * 
 * @see RetryableRequest
 * 
 * 
 */
public class HttpGetRequester implements Requester<HttpRequestResponse> {

	/** Default connection timeout (in ms). */
	private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
	/** Default socket timeout (in ms). */
	private static final int DEFAULT_SOCKET_TIMEOUT = 10000;

	/** The HTTP URL that will be requested. */
	private final String url;

	/** Configuration parameters to use for the request. */
	private final RequestConfig requestConfig;

	/**
	 * Constructs a new {@link HttpGetRequester} that requests a certain HTTP
	 * URL with default request configuration.
	 * 
	 * @param url
	 *            The HTTP URL that will be requested.
	 */
	public HttpGetRequester(String url) {
		this(url, defaultRequestConfig());
	}

	/**
	 * Constructs a new {@link HttpGetRequester} that requests a certain HTTP
	 * URL with.
	 * 
	 * @param url
	 *            The HTTP URL that will be requested.
	 * @param requestConfig
	 *            Configuration parameters to use for the request.
	 */
	public HttpGetRequester(String url, RequestConfig requestConfig) {
		checkNotNull(url, "URL cannot be null");
		checkNotNull(requestConfig, "Request configuration cannot be null");
		this.url = url;
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
			response = doGet(httpclient, this.url);
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

	private CloseableHttpClient prepareClient() throws Exception {
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

	private CloseableHttpResponse doGet(final CloseableHttpClient httpclient,
			final String url) throws ClientProtocolException, IOException {
		HttpGet httpget = new HttpGet(url);
		CloseableHttpResponse response = httpclient.execute(httpget);
		return response;
	}

}
