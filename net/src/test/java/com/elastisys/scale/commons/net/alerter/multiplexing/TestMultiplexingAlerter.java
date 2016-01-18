package com.elastisys.scale.commons.net.alerter.multiplexing;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.json.types.TimeInterval;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.AlertBuilder;
import com.elastisys.scale.commons.net.alerter.AlertSeverity;
import com.elastisys.scale.commons.net.alerter.Alerter;
import com.elastisys.scale.commons.net.alerter.filtering.FilteringAlerter;
import com.elastisys.scale.commons.net.alerter.http.HttpAlerter;
import com.elastisys.scale.commons.net.alerter.http.HttpAlerterConfig;
import com.elastisys.scale.commons.net.alerter.http.HttpAuthConfig;
import com.elastisys.scale.commons.net.alerter.smtp.SmtpAlerter;
import com.elastisys.scale.commons.net.alerter.smtp.SmtpAlerterConfig;
import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.net.smtp.SmtpClientAuthentication;
import com.elastisys.scale.commons.net.smtp.SmtpClientConfig;
import com.elastisys.scale.commons.net.smtp.SmtpTestServerUtil;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

/**
 * Exercise the {@link MultiplexingAlerter} class.
 */
public class TestMultiplexingAlerter {

	/** Trusted user on SSL server. */
	private static final String USERNAME = "user";
	/** Trusted user's password on SSL server. */
	private static final String PASSWORD = "password";

	/** Port where fake SMTP server is listening for SSL */
	private static int SMTP_SSL_PORT;

	static {
		List<Integer> freePorts = HostUtils.findFreePorts(2);
		SMTP_SSL_PORT = freePorts.get(1);
	}

	/** Fake email SMTP server with SSL. */
	private GreenMail sslMailServer;

	private EventBus eventBus = new EventBus();

	/** Object under test. */
	private MultiplexingAlerter multiplexingAlerter;

	@Before
	public void beforeTestMethod() {
		startSmtpServer();

		// freeze current time in tests
		FrozenTime.setFixed(UtcTime.parse("2015-01-01T12:00:00Z"));

		this.multiplexingAlerter = new MultiplexingAlerter(
				FilteringAlerter.TOPIC_IDENTITY_FUNCTION);
		this.eventBus.register(this.multiplexingAlerter);
	}

	@After
	public void onTearDown() {
		stopSmtpServer();
	}

	private void stopSmtpServer() {
		if (this.sslMailServer != null) {
			this.sslMailServer.stop();
		}
	}

	/**
	 * Start the mail servers used in the test.
	 */
	private void startSmtpServer() {
		this.sslMailServer = SmtpTestServerUtil
				.startSslStmpServer(SMTP_SSL_PORT, USERNAME, PASSWORD);
	}

	/**
	 * <code>null</code> values are interpreted as "none".
	 */
	@Test
	public void registerNullAlertConfig() {
		this.multiplexingAlerter.registerAlerters(null, null);
		assertThat(this.multiplexingAlerter.alerters(), is(alerters()));
	}

	@Test
	public void registerEmptyAlerterConfig() {
		this.multiplexingAlerter.registerAlerters(alertConfig(null, null, null),
				null);
		assertThat(this.multiplexingAlerter.alerters(), is(alerters()));
	}

	@Test
	public void registerHttpAlerterConfigWithoutStandardTags() {
		List<SmtpAlerterConfig> smtpAlerters = null;
		HttpAlerterConfig http1Config = httpConfig("http://hook", ".*");
		List<HttpAlerterConfig> httpAlerters = Arrays.asList(http1Config);
		Map<String, JsonElement> standardTags = ImmutableMap.of();

		AlertersConfig alertConfig = alertConfig(smtpAlerters, httpAlerters,
				null);

		this.multiplexingAlerter.registerAlerters(alertConfig, standardTags);
		assertThat(this.multiplexingAlerter.alerters(),
				is(alerters(filtered(http(http1Config, standardTags),
						alertConfig.getDuplicateSuppression()))));
	}

	@Test
	public void registerHttpAlerterConfigWithStandardTags() {
		List<SmtpAlerterConfig> smtpAlerters = null;
		HttpAlerterConfig http1Config = httpConfig("http://hook", ".*");
		List<HttpAlerterConfig> httpAlerters = Arrays.asList(http1Config);
		AlertersConfig alertConfig = alertConfig(smtpAlerters, httpAlerters,
				null);

		this.multiplexingAlerter.registerAlerters(alertConfig, standardTags());
		assertThat(this.multiplexingAlerter.alerters(),
				is(alerters(filtered(http(http1Config, standardTags()),
						alertConfig.getDuplicateSuppression()))));
	}

	@Test
	public void registerSmtpAlerterConfigWithoutStandardTags() {
		SmtpAlerterConfig smtp1Conf = smtpConfig("john@doe.com", ".*");
		List<SmtpAlerterConfig> smtpAlerters = Arrays.asList(smtp1Conf);
		List<HttpAlerterConfig> httpAlerters = null;
		Map<String, JsonElement> standardTags = ImmutableMap.of();
		AlertersConfig alertConfig = alertConfig(smtpAlerters, httpAlerters,
				null);

		this.multiplexingAlerter.registerAlerters(alertConfig, standardTags);
		assertThat(this.multiplexingAlerter.alerters(),
				is(alerters(filtered(smtp(smtp1Conf, standardTags),
						alertConfig.getDuplicateSuppression()))));
	}

	@Test
	public void registerSmtpAlerterConfigWithStandardTags() {
		SmtpAlerterConfig smtp1Conf = smtpConfig("john@doe.com", ".*");
		List<SmtpAlerterConfig> smtpAlerters = Arrays.asList(smtp1Conf);
		List<HttpAlerterConfig> httpAlerters = null;
		AlertersConfig alertConfig = alertConfig(smtpAlerters, httpAlerters,
				null);

		this.multiplexingAlerter.registerAlerters(alertConfig, standardTags());
		assertThat(this.multiplexingAlerter.alerters(),
				is(alerters(filtered(smtp(smtp1Conf, standardTags()),
						alertConfig.getDuplicateSuppression()))));
	}

	@Test
	public void registerHttpAndSmtpAlerterConfig() {
		SmtpAlerterConfig smtp1Conf = smtpConfig("john@doe.com", ".*");
		List<SmtpAlerterConfig> smtpAlerters = Arrays.asList(smtp1Conf);
		HttpAlerterConfig http1Conf = httpConfig("http://hook", ".*");
		List<HttpAlerterConfig> httpAlerters = Arrays.asList(http1Conf);
		AlertersConfig alertConfig = alertConfig(smtpAlerters, httpAlerters,
				null);

		this.multiplexingAlerter.registerAlerters(alertConfig, standardTags());
		assertThat(this.multiplexingAlerter.alerters(),
				is(alerters(
						filtered(smtp(smtp1Conf, standardTags()),
								alertConfig.getDuplicateSuppression()),
						filtered(http(http1Conf, standardTags()),
								alertConfig.getDuplicateSuppression()))));
	}

	/**
	 * Should be possible to call register several times to add more
	 * {@link Alerter}s.
	 */
	@Test
	public void registerAdditionalAlerters() {
		// register an additional smtp alerter
		SmtpAlerterConfig smtp1Conf = smtpConfig("john@doe.com", ".*");
		List<SmtpAlerterConfig> smtpAlerters = Arrays.asList(smtp1Conf);
		AlertersConfig alert1Config = alertConfig(smtpAlerters, null,
				new TimeInterval(10L, TimeUnit.MINUTES));
		this.multiplexingAlerter.registerAlerters(alert1Config, standardTags());
		assertThat(this.multiplexingAlerter.alerters(),
				is(alerters(filtered(smtp(smtp1Conf, standardTags()),
						alert1Config.getDuplicateSuppression()))));

		// register an additional http alerter
		HttpAlerterConfig http1Conf = httpConfig("http://hook", ".*");
		List<HttpAlerterConfig> httpAlerters = Arrays.asList(http1Conf);
		AlertersConfig alert2Config = alertConfig(null, httpAlerters,
				new TimeInterval(18L, TimeUnit.MINUTES));
		this.multiplexingAlerter.registerAlerters(alert2Config, standardTags());
		assertThat(this.multiplexingAlerter.alerters(),
				is(alerters(
						filtered(smtp(smtp1Conf, standardTags()),
								alert1Config.getDuplicateSuppression()),
						filtered(http(http1Conf, standardTags()),
								alert2Config.getDuplicateSuppression()))));
	}

	@Test
	public void unregister() {
		// register
		SmtpAlerterConfig smtp1Conf = smtpConfig("john@doe.com", ".*");
		List<SmtpAlerterConfig> smtpAlerters = Arrays.asList(smtp1Conf);
		HttpAlerterConfig http1Conf = httpConfig("http://hook", ".*");
		List<HttpAlerterConfig> httpAlerters = Arrays.asList(http1Conf);
		AlertersConfig alertConfig = alertConfig(smtpAlerters, httpAlerters,
				null);
		this.multiplexingAlerter.registerAlerters(alertConfig, standardTags());
		assertThat(this.multiplexingAlerter.alerters(),
				is(alerters(
						filtered(smtp(smtp1Conf, standardTags()),
								alertConfig.getDuplicateSuppression()),
						filtered(http(http1Conf, standardTags()),
								alertConfig.getDuplicateSuppression()))));

		// unregister
		this.multiplexingAlerter.unregisterAlerters();
		assertThat(this.multiplexingAlerter.alerters(), is(alerters()));
	}

	/**
	 * Make sure {@link Alert}s are dispatched to registered {@link Alerter}s.
	 */
	@Test
	public void dispatch() throws Exception {
		assertThat(this.sslMailServer.getReceivedMessages().length, is(0));
		// set up a http server
		RequestLoggingHttpServer webServer = startHttpServer();

		try {
			// register alerters
			SmtpAlerterConfig smtpAlerter = smtpConfig("recipient@company.com",
					".*");
			HttpAlerterConfig httpAlerter = httpConfig(
					"http://localhost:" + webServer.getHttpPort(), ".*");
			AlertersConfig alertConfig = alertConfig(asList(smtpAlerter),
					asList(httpAlerter), null);
			this.multiplexingAlerter.registerAlerters(alertConfig,
					standardTags());

			assertThat(webServer.getPostedMessages().size(), is(0));
			assertThat(this.sslMailServer.getReceivedMessages().length, is(0));

			// dispatch alert
			Alert alert = AlertBuilder.create().topic("topic")
					.severity(AlertSeverity.ERROR).message("error!").build();
			String expectedMessage = JsonUtils.toPrettyString(
					JsonUtils.toJson(alert.withMetadata(standardTags())));

			this.multiplexingAlerter.handleAlert(alert);
			// verify that alert (with the set standardTags) was dispatched to
			// http and smtp alerters
			assertThat(webServer.getPostedMessages().size(), is(1));
			assertThat(webServer.getPostedMessages().get(0),
					is(expectedMessage));
			MimeMessage[] receivedMessages = this.sslMailServer
					.getReceivedMessages();
			assertThat(receivedMessages.length, is(1));
			SmtpTestServerUtil.assertAlertMail(receivedMessages[0],
					alert.withMetadata(standardTags()));
		} finally {
			webServer.stop();
		}
	}

	/**
	 * Make sure duplicate {@link Alert}s are not dispatched to {@link Alerter}
	 * s.
	 */
	@Test
	public void honorDuplicateSuppression() throws Exception {
		assertThat(this.sslMailServer.getReceivedMessages().length, is(0));
		// set up a http server
		RequestLoggingHttpServer webServer = startHttpServer();

		TimeInterval duplicateSuppression = new TimeInterval(3L,
				TimeUnit.MINUTES);
		try {
			// register alerters
			SmtpAlerterConfig smtpAlerter = smtpConfig("recipient@company.com",
					".*");
			HttpAlerterConfig httpAlerter = httpConfig(
					"http://localhost:" + webServer.getHttpPort(), ".*");
			AlertersConfig alertConfig = alertConfig(asList(smtpAlerter),
					asList(httpAlerter), duplicateSuppression);
			this.multiplexingAlerter.registerAlerters(alertConfig,
					standardTags());

			assertThat(webServer.getPostedMessages().size(), is(0));
			assertThat(this.sslMailServer.getReceivedMessages().length, is(0));

			// dispatch alert
			Alert alert1 = AlertBuilder.create().topic("topic1")
					.severity(AlertSeverity.ERROR).message("error").build();
			Alert alert2 = AlertBuilder.create().topic("topic2")
					.severity(AlertSeverity.INFO).message("info").build();

			this.multiplexingAlerter.handleAlert(alert1);
			this.multiplexingAlerter.handleAlert(alert2);
			// verify that alert was dispatched to http and smtp alerters
			assertThat(webServer.getPostedMessages().size(), is(2));
			assertThat(this.sslMailServer.getReceivedMessages().length, is(2));
			assertAlertMail(this.sslMailServer.getReceivedMessages()[0],
					"topic1");
			assertAlertMail(this.sslMailServer.getReceivedMessages()[1],
					"topic2");

			FrozenTime.tick(60);
			// dispatch duplicate alerts, should be filtered out
			this.multiplexingAlerter.handleAlert(alert1);
			this.multiplexingAlerter.handleAlert(alert2);
			assertThat(webServer.getPostedMessages().size(), is(2));
			assertThat(this.sslMailServer.getReceivedMessages().length, is(2));

			FrozenTime.tick(60);
			// dispatch duplicate alerts again, should be filtered out
			this.multiplexingAlerter.handleAlert(alert1);
			this.multiplexingAlerter.handleAlert(alert2);
			assertThat(webServer.getPostedMessages().size(), is(2));
			assertThat(this.sslMailServer.getReceivedMessages().length, is(2));

			FrozenTime.tick(61);
			// duplicate suppression has passed, alert should no longer be
			// filtered
			this.multiplexingAlerter.handleAlert(alert1);
			this.multiplexingAlerter.handleAlert(alert2);
			assertThat(webServer.getPostedMessages().size(), is(4));
			assertThat(this.sslMailServer.getReceivedMessages().length, is(4));
			assertAlertMail(this.sslMailServer.getReceivedMessages()[2],
					"topic1");
			assertAlertMail(this.sslMailServer.getReceivedMessages()[3],
					"topic2");
		} finally {
			webServer.stop();
		}
	}

	private RequestLoggingHttpServer startHttpServer() throws Exception {
		int httpPort = HostUtils.findFreePorts(1).get(0);
		RequestLoggingHttpServer webServer = new RequestLoggingHttpServer("/",
				httpPort);
		webServer.start();
		return webServer;
	}

	private Map<String, JsonElement> standardTags() {
		return ImmutableMap.of("ip", JsonUtils.toJson("1.2.3.4"), //
				"poolName", JsonUtils.toJson("cloudpool"));

	}

	private List<Alerter> alerters(Alerter... alerters) {
		if (alerters == null) {
			return ImmutableList.of();
		}
		return ImmutableList.copyOf(alerters);
	}

	private AlertersConfig alertConfig(List<SmtpAlerterConfig> emailAlerters,
			List<HttpAlerterConfig> httpAlerters,
			TimeInterval duplicateSuppression) {
		return new AlertersConfig(emailAlerters, httpAlerters,
				duplicateSuppression);
	}

	private Alerter filtered(Alerter alerter,
			TimeInterval duplicateSuppression) {
		return new FilteringAlerter(alerter, duplicateSuppression.getTime(),
				duplicateSuppression.getUnit());
	}

	private Alerter http(HttpAlerterConfig httpAlerterConfig,
			Map<String, JsonElement> standardTags) {
		return new HttpAlerter(httpAlerterConfig, standardTags);
	}

	private Alerter smtp(SmtpAlerterConfig smtpAlerterConfig,
			Map<String, JsonElement> standardTags) {
		return new SmtpAlerter(smtpAlerterConfig, standardTags);
	}

	private SmtpAlerterConfig smtpConfig(String recipient,
			String severityFilter) {
		return new SmtpAlerterConfig(Arrays.asList(recipient),
				"noreply@elastisys.com", "subject", severityFilter,
				smtpClientConfig());
	}

	private SmtpClientConfig smtpClientConfig() {
		return new SmtpClientConfig("localhost", SMTP_SSL_PORT,
				new SmtpClientAuthentication(USERNAME, PASSWORD), true);
	}

	private HttpAlerterConfig httpConfig(String url, String severityFilter) {
		return new HttpAlerterConfig(Arrays.asList(url), severityFilter,
				new HttpAuthConfig(new BasicCredentials("user", "pass"), null));
	}

	/**
	 * Asserts that the given email message is an {@link Alert} with a given
	 * topic and severity.
	 *
	 * @param emailMessage
	 *            The email message.
	 * @param expectedTopic
	 *            The expected {@link Alert} topic.
	 * @param expectedSeverity
	 *            The expected {@link Alert} severity. Can be null, in which
	 *            case it is not checked.
	 */
	private static void assertAlertMail(MimeMessage emailMessage,
			String expectedTopic, AlertSeverity expectedSeverity) {
		Alert notificationAlert = extractAlert(emailMessage);
		assertThat(notificationAlert.getTopic(), is(expectedTopic));
		if (expectedSeverity != null) {
			assertThat(notificationAlert.getSeverity(), is(expectedSeverity));
		}
	}

	private static Alert extractAlert(MimeMessage emailMessage) {
		String notificationMail = GreenMailUtil.getBody(emailMessage);
		Alert notificationAlert = JsonUtils.toObject(
				JsonUtils.parseJsonString(notificationMail), Alert.class);
		return notificationAlert;
	}

	/**
	 * Asserts that the given email message is an {@link Alert} with a given
	 * topic.
	 *
	 * @param emailMessage
	 * @param expectedTopic
	 */
	private static void assertAlertMail(MimeMessage emailMessage,
			String expectedTopic) {
		assertAlertMail(emailMessage, expectedTopic, null);
	}
}
