package com.elastisys.scale.commons.net.smtp.alerter.lab;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.elastisys.scale.commons.net.smtp.ClientAuthentication;
import com.elastisys.scale.commons.net.smtp.SmtpServerSettings;
import com.elastisys.scale.commons.net.smtp.alerter.Alert;
import com.elastisys.scale.commons.net.smtp.alerter.AlertSeverity;
import com.elastisys.scale.commons.net.smtp.alerter.EmailAlerter;
import com.elastisys.scale.commons.net.smtp.alerter.SendSettings;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

/**
 * A simple lab program that exercises the {@link EmailAlerter}.
 * <p/>
 * Some parameters need to be set as environment variables.
 */
public class SmtpAlerterLab {
	// TODO: NOTE: in order for this program to work, the dependency on
	// org.jvnet.mock-javamail needs to be disabled, or else the email library
	// will make use of a mock javax.mail provider.

	// TODO: make sure ${EMAIL_ADDRESS} is set
	private static final List<String> RECIPIENTS = Arrays.asList(System
			.getenv("EMAIL_ADDRESS"));
	// TODO: make sure ${EMAIL_SERVER} is set
	private static final String MAIL_SERVER = System.getenv("EMAIL_SERVER");
	private static final int MAIL_PORT = 25;
	private static final ClientAuthentication AUTH = null;
	private static final boolean USE_SSL = false;

	public static void main(String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(5);
		EventBus eventBus = new AsyncEventBus("alert-bus", executor);

		EmailAlerter alerter = new EmailAlerter(new SmtpServerSettings(
				MAIL_SERVER, MAIL_PORT, AUTH, USE_SSL), new SendSettings(
				RECIPIENTS, "noreply@elastisys.com", "alert message",
				"WARN|ERROR"));

		eventBus.register(alerter);
		// should NOT be sent (doesn't match severity filter)
		eventBus.post(new Alert("/topic", AlertSeverity.INFO, UtcTime
				.now(), "hello info"));
		// should be sent (matches severity filter)
		eventBus.post(new Alert("/topic", AlertSeverity.WARN, UtcTime
				.now(), "hello warning"));
		eventBus.unregister(alerter);

		executor.shutdownNow();
	}
}
