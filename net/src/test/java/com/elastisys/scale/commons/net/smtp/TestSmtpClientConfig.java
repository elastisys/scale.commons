package com.elastisys.scale.commons.net.smtp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Exercises the {@link SmtpClientConfig} class.
 */
public class TestSmtpClientConfig {

    @Test
    public void createValidServerSettings() {
        // no auth, no ssl
        SmtpClientConfig settings = new SmtpClientConfig("localhost", 25, null, false);
        settings.validate();
        assertThat(settings.getSmtpHost(), is("localhost"));
        assertThat(settings.getSmtpPort(), is(25));
        assertThat(settings.getAuthentication(), is(nullValue()));
        assertThat(settings.isUseSsl(), is(false));

        // no auth, ssl
        settings = new SmtpClientConfig("localhost", 25, null, true);
        settings.validate();
        assertThat(settings.getSmtpHost(), is("localhost"));
        assertThat(settings.getSmtpPort(), is(25));
        assertThat(settings.getAuthentication(), is(nullValue()));
        assertThat(settings.isUseSsl(), is(true));

        // auth, no ssl
        settings = new SmtpClientConfig("localhost", 25, new SmtpClientAuthentication("user", "pass"), false);
        settings.validate();
        assertThat(settings.getSmtpHost(), is("localhost"));
        assertThat(settings.getSmtpPort(), is(25));
        assertThat(settings.getAuthentication(), is(new SmtpClientAuthentication("user", "pass")));
        assertThat(settings.isUseSsl(), is(false));

        // auth, ssl
        settings = new SmtpClientConfig("localhost", 25, new SmtpClientAuthentication("user", "pass"), false);
        settings.validate();
        assertThat(settings.getSmtpHost(), is("localhost"));
        assertThat(settings.getSmtpPort(), is(25));
        assertThat(settings.getAuthentication(), is(new SmtpClientAuthentication("user", "pass")));
        assertThat(settings.isUseSsl(), is(false));

        // three-arg constructor
        settings = new SmtpClientConfig("localhost", 25, new SmtpClientAuthentication("user", "pass"));
        settings.validate();
        assertThat(settings.getSmtpHost(), is("localhost"));
        assertThat(settings.getSmtpPort(), is(25));
        assertThat(settings.getAuthentication(), is(new SmtpClientAuthentication("user", "pass")));
        assertThat(settings.isUseSsl(), is(false));
    }

    @Test
    public void createWithDefaults() {
        Integer smtpPort = null;
        Boolean useSsl = null;
        SmtpClientConfig smtpClientConfig = new SmtpClientConfig("localhost", smtpPort,
                new SmtpClientAuthentication("user", "pass"), useSsl);
        assertThat(smtpClientConfig.getSmtpPort(), is(SmtpClientConfig.DEFAULT_SMTP_PORT));
        assertThat(smtpClientConfig.isUseSsl(), is(false));
        assertThat(smtpClientConfig.getConnectionTimeout(), is(SmtpClientConfig.DEFAULT_CONNECTION_TIMEOUT));
        assertThat(smtpClientConfig.getSocketTimeout(), is(SmtpClientConfig.DEFAULT_SOCKET_TIMEOUT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullSmtpHost() {
        new SmtpClientConfig(null, 25, new SmtpClientAuthentication("user", "pass"), false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithIllegalSmtpPort() {
        new SmtpClientConfig("localhost", -1, new SmtpClientAuthentication("user", "pass"), false);
    }

}
