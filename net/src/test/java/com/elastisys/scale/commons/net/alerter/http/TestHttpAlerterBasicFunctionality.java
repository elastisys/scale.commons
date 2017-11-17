package com.elastisys.scale.commons.net.alerter.http;

import static com.elastisys.scale.commons.util.time.UtcTime.now;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.AlertBuilder;
import com.elastisys.scale.commons.net.alerter.AlertSeverity;
import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.server.ServletDefinition;
import com.elastisys.scale.commons.server.ServletServerBuilder;
import com.elastisys.scale.commons.server.SslKeyStoreType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Verifies the behavior of the {@link HttpAlerter} when no authentication has
 * been configured.
 */
public class TestHttpAlerterBasicFunctionality {
    // Server keystore
    private static final String SERVER_PKCS12_KEYSTORE = "src/test/resources/security/server/server_keystore.p12";
    private static final String SERVER_PKCS12_KEYSTORE_PASSWORD = "serverpassword";

    /** The port where a HTTPS server is set up. */
    private static Integer httpsPort;
    /** Dummy HTTPS server. */
    private static Server server;
    /** The web hook that receives all alerts. */
    private static WebhookServlet webhook;

    @BeforeClass
    public static void beforeTests() throws Exception {
        // find a free port for test server
        List<Integer> freePorts = HostUtils.findFreePorts(1);
        httpsPort = freePorts.get(0);
        // server instances are created by each individual test method
        server = createHttpsServer(httpsPort);
        server.start();
    }

    @Before
    public void beforeTestMethod() {
        webhook.clear();
    }

    /**
     * Creates a HTTPS server to be used during the test methods. The server
     * requires no client authentication whatsoever.
     *
     * @param httpsPort
     * @return
     */
    private static Server createHttpsServer(int httpsPort) {
        webhook = new WebhookServlet(200);
        ServletDefinition servlet = new ServletDefinition.Builder().servlet(webhook).servletPath("/")
                .requireBasicAuth(false).requireRole("USER").build();
        return ServletServerBuilder.create().httpsPort(httpsPort).sslKeyStoreType(SslKeyStoreType.PKCS12)
                .sslKeyStorePath(SERVER_PKCS12_KEYSTORE).sslKeyStorePassword(SERVER_PKCS12_KEYSTORE_PASSWORD)
                .sslRequireClientCert(false).addServlet(servlet).build();
    }

    /**
     * Tests basic delivery of alerts.
     */
    @Test
    public void testBasicDelivery() {
        HttpAlerter alerter = new HttpAlerter(config(webhookUrl(), null, null), null);

        // alerts with different severity
        Alert alert1 = AlertBuilder.create().topic("topic1").severity(AlertSeverity.DEBUG)
                .timestamp(new DateTime(1, DateTimeZone.UTC)).message("debug message").build();
        Alert alert2 = AlertBuilder.create().topic("topic2").severity(AlertSeverity.INFO)
                .timestamp(new DateTime(2, DateTimeZone.UTC)).message("info message").build();
        Alert alert3 = AlertBuilder.create().topic("topic3").severity(AlertSeverity.NOTICE)
                .timestamp(new DateTime(3, DateTimeZone.UTC)).message("notice message").build();
        Alert alert4 = AlertBuilder.create().topic("topic4").severity(AlertSeverity.WARN)
                .timestamp(new DateTime(4, DateTimeZone.UTC)).message("warn message").build();
        Alert alert5 = AlertBuilder.create().topic("topic5").severity(AlertSeverity.ERROR)
                .timestamp(new DateTime(5, DateTimeZone.UTC)).message("error message").build();
        // alert with metadata
        Map<String, JsonElement> metadata = new HashMap<>();
        metadata.put("key1", JsonUtils.toJson("value1"));
        Alert alert6 = AlertBuilder.create().topic("topic6").severity(AlertSeverity.FATAL)
                .timestamp(new DateTime(6, DateTimeZone.UTC)).message("fatal message")
                .addMetadata("key1", JsonUtils.toJson("value1")).build();

        alerter.handleAlert(alert1);
        alerter.handleAlert(alert2);
        alerter.handleAlert(alert3);
        alerter.handleAlert(alert4);
        alerter.handleAlert(alert5);
        alerter.handleAlert(alert6);

        assertThat(webhook.getReceivedMessages().size(), is(6));
        assertThat(webhook.getReceivedMessages().get(0), is(alert1));
        assertThat(webhook.getReceivedMessages().get(1), is(alert2));
        assertThat(webhook.getReceivedMessages().get(2), is(alert3));
        assertThat(webhook.getReceivedMessages().get(3), is(alert4));
        assertThat(webhook.getReceivedMessages().get(4), is(alert5));
        assertThat(webhook.getReceivedMessages().get(5), is(alert6));
    }

    /**
     * Tests delivery of alerts with standard metadata tags (to be added to
     * every outgoing alert).
     */
    @Test
    public void testDeliveryWithStandadMetadataTags() {
        // create alerter with standard metadata tags
        Map<String, JsonElement> standardMetadata = new HashMap<>();
        standardMetadata.put("key1", JsonUtils.toJson("value1"));
        JsonObject value2 = JsonUtils.parseJsonString("{'x': true, 'y': 'z'}").getAsJsonObject();
        standardMetadata.put("key2", value2);
        HttpAlerter alerter = new HttpAlerter(config(webhookUrl(), null, null), standardMetadata);

        // send alert without any extra metadata tags
        Alert alert = new Alert("topic1", AlertSeverity.INFO, new DateTime(2, DateTimeZone.UTC), "info message", null);
        // verify that standard metadata tags were added
        alerter.handleAlert(alert);
        Alert expectedAlert = alert.withMetadata("key1", JsonUtils.toJson("value1")).withMetadata("key2", value2);
        assertThat(webhook.getReceivedMessages().size(), is(1));
        assertThat(webhook.getReceivedMessages().get(0), is(expectedAlert));

        // send alert with some extra metadata tags
        Map<String, JsonElement> extraMetadata = new HashMap<>();
        extraMetadata.put("key3", JsonUtils.toJson("value3"));
        alert = new Alert("topic1", AlertSeverity.INFO, new DateTime(2, DateTimeZone.UTC), "info message", null,
                extraMetadata);
        // verify that standard metadata tags were added
        alerter.handleAlert(alert);
        expectedAlert = alert.withMetadata("key1", JsonUtils.toJson("value1")).withMetadata("key2", value2);
        assertThat(webhook.getReceivedMessages().size(), is(2));
        assertThat(webhook.getReceivedMessages().get(1), is(expectedAlert));
    }

    /**
     * Only {@link Alert}s that match the severity filter should be sent.
     */
    @Test
    public void testDeliverySuppression() {
        // use a non-default severity filter
        String severityFilter = "WARN|ERROR";
        HttpAlerter alerter = new HttpAlerter(config(webhookUrl(), severityFilter, null), null);

        // send alerts with different severity
        Alert alert1 = new Alert("topic", AlertSeverity.DEBUG, now(), "msg", null);
        Alert alert2 = new Alert("topic", AlertSeverity.INFO, now(), "msg", null);
        Alert alert3 = new Alert("topic", AlertSeverity.NOTICE, now(), "msg", null);
        Alert alert4 = new Alert("topic", AlertSeverity.WARN, now(), "msg", null);
        Alert alert5 = new Alert("topic", AlertSeverity.ERROR, now(), "msg", null);
        Alert alert6 = new Alert("topic", AlertSeverity.FATAL, now(), "msg", null);
        alerter.handleAlert(alert1);
        alerter.handleAlert(alert2);
        alerter.handleAlert(alert3);
        alerter.handleAlert(alert4);
        alerter.handleAlert(alert5);
        alerter.handleAlert(alert6);

        // make sure that all but the WARN and ERROR alerts were suppressed
        assertThat(webhook.getReceivedMessages().size(), is(2));
        assertThat(webhook.getReceivedMessages().get(0), is(alert4));
        assertThat(webhook.getReceivedMessages().get(1), is(alert5));
    }

    /**
     * Verify sending to multiple destinations.
     */
    @Test
    public void multipleDestinations() {
        // use multiple destinations (although they are actually the same
        // endpoint)
        HttpAlerterConfig config = new HttpAlerterConfig(Arrays.asList(webhookUrl(), webhookUrl()), null, null, 100,
                100);
        HttpAlerter alerter = new HttpAlerter(config, null);

        Alert alert = new Alert("topic", AlertSeverity.INFO, now(), "msg", null);
        alerter.handleAlert(alert);

        // make sure that the endpoint got the alert twice
        assertThat(webhook.getReceivedMessages().size(), is(2));
        assertThat(webhook.getReceivedMessages().get(0), is(alert));
        assertThat(webhook.getReceivedMessages().get(1), is(alert));
    }

    /**
     * Verify that one bad destination receiver doesn't stop the delivery to
     * functioning destinations.
     */
    @Test
    public void multipleDestinationsAndOneThatCannotBeReached() {
        // use multiple destinations, one of which is not reachable
        HttpAlerterConfig config = new HttpAlerterConfig(Arrays.asList(webhookUrl(), "https://non.existing.host:443/"),
                null, null, 100, 100);
        HttpAlerter alerter = new HttpAlerter(config, null);

        Alert alert = new Alert("topic", AlertSeverity.INFO, now(), "msg", null);
        alerter.handleAlert(alert);

        // make sure that the functioning endpoint got the alert
        assertThat(webhook.getReceivedMessages().size(), is(1));
        assertThat(webhook.getReceivedMessages().get(0), is(alert));
    }

    private HttpAlerterConfig config(String url, String severityFilter, HttpAuthConfig authConfig) {
        return new HttpAlerterConfig(Arrays.asList(url), severityFilter, authConfig, 1000, 1000);
    }

    private String webhookUrl() {
        return String.format("https://localhost:%d/", httpsPort);
    }
}
