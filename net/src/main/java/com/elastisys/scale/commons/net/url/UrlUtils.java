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
        throw new UnsupportedOperationException("not intended to be instantiated");
    }

    /**
     * Converts a given string to its corresponding {@link URL}. Instead of
     * failing with the {@link URL}-constructor's checked
     * {@link MalformedURLException}, this method will convert that exception to
     * an unchecked {@link IllegalArgumentException}.
     *
     * @param url
     * @return
     * @throws IllegalArgumentException
     *             If the {@code url} is malformed or invalid.
     */
    public static URL url(String url) throws IllegalArgumentException {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("illegal URL: " + e.getMessage(), e);
        }
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
    public static URI encodeHttpUrl(String httpUrl) throws MalformedURLException, URISyntaxException {
        // First build a URL (to be able to extract parts).
        URL url = new URL(httpUrl);
        // URI constructor takes care of encoding query part of URL
        String portPart = url.getPort() >= 0 ? ":" + url.getPort() : "";
        URI httpUri = new URI(url.getProtocol(), url.getHost() + portPart, url.getPath(), url.getQuery(), url.getRef());
        return httpUri;
    }

    /**
     * Extracts the "basename" of an URL or path -- the string following that
     * last {@code /} in the path/URL.
     * <p/>
     * For example, the basename for URL
     * {@code https://www.googleapis.com/compute/v1/projects/my-project/zones/europe-west1-b}
     * would be {@code europe-west1-b}.
     *
     * @param urlOrPath
     *            A URL or a slash-separated file path.
     * @return
     */
    public static String basename(String urlOrPath) {
        int lastSlashIndex = urlOrPath.lastIndexOf("/");
        if (lastSlashIndex < 0) {
            return urlOrPath;
        }
        return urlOrPath.substring(lastSlashIndex + 1);
    }
}
