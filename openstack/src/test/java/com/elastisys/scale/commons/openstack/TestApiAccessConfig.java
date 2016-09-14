package com.elastisys.scale.commons.openstack;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Exercises the {@link ApiAccessConfig}.
 */
public class TestApiAccessConfig {

    @Test
    public void creation() {
        // explicit floating IP assignment
        AuthConfig auth = new AuthConfig("https://keystone.host:5000/v3/", null,
                new AuthV3Credentials(new Scope("domain_id", null), "user_id", "pass"));
        String region = "RegionOne";
        ApiAccessConfig config = new ApiAccessConfig(auth, region);
        assertThat(config.getAuth(), is(auth));
        assertThat(config.getRegion(), is(region));

        // explicit connection timeouts
        int connectionTimeout = 5000;
        int socketTimeout = 7000;
        config = new ApiAccessConfig(auth, region, connectionTimeout, socketTimeout);
        assertThat(config.getConnectionTimeout(), is(connectionTimeout));
        assertThat(config.getSocketTimeout(), is(socketTimeout));

        // default floating IP assignment (true)
        config = new ApiAccessConfig(auth, region, null, null);
        assertThat(config.getAuth(), is(auth));
        assertThat(config.getRegion(), is(region));
        // default connection timeouts
        assertThat(config.getConnectionTimeout(), is(ApiAccessConfig.DEFAULT_CONNECTION_TIMEOUT));
        assertThat(config.getSocketTimeout(), is(ApiAccessConfig.DEFAULT_SOCKET_TIMEOUT));
    }

    /** Config must specify authentication details. */
    @Test(expected = IllegalArgumentException.class)
    public void missingAuth() {
        AuthConfig auth = null;
        String region = "RegionOne";
        new ApiAccessConfig(auth, region, null, null);
    }

    /** Config must specify region to operate against. */
    @Test(expected = IllegalArgumentException.class)
    public void missingRegion() {
        AuthConfig auth = new AuthConfig("https://keystone.host:5000/v3/", null,
                new AuthV3Credentials(new Scope("domain_id", null), "user_id", "pass"));
        String region = null;
        new ApiAccessConfig(auth, region, null, null);
    }

    /**
     * Connection timeout must be positive.
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalConnectionTimeout() {
        AuthConfig authConfig = new AuthConfig("https://keystone.host:5000/v3/", null,
                new AuthV3Credentials(new Scope("domain_id", null), "user_id", "pass"));
        new ApiAccessConfig(authConfig, "region", 0, 10000).validate();
    }

    /**
     * Socket timeout must be positive.
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalSocketTimeout() {
        AuthConfig authConfig = new AuthConfig("https://keystone.host:5000/v3/", null,
                new AuthV3Credentials(new Scope("domain_id", null), "user_id", "pass"));
        new ApiAccessConfig(authConfig, "region", 10000, -1).validate();
    }

}
