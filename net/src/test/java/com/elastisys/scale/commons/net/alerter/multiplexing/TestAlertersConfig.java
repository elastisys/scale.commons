package com.elastisys.scale.commons.net.alerter.multiplexing;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.json.types.TimeInterval;
import com.elastisys.scale.commons.net.alerter.http.HttpAlerterConfig;
import com.elastisys.scale.commons.net.alerter.http.HttpAuthConfig;
import com.elastisys.scale.commons.net.alerter.smtp.SmtpAlerterConfig;
import com.elastisys.scale.commons.net.smtp.SmtpClientConfig;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;

/**
 * Exercise {@link AlertersConfig}.
 */
public class TestAlertersConfig {

    /**
     * Make sure arguments are stored correctly.
     */
    @Test
    public void basicSanity() {
        SmtpAlerterConfig emailAlerter1 = smtpAlerterConfig("user1@elastisys.com", "ERROR");
        SmtpAlerterConfig emailAlerter2 = smtpAlerterConfig("user2@elastisys.com", "FATAL");
        HttpAlerterConfig httpAlerter1 = new HttpAlerterConfig(Arrays.asList("http://host1/"), "INFO|WARN", null);
        HttpAlerterConfig httpAlerter2 = new HttpAlerterConfig(Arrays.asList("https://host2/"), "ERROR",
                new HttpAuthConfig(new BasicCredentials("user", "pass"), null));

        TimeInterval duplicateSuppression = new TimeInterval(10L, TimeUnit.MINUTES);
        AlertersConfig config = new AlertersConfig(asList(emailAlerter1, emailAlerter2),
                asList(httpAlerter1, httpAlerter2), duplicateSuppression);
        config.validate();
        assertThat(config.getSmtpAlerters(), is(asList(emailAlerter1, emailAlerter2)));
        assertThat(config.getHttpAlerters(), is(asList(httpAlerter1, httpAlerter2)));
        assertThat(config.getDuplicateSuppression(), is(duplicateSuppression));
    }

    /**
     * A default value is used if no duplicate suppression is specified.
     */
    @Test
    public void withDefaultDuplicateSuppression() {
        SmtpAlerterConfig emailAlerter1 = smtpAlerterConfig("user1@elastisys.com", "ERROR");
        HttpAlerterConfig httpAlerter1 = new HttpAlerterConfig(Arrays.asList("http://host1/"), "INFO|WARN", null);

        AlertersConfig config = new AlertersConfig(asList(emailAlerter1), asList(httpAlerter1), null);
        config.validate();
        assertThat(config.getDuplicateSuppression(), is(AlertersConfig.DEFAULT_DUPLICATE_SUPPRESSION));

    }

    /**
     * <code>null</code> arguments should be allowed and should default to empty
     * list.
     */
    @Test
    public void createWithNulls() {
        AlertersConfig config = new AlertersConfig(null, null);
        config.validate();
        List<HttpAlerterConfig> emptyHttpAlerters = Collections.emptyList();
        List<SmtpAlerterConfig> emptySmtpAlerters = Collections.emptyList();
        assertThat(config.getHttpAlerters(), is(emptyHttpAlerters));
        assertThat(config.getSmtpAlerters(), is(emptySmtpAlerters));
    }

    /**
     * Make sure that {@link AlertersConfig#validate()} calls through to
     * validate configured SMTP alerters.
     */
    @Test(expected = IllegalArgumentException.class)
    public void validationOfSmtpAlerter() {
        SmtpAlerterConfig illegalSmtpAlerter = JsonUtils.toObject(
                JsonUtils.parseJsonString("{\"subject\": \"error\", " + "\"recipients\": null}"),
                SmtpAlerterConfig.class);

        new AlertersConfig(asList(illegalSmtpAlerter), asList()).validate();
    }

    /**
     * Make sure that {@link AlertersConfig#validate()} calls through to
     * validate configured HTTP alerters.
     */
    @Test(expected = IllegalArgumentException.class)
    public void validationOfHttpAlerter() {
        HttpAlerterConfig illegalHttpAlerter = JsonUtils.toObject(
                JsonUtils.parseJsonString("{\"severityFilter\": \"ERROR\", \"destinationUrls\": null}"),
                HttpAlerterConfig.class);

        new AlertersConfig(asList(), asList(illegalHttpAlerter)).validate();
    }

    private SmtpAlerterConfig smtpAlerterConfig(String recipient, String severityFilter) {
        return new SmtpAlerterConfig(Arrays.asList(recipient), "sender@elastisys.com", "subject", severityFilter,
                smtpClientConfig());
    }

    private SmtpClientConfig smtpClientConfig() {
        return new SmtpClientConfig("some.mail.host", 25, null);
    }

}
