package com.elastisys.scale.commons.net.alerter.smtp;

import static com.elastisys.scale.commons.net.smtp.SmtpTestServerUtil.assertAlertMail;
import static com.elastisys.scale.commons.net.smtp.SmtpTestServerUtil.extractAlert;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.AlertBuilder;
import com.elastisys.scale.commons.net.alerter.AlertSeverity;
import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.net.smtp.SmtpClientAuthentication;
import com.elastisys.scale.commons.net.smtp.SmtpClientConfig;
import com.elastisys.scale.commons.net.smtp.SmtpTestServerUtil;
import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;
import com.icegreen.greenmail.util.GreenMail;

/**
 * Exercise the mail sending of the {@link SmtpAlerter} against an embedded SMTP
 * server.
 */
public class TestSmtpAlerter {

	/** Trusted user on SSL server. */
	private static final String USERNAME = "user";
	/** Trusted user's password on SSL server. */
	private static final String PASSWORD = "password";

	/** Port where fake SMTP server is listening */
	private static int SMTP_PORT;
	/** Port where fake SMTP server is listening for SSL */
	private static int SMTP_SSL_PORT;

	static {
		List<Integer> freePorts = HostUtils.findFreePorts(2);
		SMTP_PORT = freePorts.get(0);
		SMTP_SSL_PORT = freePorts.get(1);
	}

	/** Fake email SMTP server without SSL. */
	private GreenMail insecureMailServer;

	/** Fake email SMTP server with SSL. */
	private GreenMail sslMailServer;

	private EventBus eventBus;

	@Before
	public void onSetup() {
		startServers();

		// freeze current time in tests
		FrozenTime.setFixed(UtcTime.parse("2014-03-12T12:00:00Z"));

		this.eventBus = new EventBus();
	}

	/**
	 * Start the mail servers used in the test.
	 */
	private void startServers() {
		this.insecureMailServer = SmtpTestServerUtil.startSmtpServer(SMTP_PORT);

		this.sslMailServer = SmtpTestServerUtil
				.startSslStmpServer(SMTP_SSL_PORT, USERNAME, PASSWORD);
	}

	@After
	public void onTearDown() {
		stopServers();
	}

	private void stopServers() {
		if (this.insecureMailServer != null) {
			this.insecureMailServer.stop();
		}
		if (this.sslMailServer != null) {
			this.sslMailServer.stop();
		}
	}

	@Test
	public void sendUnauthenticatedNoSsl() throws Exception {
		assertThat(this.insecureMailServer.getReceivedMessages().length, is(0));
		assertThat(this.sslMailServer.getReceivedMessages().length, is(0));

		SmtpClientConfig noAuthNoSslClient = new SmtpClientConfig("localhost",
				SMTP_PORT, null, false);
		SmtpAlerter alerter = new SmtpAlerter(new SmtpAlerterConfig(
				Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", null, noAuthNoSslClient));
		this.eventBus.register(alerter);

		// post an Alert on the event bus
		Alert alert = new Alert("/alert/topic", AlertSeverity.INFO,
				UtcTime.now(), "message", null);
		this.eventBus.post(alert);

		// check mailbox after alert
		MimeMessage[] receivedMessages = this.insecureMailServer
				.getReceivedMessages();
		assertThat(receivedMessages.length, is(1));
		assertThat(receivedMessages[0].getSubject(), is("subject"));
		assertThat(receivedMessages[0].getSentDate(),
				is(FrozenTime.now().toDate()));
		assertAlertMail(receivedMessages[0], alert);

		assertThat(this.sslMailServer.getReceivedMessages().length, is(0));
	}

	@Test
	public void sendAuthenticatedWithSsl() throws Exception {
		assertThat(this.insecureMailServer.getReceivedMessages().length, is(0));
		assertThat(this.sslMailServer.getReceivedMessages().length, is(0));

		SmtpClientConfig authSslClient = new SmtpClientConfig("localhost",
				SMTP_SSL_PORT, new SmtpClientAuthentication(USERNAME, PASSWORD),
				true);
		SmtpAlerter alerter = new SmtpAlerter(new SmtpAlerterConfig(
				Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", null, authSslClient));
		this.eventBus.register(alerter);

		// post an Alert on the event bus
		Alert alert = new Alert("/alert/topic", AlertSeverity.INFO,
				UtcTime.now(), "message", null);
		this.eventBus.post(alert);

		// check mailbox after alert
		MimeMessage[] receivedMessages = this.sslMailServer
				.getReceivedMessages();
		assertThat(receivedMessages.length, is(1));
		assertThat(receivedMessages[0].getSubject(), is("subject"));
		assertThat(receivedMessages[0].getSentDate(),
				is(FrozenTime.now().toDate()));
		assertAlertMail(receivedMessages[0], alert);

		assertThat(this.insecureMailServer.getReceivedMessages().length, is(0));
	}

	/**
	 * Test creating a {@link SmtpAlerter} that will include standard tags in
	 * every alert it receives.
	 *
	 * @throws Exception
	 */
	@Test
	public void sendWithStandardTags() throws Exception {
		assertThat(this.insecureMailServer.getReceivedMessages().length, is(0));
		assertThat(this.sslMailServer.getReceivedMessages().length, is(0));

		JsonElement tag2Value = JsonUtils
				.parseJsonString("{\"k1\": true, \"k2\": \"value2\"}");
		Map<String, JsonElement> standardTags = ImmutableMap.of("tag1",
				JsonUtils.toJson("value1"), "tag2", tag2Value);
		SmtpClientConfig clientSettings = new SmtpClientConfig("localhost",
				SMTP_PORT, null, false);
		SmtpAlerterConfig config = new SmtpAlerterConfig(
				Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", null, clientSettings);
		SmtpAlerter alerter = new SmtpAlerter(config, standardTags);
		this.eventBus.register(alerter);

		// post an Alert on the event bus
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.INFO,
				UtcTime.now(), "message", null));

		// check mailbox after alert
		MimeMessage[] receivedMessages = this.insecureMailServer
				.getReceivedMessages();
		assertThat(receivedMessages.length, is(1));
		assertThat(receivedMessages[0].getSubject(), is("subject"));
		assertThat(receivedMessages[0].getSentDate(),
				is(FrozenTime.now().toDate()));

		Alert expectedAlert = AlertBuilder.create().topic("/alert/topic")
				.severity(AlertSeverity.INFO).timestamp(UtcTime.now())
				.message("message").addMetadata("tag1", "value1")
				.addMetadata("tag2", tag2Value).build();
		assertAlertMail(receivedMessages[0], expectedAlert);
	}

	/**
	 * Verify that {@link SmtpAlerter} suppresses any {@link Alert}s whose
	 * {@link AlertSeverity} doesn't match the specified severity filter.
	 */
	@Test
	public void suppressAlertWithWrongSeverity() throws Exception {
		assertThat(this.sslMailServer.getReceivedMessages().length, is(0));

		// specify severity filter
		String severityFilter = "ERROR|FATAL";
		SmtpAlerter alerter = new SmtpAlerter(new SmtpAlerterConfig(
				Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", severityFilter,
				new SmtpClientConfig("localhost", SMTP_SSL_PORT,
						new SmtpClientAuthentication(USERNAME, PASSWORD),
						true)));
		this.eventBus.register(alerter);

		// post Alerts with different severity on the event bus
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.INFO,
				UtcTime.now(), "info message", null));
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.WARN,
				UtcTime.now(), "warn message", null));
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.ERROR,
				UtcTime.now(), "error message", null));
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.FATAL,
				UtcTime.now(), "fatal message", null));

		// check mailbox after alerts: should only contain alerts that match
		// filter
		MimeMessage[] receivedMessages = this.sslMailServer
				.getReceivedMessages();
		assertThat(receivedMessages.length, is(2));
		assertThat(extractAlert(receivedMessages[0]).getSeverity(),
				is(AlertSeverity.ERROR));
		assertThat(extractAlert(receivedMessages[1]).getSeverity(),
				is(AlertSeverity.FATAL));
	}

}
