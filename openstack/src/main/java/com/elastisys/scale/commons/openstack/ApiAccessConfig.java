package com.elastisys.scale.commons.openstack;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import java.util.Optional;

import com.elastisys.scale.commons.json.JsonUtils;

/**
 * Configuration object for an {@link OSClientFactory}, which declares how to
 * authenticate and what OpenStack region to operate against.
 * <p/>
 * The {@link OSClientFactory} can be configured to use either use version 2 or
 * version 3 of the
 * <a href="http://docs.openstack.org/developer/keystone/http-api.html#history"
 * >identity HTTP API</a>.
 */
public class ApiAccessConfig {

    /**
     * The default timeout in milliseconds until a connection is established.
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
    /**
     * The default socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is
     * the timeout for waiting for data or, put differently, a maximum period
     * inactivity between two consecutive data packets).
     */
    public static final int DEFAULT_SOCKET_TIMEOUT = 10000;

    /**
     * Declares how to authenticate with the OpenStack identity service
     * (Keystone).
     */
    private final AuthConfig auth;

    /**
     * The particular OpenStack region (out of the ones available in Keystone's
     * service catalog) to connect to. For example, {@code RegionOne}.
     */
    private final String region;

    /**
     * The timeout in milliseconds until a connection is established.
     */
    private final Integer connectionTimeout;

    /**
     * The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the
     * timeout for waiting for data or, put differently, a maximum period
     * inactivity between two consecutive data packets.
     */
    private final Integer socketTimeout;

    /**
     * Indicates if HTTP requests are to be logged. If <code>true</code>, each
     * HTTP request will be logged (using a logger named {@code os}). Default:
     * <code>false</code>.
     */
    private final boolean logHttpRequests;

    /**
     * Creates a new {@link ApiAccessConfig}.
     *
     * @param auth
     *            Declares how to authenticate with the OpenStack identity
     *            service (Keystone).
     * @param region
     *            The particular OpenStack region (out of the ones available in
     *            Keystone's service catalog) to connect to. For example,
     *            {@code RegionOne}.
     */
    public ApiAccessConfig(AuthConfig auth, String region) {
        this(auth, region, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Creates a new {@link ApiAccessConfig}.
     *
     * @param auth
     *            Declares how to authenticate with the OpenStack identity
     *            service (Keystone).
     * @param region
     *            The particular OpenStack region (out of the ones available in
     *            Keystone's service catalog) to connect to. For example,
     *            {@code RegionOne}.
     * @param connectionTimeout
     *            The timeout in milliseconds until a connection is established.
     *            May be <code>null</code>. Default:
     *            {@value #DEFAULT_CONNECTION_TIMEOUT} ms.
     * @param socketTimeout
     *            The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which
     *            is the timeout for waiting for data or, put differently, a
     *            maximum period inactivity between two consecutive data
     *            packets. May be <code>null</code>. Default:
     *            {@value #DEFAULT_SOCKET_TIMEOUT} ms.
     */
    public ApiAccessConfig(AuthConfig auth, String region, Integer connectionTimeout, Integer socketTimeout) {
        this(auth, region, connectionTimeout, socketTimeout, false);
        validate();
    }

    /**
     * Creates a new {@link ApiAccessConfig}.
     *
     * @param auth
     *            Declares how to authenticate with the OpenStack identity
     *            service (Keystone).
     * @param region
     *            The particular OpenStack region (out of the ones available in
     *            Keystone's service catalog) to connect to. For example,
     *            {@code RegionOne}.
     * @param connectionTimeout
     *            The timeout in milliseconds until a connection is established.
     *            May be <code>null</code>. Default:
     *            {@value #DEFAULT_CONNECTION_TIMEOUT} ms.
     * @param socketTimeout
     *            The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which
     *            is the timeout for waiting for data or, put differently, a
     *            maximum period inactivity between two consecutive data
     *            packets. May be <code>null</code>. Default:
     *            {@value #DEFAULT_SOCKET_TIMEOUT} ms.
     * @param logHttpRequests
     *            Indicates if HTTP requests are to be logged. If
     *            <code>true</code>, each HTTP request will be logged (using a
     *            logger named {@code os}). Default: <code>false</code>.
     */
    public ApiAccessConfig(AuthConfig auth, String region, Integer connectionTimeout, Integer socketTimeout,
            boolean logHttpRequests) {
        this.auth = auth;
        this.region = region;
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
        this.logHttpRequests = logHttpRequests;
        validate();
    }

    /**
     * Returns a description of how to authenticate with the OpenStack identity
     * service (Keystone).
     *
     * @return
     */
    public AuthConfig getAuth() {
        return this.auth;
    }

    /**
     * Returns the particular OpenStack region (out of the ones available in
     * Keystone's service catalog) to connect to. For example, {@code RegionOne}
     * .
     *
     * @return
     */
    public String getRegion() {
        return this.region;
    }

    /**
     * The timeout in milliseconds until a connection is established.
     *
     * @return
     */
    public Integer getConnectionTimeout() {
        return Optional.ofNullable(this.connectionTimeout).orElse(DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the
     * timeout for waiting for data or, put differently, a maximum period
     * inactivity between two consecutive data packets.
     *
     * @return
     */
    public Integer getSocketTimeout() {
        return Optional.ofNullable(this.socketTimeout).orElse(DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Indicates if HTTP requests should be logged. If <code>true</code>, each
     * HTTP request will be logged (using a logger named {@code os}).
     *
     * @return
     */
    public boolean shouldLogHttpRequests() {
        return this.logHttpRequests;
    }

    /**
     * Performs basic validation of this configuration. Throws an
     * {@link IllegalArgumentException} on failure to validate the
     * configuration.
     *
     * @throws IllegalArgumentException
     */
    public void validate() throws IllegalArgumentException {
        checkArgument(this.auth != null, "no auth method specified");
        checkArgument(this.region != null, "missing region");
        this.auth.validate();
        checkArgument(getConnectionTimeout() > 0, "connectionTimeout must be positive");
        checkArgument(getSocketTimeout() > 0, "socketTimeout must be positive");
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.auth, this.region, getConnectionTimeout(), getSocketTimeout());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApiAccessConfig) {
            ApiAccessConfig that = (ApiAccessConfig) obj;
            return Objects.equals(this.auth, that.auth) //
                    && Objects.equals(this.region, that.region) //
                    && Objects.equals(getConnectionTimeout(), that.getConnectionTimeout()) //
                    && Objects.equals(getSocketTimeout(), that.getSocketTimeout());
        }
        return false;
    }

    @Override
    public String toString() {
        return JsonUtils.toString(JsonUtils.toJson(this));
    }
}
