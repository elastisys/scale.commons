package com.elastisys.scale.commons.net.http;

import static java.lang.String.format;

import java.io.IOException;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Range;

/**
 * A HTTP(S) client. Use {@link Http#builder()} to construct a new instance.
 * <p/>
 * This class is thread-safe.
 *
 * @see HttpBuilder
 */
public class Http {
    /** Default logger instance to use. */
    static Logger LOG = LoggerFactory.getLogger(Http.class);

    /**
     * The {@link HttpClientBuilder} used to instantiate a new
     * {@link CloseableHttpClient} for every call to
     * {@link #execute(HttpRequestBase)}.
     */
    private final HttpClientBuilder clientBuilder;
    /** Logger instance to log to. */
    private final Logger logger;

    Http(HttpClientBuilder clientBuilder) {
        this(clientBuilder, LOG);
    }

    Http(HttpClientBuilder clientBuilder, Logger logger) {
        this.clientBuilder = clientBuilder;
        this.logger = logger;
    }

    /**
     * Creates a {@link HttpBuilder} from which a {@link Http} instance can be
     * instantiated.
     *
     * @return
     */
    public static HttpBuilder builder() {
        return new HttpBuilder();
    }

    /**
     * Sends a HTTP request to a remote endpoint and returns a
     * {@link HttpRequestResponse} object holding the response message status,
     * body, and headers. On failure to complete the request, an
     * {@link IOException} is thrown. If a HTTP response is received but the
     * response code is not a {@code 2XX} one, a {@link HttpResponseException}
     * is raised.
     * <p/>
     * On return the response has been fully consumed, the connection is closed,
     * and any system resources used for connection establishment have been
     * released.
     *
     * @param request
     *            The request to send.
     * @return The received response.
     * @throws HttpResponseException
     *             If a HTTP response was received with non-{@code 2XX} status
     *             code.
     * @throws IOException
     *             On failure to send the request.
     */
    public HttpRequestResponse execute(HttpRequestBase request) throws HttpResponseException, IOException {

        CloseableHttpClient client = this.clientBuilder.build();
        try {
            CloseableHttpResponse httpResponse = null;
            try {
                this.logger.debug(format("sending request (%s)", request));
                httpResponse = client.execute(request);
            } catch (Exception e) {
                Throwables.propagateIfInstanceOf(e, IOException.class);
                throw new IOException(format("failed to send request (%s): %s", request, e.getMessage()), e);
            }

            HttpRequestResponse response = new HttpRequestResponse(httpResponse);
            int responseCode = response.getStatusCode();
            String responseBody = response.getResponseBody();
            // raise error if response code is not 2XX
            if (!Range.closed(200, 299).contains(responseCode)) {
                throw new HttpResponseException(responseCode,
                        format("error response received from remote endpoint " + "on request (%s): %s:\n%s", request,
                                responseCode, responseBody));
            }
            return response;
        } finally {
            HttpClientUtils.closeQuietly(client);
        }
    }
}
