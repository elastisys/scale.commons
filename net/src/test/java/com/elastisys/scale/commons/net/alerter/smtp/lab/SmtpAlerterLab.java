package com.elastisys.scale.commons.net.alerter.smtp.lab;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.AlertSeverity;
import com.elastisys.scale.commons.net.alerter.Alerter;
import com.elastisys.scale.commons.net.alerter.smtp.SmtpAlerter;
import com.elastisys.scale.commons.net.alerter.smtp.SmtpAlerterConfig;
import com.elastisys.scale.commons.net.smtp.SmtpClientAuthentication;
import com.elastisys.scale.commons.net.smtp.SmtpClientConfig;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

/**
 * A simple lab program that exercises the {@link SmtpAlerter}.
 * <p/>
 * Some parameters need to be set as environment variables.
 */
public class SmtpAlerterLab {
	// TODO: make sure ${EMAIL_ADDRESS} is set
	private static final List<String> RECIPIENTS = Arrays
			.asList(System.getenv("EMAIL_ADDRESS"));
	// TODO: make sure ${EMAIL_SERVER} is set
	private static final String MAIL_SERVER = System.getenv("EMAIL_SERVER");
	private static final int MAIL_PORT = 25;
	private static final SmtpClientAuthentication AUTH = null;
	private static final boolean USE_SSL = false;

	public static void main(String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(5);
		EventBus eventBus = new AsyncEventBus("alert-bus", executor);

		SmtpClientConfig smtpClientconfig = new SmtpClientConfig(MAIL_SERVER,
				MAIL_PORT, AUTH, USE_SSL);
		Alerter alerter = new SmtpAlerter(
				new SmtpAlerterConfig(RECIPIENTS, "noreply@elastisys.com",
						"alert message", "WARN|ERROR", smtpClientconfig));

		eventBus.register(alerter);
		// should NOT be sent (doesn't match severity filter)
		eventBus.post(new Alert("/topic", AlertSeverity.INFO, UtcTime.now(),
				"hello info", null));
		// should be sent (matches severity filter)
		eventBus.post(new Alert("/topic", AlertSeverity.WARN, UtcTime.now(),
				"hello warning", null));
		eventBus.unregister(alerter);

		executor.shutdownNow();
	}
}
