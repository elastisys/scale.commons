package com.elastisys.scale.commons.openstack;

import static com.google.common.base.Preconditions.checkArgument;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.OSClient.OSClientV2;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.client.IOSClientBuilder.V2;
import org.openstack4j.api.client.IOSClientBuilder.V3;
import org.openstack4j.core.transport.Config;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.internal.OSClientSession;
import org.openstack4j.openstack.internal.OSClientSession.OSClientSessionV2;
import org.openstack4j.openstack.internal.OSClientSession.OSClientSessionV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating authenticated {@link OSClient} objects ready for use.
 */
public class OSClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(OSClientFactory.class);
    /**
     * API access configuration that describes how to authenticate with and
     * communicate over the OpenStack API.
     */
    private final ApiAccessConfig apiAccessConfig;

    /**
     * The {@link OSClientBuilder} used to instantiate {@link OSClient}s for
     * different auth schemes.
     */
    private final OSClientBuilder clientBuilder;

    /**
     * An authenticated client that, after initialization, will serve as a
     * template for creating additional per-thread {@link OSClient}s that re-use
     * the same authentication token. As explained
     * <a href="http://www.openstack4j.com/learn/threads/">here</a>, client
     * sessions are thread-scoped but an authentication token from an
     * authenticated client can be re-used by {@link OSClient}s bound to other
     * threads.
     */
    private OSClient<?> authenticatedClient = null;

    /** Mutex to protect critical sections. */
    private Object lock = new Object();

    /**
     * Creates an {@link OSClientFactory} with a {@link StandardOSClientCreator}
     * creating clients according to the given configuration.
     *
     * @param apiAccessConfig
     *            API access configuration that describes how to authenticate
     *            with and communicate over the OpenStack API.
     */
    public OSClientFactory(ApiAccessConfig apiAccessConfig) {
        this(apiAccessConfig, new StandardOSClientBuilder(apiAccessConfig.getConnectionTimeout(),
                apiAccessConfig.getSocketTimeout()));
    }

    /**
     * Creates an {@link OSClientFactory} with a custom {@link OSClientCreator}.
     *
     * @param apiAccessConfig
     *            API access configuration that describes how to authenticate
     *            with and communicate over the OpenStack API.
     *
     */
    public OSClientFactory(ApiAccessConfig apiAccessConfig, OSClientBuilder clientBuilder) {
        checkArgument(apiAccessConfig != null, "no apiAccessConfig given");
        checkArgument(clientBuilder != null, "no clientBuilder given");
        apiAccessConfig.validate();

        this.apiAccessConfig = apiAccessConfig;
        this.clientBuilder = clientBuilder;
    }

    /**
     * Returns an OpenStack API client, authenticated and configured according
     * to the {@link ApiAccessConfig} provided at construction-time.
     * <p/>
     * <b>Note</b>: the returned {@link OSClient} is bound to the calling thread
     * (since Openstack4j uses thread-scoped sessions) and should therefore
     * never be used by a different thread. If another thread needs to make use
     * of an {@link OSClient}, pass the {@link OSClientFactory} to that thread
     * and make a new call to {@link #authenticatedClient()}.
     *
     * @return An authenticated {@link OSClient} ready for use.
     */
    public OSClient<?> authenticatedClient() {
        // check if we need to do the first-time initialization of the seed
        // client
        synchronized (this.lock) {
            if (this.authenticatedClient == null) {
                this.authenticatedClient = acquireAuthenticatedClient();
                this.authenticatedClient.useRegion(this.apiAccessConfig.getRegion());
            }
        }

        // check if a client session is already bound to this thread and, if so,
        // return that client.
        if (OSClientSession.getCurrent() != null) {
            if (this.authenticatedClient instanceof OSClientV2) {
                return (OSClientSessionV2) OSClientSession.getCurrent();
            } else {
                return (OSClientSessionV3) OSClientSession.getCurrent();
            }
        } else {
            // if no client session is already bound to this thread, a copy
            // that reuses the same auth token as the template client is bound
            // to serve the current thread.
            OSClient<?> threadClient;
            if (this.authenticatedClient instanceof OSClientV2) {
                OSClientV2 client = (OSClientV2) this.authenticatedClient;
                threadClient = OSFactory.clientFromAccess(client.getAccess());
            } else {
                OSClientV3 client = (OSClientV3) this.authenticatedClient;
                threadClient = OSFactory.clientFromToken(client.getToken());
            }
            return threadClient.useRegion(this.apiAccessConfig.getRegion());
        }
    }

    /**
     * Creates a new {@link OSClient} by authenticating against a Keystone
     * identity service using the authentication scheme specified in the
     * {@link ApiAccessConfig} supplied at construction-time.
     *
     * @return An authenticated {@link OSClient} ready for use.
     */
    OSClient<?> acquireAuthenticatedClient() {
        AuthConfig auth = this.apiAccessConfig.getAuth();
        checkArgument(auth.getKeystoneUrl() != null, "cannot authenticate without a keystone endpoint URL");
        checkArgument(auth.isV2Auth() ^ auth.isV3Auth(),
                "*either* version 2 or version 3 style " + "authentication needs to be specified");

        LOG.debug("acquiring an authenticated openstack client ...");

        if (auth.isV2Auth()) {
            AuthV2Credentials v2Creds = auth.getV2Credentials();
            return this.clientBuilder.v2Auth().keyStoneUrl(auth.getKeystoneUrl()).tenantName(v2Creds.getTenantName())
                    .credentials(v2Creds.getUserName(), v2Creds.getPassword()).createAuthenticated();
        } else {
            OSClientV3Builder v3Builder = this.clientBuilder.v3Auth().keyStoneUrl(auth.getKeystoneUrl());

            AuthV3Credentials v3Creds = auth.getV3Credentials();

            if (v3Creds.getUserId() != null) {
                v3Builder.credentials(v3Creds.getUserId(), v3Creds.getPassword());
            } else {
                v3Builder.credentials(v3Creds.getUserName(), v3Creds.getUserDomain(), v3Creds.getPassword());
            }

            if (v3Creds.getProjectId() != null) {
                v3Builder.project(v3Creds.getProjectId());
            } else {
                v3Builder.project(v3Creds.getProjectName(), v3Creds.getProjectDomain());
            }
            return v3Builder.createAuthenticated();
        }
    }

    /**
     * Returns the API access configuration that describes how to authenticate
     * with and communicate over the OpenStack API.
     *
     * @return
     */
    public ApiAccessConfig getApiAccessConfig() {
        return this.apiAccessConfig;
    }

    /**
     * {@link OSClientBuilder} creation methods for different kinds of
     * authentication schemes.
     */
    interface OSClientBuilder {
        OSClientV2Builder v2Auth();

        OSClientV3Builder v3Auth();
    }

    interface OSClientV2Builder {
        OSClientV2Builder keyStoneUrl(String keystoneUrl);

        OSClientV2Builder tenantName(String tenantName);

        OSClientV2Builder credentials(String userName, String password);

        OSClientV2 createAuthenticated();
    }

    interface OSClientV3Builder {
        OSClientV3Builder keyStoneUrl(String keystoneUrl);

        OSClientV3Builder credentials(String userId, String password);

        OSClientV3Builder credentials(String userName, Domain userDomain, String password);

        OSClientV3Builder project(String projectId);

        OSClientV3Builder project(String projectName, Domain projectDomain);

        OSClientV3 createAuthenticated();
    }

    /**
     * Default {@link OSClientCreator} implementation.
     */
    private static class StandardOSClientBuilder implements OSClientBuilder {

        /**
         * The timeout in milliseconds until a connection is established.
         */
        private final int connectionTimeout;

        /**
         * The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the
         * timeout for waiting for data or, put differently, a maximum period
         * inactivity between two consecutive data packets).
         */
        private final int socketTimeout;

        /**
         * Creates a new {@link StandardOSClientCreator}.
         *
         * @param connectionTimeout
         *            The timeout in milliseconds until a connection is
         *            established.
         * @param socketTimeout
         *            The socket timeout ({@code SO_TIMEOUT}) in milliseconds,
         *            which is the timeout for waiting for data or, put
         *            differently, a maximum period inactivity between two
         *            consecutive data packets).
         */
        public StandardOSClientBuilder(int connectionTimeout, int socketTimeout) {
            this.connectionTimeout = connectionTimeout;
            this.socketTimeout = socketTimeout;
        }

        private Config clientConfig() {
            return Config.newConfig().withConnectionTimeout(this.connectionTimeout).withReadTimeout(this.socketTimeout);
        }

        @Override
        public OSClientV2Builder v2Auth() {
            return new StandardOSClientV2Builder(OSFactory.builderV2().withConfig(clientConfig()));
        }

        @Override
        public OSClientV3Builder v3Auth() {
            return new StandardOSClientV3Builder(OSFactory.builderV3().withConfig(clientConfig()));
        }

    }

    private static class StandardOSClientV2Builder implements OSClientV2Builder {

        private final V2 factory;

        public StandardOSClientV2Builder(V2 factory) {
            this.factory = factory;
        }

        @Override
        public OSClientV2Builder keyStoneUrl(String keystoneUrl) {
            this.factory.endpoint(keystoneUrl);
            return this;
        }

        @Override
        public OSClientV2Builder tenantName(String tenantName) {
            this.factory.tenantName(tenantName);
            return this;
        }

        @Override
        public OSClientV2Builder credentials(String userName, String password) {
            this.factory.credentials(userName, password);
            return this;
        }

        @Override
        public OSClientV2 createAuthenticated() {
            return this.factory.authenticate();
        }

    }

    private static class StandardOSClientV3Builder implements OSClientV3Builder {

        private final V3 factory;

        public StandardOSClientV3Builder(V3 factory) {
            this.factory = factory;
        }

        @Override
        public OSClientV3Builder keyStoneUrl(String keystoneUrl) {
            this.factory.endpoint(keystoneUrl);
            return this;
        }

        @Override
        public OSClientV3Builder credentials(String userId, String password) {
            this.factory.credentials(userId, password);
            return this;
        }

        @Override
        public OSClientV3Builder credentials(String userName, Domain userDomain, String password) {
            this.factory.credentials(userName, password, userDomain.toIdentifier());
            return this;
        }

        @Override
        public OSClientV3Builder project(String projectId) {
            this.factory.scopeToProject(Identifier.byId(projectId));
            return this;
        }

        @Override
        public OSClientV3Builder project(String projectName, Domain projectDomain) {
            this.factory.scopeToProject(Identifier.byName(projectName), projectDomain.toIdentifier());
            return this;
        }

        @Override
        public OSClientV3 createAuthenticated() {
            return this.factory.authenticate();
        }

    }
}
