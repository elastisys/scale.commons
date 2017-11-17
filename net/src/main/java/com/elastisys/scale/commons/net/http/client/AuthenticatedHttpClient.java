package com.elastisys.scale.commons.net.http.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.net.http.Http;
import com.elastisys.scale.commons.net.http.HttpBuilder;
import com.elastisys.scale.commons.net.http.HttpRequestResponse;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;

/**
 * Performs HTTP requests that may optionally authenticate using Basic
 * authentication, client certificate authentication or both.
 * <p/>
 * This class is thread-safe.
 */
public class AuthenticatedHttpClient {
    private static Logger LOG = LoggerFactory.getLogger(AuthenticatedHttpClient.class);

    /**
     * The default timeout in milliseconds until a connection is established. A
     * timeout value of zero is interpreted as an infinite timeout. A negative
     * value is interpreted as undefined (system default).
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    /**
     * The default socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is
     * the timeout for waiting for data or, put differently, a maximum period
     * inactivity between two consecutive data packets). A timeout value of zero
     * is interpreted as an infinite timeout. A negative value is interpreted as
     * undefined (system default).
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 60000;

    /** HTTP(S) client. */
    private final Http http;

    /**
     * Constructs an {@link AuthenticatedHttpClient} that doesn't attempt to do
     * any authentication and that uses default socket and connection timeouts.
     */
    public AuthenticatedHttpClient() {
        this(LOG, Optional.empty(), Optional.empty(), DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
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
        this(LOG, basicCredentials, certificateCredentials, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Constructs a {@link AuthenticatedHttpClient}.
     *
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
    public AuthenticatedHttpClient(Optional<BasicCredentials> basicCredentials,
            Optional<CertificateCredentials> certificateCredentials, int connectTimeout, int socketTimeout) {
        this(LOG, basicCredentials, certificateCredentials, connectTimeout, socketTimeout);
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
    public AuthenticatedHttpClient(Logger logger, Optional<BasicCredentials> basicCredentials,
            Optional<CertificateCredentials> certificateCredentials, int connectTimeout, int socketTimeout) {
        checkNotNull(logger, "null logger provided");
        HttpBuilder httpBuilder = Http.builder().logger(logger);
        if (basicCredentials.isPresent()) {
            httpBuilder.clientBasicAuth(basicCredentials.get());
        }
        if (certificateCredentials.isPresent()) {
            httpBuilder.clientCertAuth(certificateCredentials.get());
        }
        httpBuilder.connectionTimeout(connectTimeout);
        httpBuilder.socketTimeout(socketTimeout);
        this.http = httpBuilder.build();
    }

    /**
     * Sends a HTTP request to a remote endpoint and returns a
     * {@link HttpRequestResponse} object holding the response message status,
     * body, and headers. On failure to send the request, an {@link IOException}
     * is thr own. If a HTTP response is received but the response code is not a
     * {@code 2XX} one, a {@link HttpResponseException} is raised.
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
        return this.http.execute(request);
    }
}
