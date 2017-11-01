package com.elastisys.scale.commons.openstack;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Exercises the {@link ApiAccessConfig}.
 */
public class TestApiAccessConfig {

    @Test
    public void basicSanity() {
        // v2 auth
        String region = "RegionOne";
        Integer connectTimeout = 10000;
        Integer socketTimeout = 15000;
        boolean logHttpRequests = true;
        ApiAccessConfig config = new ApiAccessConfig(v2Auth(), region, connectTimeout, socketTimeout, logHttpRequests);
        config.validate();

        assertThat(config.getAuth(), is(v2Auth()));
        assertThat(config.getRegion(), is(region));
        assertThat(config.getConnectionTimeout(), is(connectTimeout));
        assertThat(config.getSocketTimeout(), is(socketTimeout));
        assertThat(config.shouldLogHttpRequests(), is(true));

        // v3 auth
        config = new ApiAccessConfig(v3Auth(), region, connectTimeout, socketTimeout, logHttpRequests);
        config.validate();

        assertThat(config.getAuth(), is(v3Auth()));
        assertThat(config.getRegion(), is(region));
        assertThat(config.getConnectionTimeout(), is(connectTimeout));
        assertThat(config.getSocketTimeout(), is(socketTimeout));
        assertThat(config.shouldLogHttpRequests(), is(true));
    }

    /** Verify that default values are provided for optional parameters. */
    @Test
    public void createWithDefaults() {
        String region = "RegionOne";
        ApiAccessConfig config = new ApiAccessConfig(v2Auth(), region);
        config.validate();

        // verify defaults
        assertThat(config.getConnectionTimeout(), is(ApiAccessConfig.DEFAULT_CONNECTION_TIMEOUT));
        assertThat(config.getSocketTimeout(), is(ApiAccessConfig.DEFAULT_SOCKET_TIMEOUT));
        assertThat(config.shouldLogHttpRequests(), is(false));
    }

    /** Config must specify authentication details. */
    @Test
    public void missingAuth() {
        AuthConfig auth = null;
        String region = "RegionOne";
        try {
            new ApiAccessConfig(auth, region, null, null);
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("no auth"));
        }
    }

    /** Config must specify region to operate against. */
    @Test
    public void missingRegion() {
        AuthConfig auth = v3Auth();
        String region = null;
        try {
            new ApiAccessConfig(auth, region, null, null);
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("missing region"));
        }
    }

    /**
     * Connection timeout must be positive.
     */
    @Test
    public void illegalConnectionTimeout() {
        try {
            new ApiAccessConfig(v2Auth(), "region", 0, 10000).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("connectionTimeout"));
        }
    }

    /**
     * Socket timeout must be positive.
     */
    @Test
    public void illegalSocketTimeout() {
        try {
            new ApiAccessConfig(v2Auth(), "region", 10000, 0).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("socketTimeout"));
        }
    }

    private static final AuthConfig v2Auth() {
        return new AuthConfig("https//keystone.host:5000/v3", v2Credentials(), null);
    }

    private static final AuthConfig v3Auth() {
        return new AuthConfig("https//keystone.host:5000/v3", null, v3Credentials());
    }

    private static AuthV2Credentials v2Credentials() {
        return new AuthV2Credentials("tenant", "user", "password");
    }

    private static AuthV3Credentials v3Credentials() {
        return new AuthV3Credentials("userId", null, null, null, "password", "projectId", null, null, null);
    }

}
