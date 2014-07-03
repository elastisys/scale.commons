package com.elastisys.scale.commons.net.url;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Convenience class for performing different kinds of URL operations.
 * 
 * 
 */
public class UrlUtils {

	private UrlUtils() {
		throw new UnsupportedOperationException(
				"not intended to be instantiated");
	}

	/**
	 * Encodes an HTTP(S) URL in such a way that illegal query string
	 * characters, such as "{" and "}", are properly encoded. See {@link URI}
	 * for a description of invalid characters in URIs.
	 * 
	 * @param httpUrl
	 *            The un-encoded HTTP URL.
	 * @return The corresponding encoded HTTP {@link URI}.
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static URI encodeHttpUrl(String httpUrl)
			throws MalformedURLException, URISyntaxException {
		// First build a URL (to be able to extract parts).
		URL url = new URL(httpUrl);
		// URI constructor takes care of encoding query part of URL
		String portPart = url.getPort() >= 0 ? ":" + url.getPort() : "";
		URI httpUri = new URI(url.getProtocol(), url.getHost() + portPart,
				url.getPath(), url.getQuery(), url.getRef());
		return httpUri;
	}
}
