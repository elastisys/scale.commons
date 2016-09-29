package com.elastisys.scale.commons.cli;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.elastisys.scale.commons.cli.server.BaseServerCliOptions;

/**
 * Exercises the logic of the {@link BaseServerCliOptions}.
 *
 */
public class TestBaseServerCliOptions {

    /**
     * At a minimum, a http port must be given.
     */
    @Test
    public void minimal() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpPort = 8182;

        options.validate();

        assertThat(options.httpPort, is(8182));
        assertThat(options.httpsPort, is(nullValue()));
        assertThat(options.sslKeyStore, is(nullValue()));
        assertThat(options.sslKeyStorePassword, is(nullValue()));
        assertThat(options.requireClientCert, is(false));
        assertThat(options.sslTrustStore, is(nullValue()));
        assertThat(options.sslTrustStorePassword, is(nullValue()));
        assertThat(options.requireBasicAuth, is(false));
        assertThat(options.realmFile, is(nullValue()));
        assertThat(options.requireRole, is(nullValue()));
        assertThat(options.help, is(false));
        assertThat(options.isHelpFlagSet(), is(false));
        assertThat(options.version, is(false));
        assertThat(options.isVersionFlagSet(), is(false));
    }

    /**
     * Set up a minimal HTTPS server.
     */
    @Test
    public void httpsServer() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpsPort = 8443;
        options.sslKeyStore = "/some/keystore.pkcs12";
        options.sslKeyStorePassword = "secret";
        options.validate();
    }

    /**
     * Set up a HTTP server with basic auth.
     */
    @Test
    public void httpWithBasicAuth() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpPort = 8080;
        options.requireBasicAuth = true;
        options.realmFile = "/some/realm.file";
        options.requireRole = "ADMIN";
        options.validate();
    }

    /**
     * At least one of {@code --http-port} and {@code --https-port} must be
     * given.
     */
    @Test
    public void requireHttpOrHttpsPort() {
        try {
            new ServerCliOptions().validate();
            fail("should not validate");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("neither --http-port nor --https-port specified"));
        }
    }

    /**
     * When https is specified, {@code --ssl-keystore} is required.
     */
    @Test
    public void whenHttpsRequireSslKeyStore() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpsPort = 9443;

        try {
            options.validate();
            fail("should not validate");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("--ssl-keystore is required when a https port is specified"));
        }
    }

    /**
     * When {@code --ssl-keystore} is specified, {@code --ssl-keystore-password}
     * is required.
     */
    @Test
    public void whenSslKeyStoreRequirePassword() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpsPort = 9443;
        options.sslKeyStore = "/some/keystore.pkcs12";

        try {
            options.validate();
            fail("should not validate");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("no --ssl-keystore-password specified"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalHttpPort() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpPort = 0;
        options.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalHttpsPort() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpsPort = 0;
        options.validate();
    }

    /**
     * It should be possible to combine both http and https.
     */
    @Test
    public void httpAndHttpsAreCombineable() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpPort = 8080;
        options.httpsPort = 8443;
        options.sslKeyStore = "/some/keystore.pkcs12";
        options.sslKeyStorePassword = "secret";
        options.validate();
    }

    /**
     * {@code --realm-file} must be given when {@code --require-basicauth} is
     * set.
     */
    @Test
    public void requireBasicAuthRequiresRealmFile() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpPort = 8080;
        options.requireBasicAuth = true;

        try {
            options.validate();
            fail("should not validate");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("--realm-file is required when --require-basicauth is specified"));
        }
    }

    /**
     * {@code --require-role} must be given when {@code --require-basicauth} is
     * set.
     */
    @Test
    public void requireBasicAuthRequiresRequireRole() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpPort = 8080;
        options.requireBasicAuth = true;
        options.realmFile = "/some/realm.file";

        try {
            options.validate();
            fail("should not validate");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("--require-role is required when --require-basicauth is specified"));
        }
    }

    /**
     * {@code --ssl-truststore} must be given when {@code --require-cert} is
     * set.
     */
    @Test
    public void requireCertRequiresSslTruststore() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpsPort = 8443;
        options.sslKeyStore = "/some/keystore.pkcs12";
        options.sslKeyStorePassword = "secret";
        options.requireClientCert = true;

        try {
            options.validate();
            fail("should not validate");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("--ssl-truststore is required when --require-cert is specified"));
        }
    }

    /**
     * {@code --ssl-truststore-password} must be given when
     * {@code --require-cert} is set.
     */
    @Test
    public void requireCertRequiresSslTruststorePassword() {
        BaseServerCliOptions options = new ServerCliOptions();
        options.httpsPort = 8443;
        options.sslKeyStore = "/some/keystore.pkcs12";
        options.sslKeyStorePassword = "secret";
        options.requireClientCert = true;
        options.sslTrustStore = "/some/truststore.jks";

        try {
            options.validate();
            fail("should not validate");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("--ssl-truststore-password is required when --require-cert is specified"));
        }
    }

    /**
     * A dummy extension of the {@link BaseServerCliOptions} class.
     *
     */
    public static class ServerCliOptions extends BaseServerCliOptions {

        @Override
        public String getVersion() {
            return "1.0.0";
        }

    }
}
