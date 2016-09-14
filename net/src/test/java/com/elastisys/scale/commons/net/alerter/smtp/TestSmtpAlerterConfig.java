package com.elastisys.scale.commons.net.alerter.smtp;

import static com.elastisys.scale.commons.net.alerter.smtp.SmtpAlerterConfig.DEFAULT_SEVERITY_FILTER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.elastisys.scale.commons.net.smtp.SmtpClientConfig;

/**
 * Exercises the {@link SmtpAlerterConfig} class.
 */
public class TestSmtpAlerterConfig {

    @Test
    public void createValidConfig() {
        // with default severity filter
        SmtpAlerterConfig settings = new SmtpAlerterConfig(Arrays.asList("recipient@elastisys.com"),
                "sender@elastisys.com", "subject", null, smtpClientConfig());
        assertThat(settings.getRecipients(), is(Arrays.asList("recipient@elastisys.com")));
        assertThat(settings.getSender(), is("sender@elastisys.com"));
        assertThat(settings.getSubject(), is("subject"));
        assertThat(settings.getSeverityFilter().getFilterExpression(), is(DEFAULT_SEVERITY_FILTER));
        assertThat(settings.getSmtpClientConfig(), is(smtpClientConfig()));
        settings.validate();

        // specify severity filter
        settings = new SmtpAlerterConfig(Arrays.asList("recipient@elastisys.com"), "sender@elastisys.com", "subject",
                "INFO|WARN|ERROR", smtpClientConfig());
        assertThat(settings.getSeverityFilter().getFilterExpression(), is("INFO|WARN|ERROR"));
        settings.validate();

        // no recipients
        List<String> noRecipients = Arrays.asList();
        settings = new SmtpAlerterConfig(noRecipients, "sender@elastisys.com", "subject", null, smtpClientConfig());
        assertThat(settings.getRecipients(), is(noRecipients));
        settings.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullRecipientsList() {
        new SmtpAlerterConfig(null, "sender@elastisys.com", "subject", null, smtpClientConfig());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullRecipient() {
        new SmtpAlerterConfig(Arrays.asList("recipient@elastisys.com", null), "sender@elastisys.com", "subject", null,
                smtpClientConfig());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithIllegalRecipientAddress() {
        new SmtpAlerterConfig(Arrays.asList("recipient.elastisys.com"), "sender@elastisys.com", "subject", null,
                smtpClientConfig());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullSubject() {
        new SmtpAlerterConfig(Arrays.asList("recipient@elastisys.com"), "sender@elastisys.com", null, ".*",
                smtpClientConfig());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithIllegalSenderAddress() {
        new SmtpAlerterConfig(Arrays.asList("recipient@elastisys.com"), "sender.elastisys.com", "subject", null,
                smtpClientConfig());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithIllegalSeverityFilter() {
        new SmtpAlerterConfig(Arrays.asList("recipient@elastisys.com"), "sender@elastisys.com", "subject", "**",
                smtpClientConfig());
    }

    private SmtpClientConfig smtpClientConfig() {
        return new SmtpClientConfig("some.mail.host", 25, null);
    }
}
