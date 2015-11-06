package com.elastisys.scale.commons.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

/**
 * Represents a response to a HTTP request.
 */
public class HttpRequestResponse {
	/**
	 * Regular expression to parse out the charset from a Content-Type HTTP
	 * header.
	 */
	private static final Pattern CHARSET_PATTERN = Pattern
			.compile(".*charset=([A-Za-z0-9\\-]+).*");

	/** The status code of the HTTP response. */
	private final int statusCode;
	/** The headers of the HTTP response. */
	private final Collection<Header> headers;
	/** The message body of the HTTP response. */
	private final String responseBody;

	/**
	 * Constructs a {@link HttpRequestResponse} from a {@link HttpResponse} by
	 * consuming the {@link HttpResponse}'s {@link InputStream} and closing the
	 * original {@link HttpResponse}.
	 * <p/>
	 * If the character encoding is specified in the response, it will be used
	 * to interpret the {@link InputStream}. Lacking an explicit encoding, the
	 * response message is assumed to be encoded in UTF-8.
	 *
	 * @param httpResponse
	 *            A {@link HttpResponse} that will be consumed. That is, the
	 *            {@link HttpResponse}'s {@link InputStream} will be consumed
	 *            and closed when the method returns.
	 * @throws IOException
	 */
	public HttpRequestResponse(CloseableHttpResponse httpResponse)
			throws IOException {
		this(httpResponse, Charsets.UTF_8);
	}

	/**
	 * Constructs a {@link HttpRequestResponse} from a {@link HttpResponse} by
	 * consuming the {@link HttpResponse}'s {@link InputStream} and closing the
	 * original {@link HttpResponse}. This constructor uses a particular
	 * fall-back character encoding to interpret the content with should the
	 * character encoding not be possible to determine from the response itself.
	 *
	 * @param httpResponse
	 *            A {@link HttpResponse} that will be consumed. That is, the
	 *            {@link HttpResponse}'s {@link InputStream} will be consumed
	 *            and closed when the method returns.
	 * @param fallbackCharset
	 *            The character set used to interpret the message body, if the
	 *            character encoding cannot be determined from the response
	 *            itself.
	 * @throws IOException
	 */
	public HttpRequestResponse(CloseableHttpResponse httpResponse,
			Charset fallbackCharset) throws IOException {
		this.statusCode = httpResponse.getStatusLine().getStatusCode();
		this.headers = Lists.newArrayList(httpResponse.getAllHeaders());
		Charset responseCharset = determineCharset(httpResponse);
		Charset charset = responseCharset == null ? fallbackCharset
				: responseCharset;

		// http response may not contain a message body, for example on 204 (No
		// Content) responses
		if (httpResponse.getEntity() != null) {
			this.responseBody = EntityUtils.toString(httpResponse.getEntity(),
					charset);
		} else {
			this.responseBody = null;
		}
		httpResponse.close();
	}

	/**
	 * Tries to determine the character encoding of a {@link HttpResponse}.
	 * Returns <code>null</code> if no character set was specified in the
	 * Content-Type header or if the charset was unrecognized.
	 *
	 * @param httpResponse
	 * @return The response's charset or <code>null</code> if unable to
	 *         determine it.
	 */
	private Charset determineCharset(HttpResponse httpResponse) {
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			Header contentType = entity.getContentType();
			if (contentType != null) {
				Matcher matcher = CHARSET_PATTERN
						.matcher(contentType.getValue());
				if (matcher.matches()) {
					String charset = matcher.group(1);
					try {
						return Charset.forName(charset);
					} catch (Exception e) {
						// unrecognized charset
						return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the status code of the HTTP response.
	 *
	 * @return
	 */
	public int getStatusCode() {
		return this.statusCode;
	}

	/**
	 * Returns the headers of the HTTP response.
	 *
	 * @return
	 */
	public Collection<Header> getHeaders() {
		return this.headers;
	}

	/**
	 * Returns the message body of the HTTP response or <code>null</code> if the
	 * response contained no body (this could be the case on
	 * {@code 204 (No Content)} responses.
	 *
	 * @return
	 */
	public String getResponseBody() {
		return this.responseBody;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("statusCode", this.statusCode).add("headers", this.headers)
				.toString();
	}

	/**
	 * Returns a {@link Predicate} that accepts {@link HttpRequestResponse}s
	 * with a successfull status code {@code 2XX}.
	 *
	 * @return
	 */
	public static Predicate<HttpRequestResponse> isOkResponse() {
		return response -> Range.closed(200, 299)
				.contains(response.getStatusCode());
	}
}
