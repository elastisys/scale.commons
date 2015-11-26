package com.elastisys.scale.commons.net.alerter.multiplexing;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

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
import com.elastisys.scale.commons.net.smtp.SmtpClientConfig;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;

/**
 * Exercise the {@link MultiplexingAlerter} class.
 */
public class TestMultiplexingAlerter {

	private EventBus eventBus = new EventBus();

	/** Object under test. */
	private MultiplexingAlerter alertHandler;

	@Before
	public void beforeTestMethod() {

		// freeze current time in tests
		FrozenTime.setFixed(UtcTime.parse("2015-01-01T12:00:00Z"));

		this.alertHandler = new MultiplexingAlerter(this.eventBus);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithoutEventbus() {
		new MultiplexingAlerter(null);
	}

	/**
	 * <code>null</code> values are interpreted as "none".
	 */
	@Test
	public void registerNullAlertConfig() {
		this.alertHandler.registerAlerters(null, null);
		assertThat(this.alertHandler.alerters(), is(alerters()));
	}

	@Test
	public void registerEmptyAlerterConfig() {
		this.alertHandler.registerAlerters(alertConfig(null, null, null), null);
		assertThat(this.alertHandler.alerters(), is(alerters()));
	}

	@Test
	public void registerHttpAlerterConfigWithoutStandardTags() {
		List<SmtpAlerterConfig> smtpAlerters = null;
		HttpAlerterConfig http1Config = httpConfig("http://hook", ".*");
		List<HttpAlerterConfig> httpAlerters = Arrays.asList(http1Config);
		Map<String, JsonElement> standardTags = ImmutableMap.of();

		AlertersConfig alertConfig = alertConfig(smtpAlerters, httpAlerters,
				null);

		this.alertHandler.registerAlerters(alertConfig, standardTags);
		assertThat(this.alertHandler.alerters(),
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

		this.alertHandler.registerAlerters(alertConfig, standardTags());
		assertThat(this.alertHandler.alerters(),
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

		this.alertHandler.registerAlerters(alertConfig, standardTags);
		assertThat(this.alertHandler.alerters(),
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

		this.alertHandler.registerAlerters(alertConfig, standardTags());
		assertThat(this.alertHandler.alerters(),
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

		this.alertHandler.registerAlerters(alertConfig, standardTags());
		assertThat(this.alertHandler.alerters(),
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
		this.alertHandler.registerAlerters(alert1Config, standardTags());
		assertThat(this.alertHandler.alerters(),
				is(alerters(filtered(smtp(smtp1Conf, standardTags()),
						alert1Config.getDuplicateSuppression()))));

		// register an additional http alerter
		HttpAlerterConfig http1Conf = httpConfig("http://hook", ".*");
		List<HttpAlerterConfig> httpAlerters = Arrays.asList(http1Conf);
		AlertersConfig alert2Config = alertConfig(null, httpAlerters,
				new TimeInterval(18L, TimeUnit.MINUTES));
		this.alertHandler.registerAlerters(alert2Config, standardTags());
		assertThat(this.alertHandler.alerters(),
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
		this.alertHandler.registerAlerters(alertConfig, standardTags());
		assertThat(this.alertHandler.alerters(),
				is(alerters(
						filtered(smtp(smtp1Conf, standardTags()),
								alertConfig.getDuplicateSuppression()),
						filtered(http(http1Conf, standardTags()),
								alertConfig.getDuplicateSuppression()))));

		// unregister
		this.alertHandler.unregisterAlerters();
		assertThat(this.alertHandler.alerters(), is(alerters()));
	}

	/**
	 * Make sure {@link Alert}s are dispatched to registered {@link Alerter}s.
	 */
	@Test
	public void dispatch() throws Exception {
		// set up a mock JavaMail box
		Mailbox.clearAll();
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
			this.alertHandler.registerAlerters(alertConfig, standardTags());

			assertThat(webServer.getPostedMessages().size(), is(0));
			assertThat(Mailbox.get("recipient@company.com").size(), is(0));

			// dispatch alert
			Alert alert = AlertBuilder.create().topic("topic")
					.severity(AlertSeverity.ERROR).message("error!").build();
			String expectedMessage = JsonUtils.toPrettyString(
					JsonUtils.toJson(alert.withMetadata(standardTags())));

			this.alertHandler.handleAlert(alert);
			// verify that alert (with the set standardTags) was dispatched to
			// http and smtp alerters
			assertThat(webServer.getPostedMessages().size(), is(1));
			assertThat(webServer.getPostedMessages().get(0),
					is(expectedMessage));
			assertThat(Mailbox.get("recipient@company.com").size(), is(1));
			assertThat(Mailbox.get("recipient@company.com").get(0).getContent(),
					is(expectedMessage));
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
		// set up a mock JavaMail box
		Mailbox.clearAll();
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
			this.alertHandler.registerAlerters(alertConfig, standardTags());

			assertThat(webServer.getPostedMessages().size(), is(0));
			assertThat(Mailbox.get("recipient@company.com").size(), is(0));

			// dispatch alert
			Alert alert = AlertBuilder.create().topic("topic")
					.severity(AlertSeverity.ERROR).message("error!").build();

			this.alertHandler.handleAlert(alert);
			// verify that alert was dispatched to http and smtp alerters
			assertThat(webServer.getPostedMessages().size(), is(1));
			assertThat(Mailbox.get("recipient@company.com").size(), is(1));

			FrozenTime.tick(60);
			// dispatch a duplicate alert, should be filtered out
			this.alertHandler.handleAlert(alert);
			assertThat(webServer.getPostedMessages().size(), is(1));
			assertThat(Mailbox.get("recipient@company.com").size(), is(1));

			FrozenTime.tick(60);
			// dispatch another duplicate alert, should be filtered out
			this.alertHandler.handleAlert(alert);
			assertThat(webServer.getPostedMessages().size(), is(1));
			assertThat(Mailbox.get("recipient@company.com").size(), is(1));

			FrozenTime.tick(61);
			// duplicate suppression has passed, alert should no longer be
			// filtered
			this.alertHandler.handleAlert(alert);
			assertThat(webServer.getPostedMessages().size(), is(2));
			assertThat(Mailbox.get("recipient@company.com").size(), is(2));
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
		return new SmtpClientConfig("some.mail.host", 25, null);
	}

	private HttpAlerterConfig httpConfig(String url, String severityFilter) {
		return new HttpAlerterConfig(Arrays.asList(url), severityFilter,
				new HttpAuthConfig(new BasicCredentials("user", "pass"), null));
	}

}
