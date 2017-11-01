package com.elastisys.scale.commons.openstack;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Exercises the {@link AuthConfig} class.
 */
public class TestAuthConfig {

    @Test
    public void creation() {
        // v2 credential creation.
        AuthConfig v2Auth = new AuthConfig("https://keystone.host:5000/v2.0", v2Credentials(), null);
        v2Auth.validate();
        assertThat(v2Auth.isV2Auth(), is(true));
        assertThat(v2Auth.isV3Auth(), is(false));
        assertThat(v2Auth.getKeystoneUrl(), is("https://keystone.host:5000/v2.0"));
        assertThat(v2Auth.getV2Credentials(), is(v2Credentials()));
        assertThat(v2Auth.getV3Credentials(), is(nullValue()));

        // v3 credential creation.
        AuthConfig v3Auth = new AuthConfig("https://keystone.host:5000/v3/", null, v3Credentials());
        v3Auth.validate();
        assertThat(v3Auth.isV2Auth(), is(false));
        assertThat(v3Auth.isV3Auth(), is(true));
        assertThat(v3Auth.getKeystoneUrl(), is("https://keystone.host:5000/v3/"));
        assertThat(v3Auth.getV2Credentials(), is(nullValue()));
        assertThat(v3Auth.getV3Credentials(), is(v3Credentials()));
    }

    @Test
    public void missingKeystoneUrl() {
        try {
            new AuthConfig(null, null, v3Credentials()).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("no keystoneUrl given"));
        }
    }

    @Test
    public void missingBothV2AndV3Credentials() {
        try {
            new AuthConfig("https://keystone.host:5000/v2.0", null, null).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("neither v2Credentials nor v3Credentials were given"));
        }
    }

    /**
     * Must specify either v2 or v3 credentials, not both.
     */
    @Test
    public void specifyingBothV2AndV3Credentials() {
        try {
            new AuthConfig("https://keystone.host:5000/v2.0", v2Credentials(), v3Credentials()).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("either v2Credentials or v3Credentials must be given, noto both"));
        }
    }

    private static AuthV2Credentials v2Credentials() {
        return new AuthV2Credentials("tenant", "user", "password");
    }

    private static AuthV3Credentials v3Credentials() {
        return new AuthV3Credentials("userId", null, null, null, "password", "projectId", null, null, null);
    }
}
