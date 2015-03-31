package com.elastisys.scale.commons.net.smtp.alerter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.net.smtp.ClientAuthentication;
import com.elastisys.scale.commons.net.smtp.SmtpServerSettings;
import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;

/**
 * Exercise the mail sending of the {@link EmailAlerter} with a mocked JavaMail
 * implementation, which only sends email messages to an in-memory mail box.
 *
 *
 *
 */
public class TestEmailAlerter {
	private EventBus eventBus;

	@Before
	public void onSetup() {
		// clear Mock JavaMail box
		Mailbox.clearAll();
		// freeze current time in tests
		FrozenTime.setFixed(UtcTime.parse("2014-03-12T12:00:00Z"));

		this.eventBus = new EventBus();
	}

	@After
	public void onTearDown() {
		// clear Mock JavaMail box
		Mailbox.clearAll();

		// freeze current time in tests
		FrozenTime.setFixed(UtcTime.parse("2014-03-12T12:00:00.000Z"));
	}

	@Test
	public void sendUnauthenticatedNoSsl() throws Exception {
		assertTrue(Mailbox.get("recipient@elastisys.com").isEmpty());

		EmailAlerter alerter = new EmailAlerter(new SmtpServerSettings(
				"some.smtp.host", 25, null, false), new SendSettings(
				Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", null));
		this.eventBus.register(alerter);

		// post an Alert on the event bus
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.INFO,
				UtcTime.now(), "message"));

		// check mailbox after alert
		Mailbox mailbox = Mailbox.get("recipient@elastisys.com");
		assertThat(mailbox.size(), is(1));
		Object expectedContent = "{" //
				+ "\n  \"topic\": \"/alert/topic\","
				+ "\n  \"severity\": \"INFO\"," //
				+ "\n  \"timestamp\": \"2014-03-12T12:00:00.000Z\"," //
				+ "\n  \"message\": \"message\"," //
				+ "\n  \"metadata\": {}" //
				+ "\n}";
		assertThat(mailbox.get(0).getContent(), is(expectedContent));
		assertThat(mailbox.get(0).getSubject(), is("subject"));
		assertThat(mailbox.get(0).getSentDate(), is(FrozenTime.now().toDate()));
	}

	@Test
	public void sendAuthenticatedWithSsl() throws Exception {
		assertTrue(Mailbox.get("recipient@elastisys.com").isEmpty());

		EmailAlerter alerter = new EmailAlerter(new SmtpServerSettings(
				"some.smtp.host", 25, new ClientAuthentication("user", "pass"),
				true), new SendSettings(
				Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", null));
		this.eventBus.register(alerter);

		// post an Alert on the event bus
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.INFO,
				UtcTime.now(), "message"));

		// check mailbox after alert
		Mailbox mailbox = Mailbox.get("recipient@elastisys.com");
		assertThat(mailbox.size(), is(1));
		Object expectedContent = "{" //
				+ "\n  \"topic\": \"/alert/topic\","
				+ "\n  \"severity\": \"INFO\"," //
				+ "\n  \"timestamp\": \"2014-03-12T12:00:00.000Z\"," //
				+ "\n  \"message\": \"message\"," //
				+ "\n  \"metadata\": {}" //
				+ "\n}";
		assertThat(mailbox.get(0).getContent(), is(expectedContent));
		assertThat(mailbox.get(0).getSubject(), is("subject"));
		assertThat(mailbox.get(0).getSentDate(), is(FrozenTime.now().toDate()));
	}

	/**
	 * Test creating a {@link EmailAlerter} that will include standard tags in
	 * every alert it receives.
	 *
	 * @throws Exception
	 */
	@Test
	public void sendWithStandardTags() throws Exception {
		assertTrue(Mailbox.get("recipient@elastisys.com").isEmpty());

		Map<String, JsonElement> standardTags = ImmutableMap
				.of("tag1", JsonUtils.toJson("value1"), "tag2", JsonUtils
						.parseJsonString("{\"k1\": true, \"k2\": \"value2\"}"));
		SmtpServerSettings smtpServerSettings = new SmtpServerSettings(
				"some.smtp.host", 25, null, false);
		SendSettings sendSettings = new SendSettings(
				Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", null);
		EmailAlerter alerter = new EmailAlerter(smtpServerSettings,
				sendSettings, standardTags);
		this.eventBus.register(alerter);

		// post an Alert on the event bus
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.INFO,
				UtcTime.now(), "message"));

		// check mailbox after alert
		Mailbox mailbox = Mailbox.get("recipient@elastisys.com");
		assertThat(mailbox.size(), is(1));
		Object expectedContent = "{" //
				+ "\n  \"topic\": \"/alert/topic\","
				+ "\n  \"severity\": \"INFO\"," //
				+ "\n  \"timestamp\": \"2014-03-12T12:00:00.000Z\"," //
				+ "\n  \"message\": \"message\"," //
				+ "\n  \"metadata\": {" //
				+ "\n    \"tag1\": \"value1\"," //
				+ "\n    \"tag2\": {"//
				+ "\n      \"k1\": true," //
				+ "\n      \"k2\": \"value2\"" //
				+ "\n    }" //
				+ "\n  }" //
				+ "\n}";
		assertThat(mailbox.get(0).getContent(), is(expectedContent));
		assertThat(mailbox.get(0).getSubject(), is("subject"));
		assertThat(mailbox.get(0).getSentDate(), is(FrozenTime.now().toDate()));
	}

	/**
	 * Verify that {@link EmailAlerter} suppresses any {@link Alert}s whose
	 * {@link AlertSeverity} doesn't match the specified severity filter.
	 */
	@Test
	public void suppressAlertWithWrongSeverity() throws Exception {
		assertTrue(Mailbox.get("recipient@elastisys.com").isEmpty());

		// specify severity filter
		String severityFilter = "ERROR|FATAL";
		EmailAlerter alerter = new EmailAlerter(new SmtpServerSettings(
				"some.smtp.host", 25, new ClientAuthentication("user", "pass"),
				true), new SendSettings(
				Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", severityFilter));
		this.eventBus.register(alerter);

		// post Alerts with different severity on the event bus
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.INFO,
				UtcTime.now(), "info message"));
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.WARN,
				UtcTime.now(), "warn message"));
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.ERROR,
				UtcTime.now(), "error message"));
		this.eventBus.post(new Alert("/alert/topic", AlertSeverity.FATAL,
				UtcTime.now(), "fatal message"));

		// check mailbox after alerts: should only contain alerts that match
		// filter
		Mailbox mailbox = Mailbox.get("recipient@elastisys.com");
		assertThat(mailbox.size(), is(2));
		String mail1Body = (String) mailbox.get(0).getContent();
		String mail2Body = (String) mailbox.get(1).getContent();
		assertTrue(mail1Body.contains("\"severity\": \"ERROR\""));
		assertTrue(mail2Body.contains("\"severity\": \"FATAL\""));
	}
}
