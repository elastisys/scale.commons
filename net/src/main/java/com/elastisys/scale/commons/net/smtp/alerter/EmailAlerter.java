package com.elastisys.scale.commons.net.smtp.alerter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.net.smtp.SmtpMessage;
import com.elastisys.scale.commons.net.smtp.SmtpSender;
import com.elastisys.scale.commons.net.smtp.SmtpServerSettings;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonElement;

/**
 * An alerter that, whenever its {@link #handleAlert(Alert)} method is invoked,
 * sends the {@link Alert} event to a list of email recipients.
 * <p/>
 * The {@link EmailAlerter} is intended to be registered with an
 * {@link EventBus} . Whenever an {@link Alert} event is posted on the
 * {@link EventBus}, it is forwarded to the specified list of recipients. Note
 * that it is the responsibility of the {@link EmailAlerter} creator to register
 * the alerter to (and unregister the alerter from) an {@link EventBus}.
 */
public class EmailAlerter {
	static final Logger LOG = LoggerFactory.getLogger(EmailAlerter.class);

	/** SMTP server settings. */
	private final SmtpServerSettings smtpServerSettings;
	/** Common settings for the {@link Alert} emails to be sent. */
	private final SendSettings sendSettings;
	/**
	 * Standard meta data to append to every received {@link Alert} before
	 * sending to the final receiver.
	 */
	private final Map<String, JsonElement> standardMetadata;

	/**
	 * Constructs an {@link EmailAlerter} configured to send {@link Alert}
	 * events through a given mail server to a given list of recipients.
	 *
	 * @param smtpServerSettings
	 *            SMTP server settings.
	 * @param sendSettings
	 *            Common settings for the {@link Alert} emails to be sent.
	 */
	public EmailAlerter(SmtpServerSettings smtpServerSettings,
			SendSettings sendSettings) {
		this(smtpServerSettings, sendSettings,
				new HashMap<String, JsonElement>());
	}

	/**
	 * Constructs an {@link EmailAlerter} configured to send {@link Alert}
	 * events through a given mail server to a given list of recipients.
	 *
	 * @param smtpServerSettings
	 *            SMTP server settings.
	 * @param sendSettings
	 *            Common settings for the {@link Alert} emails to be sent.
	 * @param standardMetadata
	 *            Standard meta data to every received {@link Alert} before
	 *            sending to the final receiver.
	 */
	public EmailAlerter(SmtpServerSettings smtpServerSettings,
			SendSettings sendSettings, Map<String, JsonElement> standardMetadata) {
		this.smtpServerSettings = smtpServerSettings;
		this.sendSettings = sendSettings;
		this.standardMetadata = standardMetadata;
	}

	/**
	 * Forwards an {@link Alert} to the list of email recipients that this
	 * {@link EmailAlerter} has been set up with, unless the {@link Alert} has a
	 * severity that doesn't match the severity filter in the
	 * {@link SendSettings} in which case the message will be suppressed.
	 * <p/>
	 * If this {@link EmailAlerter} has been registered with an {@link EventBus}
	 * , all {@link Alert} events posted on the {@link EventBus} will
	 * automatically be passed to this method.
	 *
	 * @param alert
	 */
	@Subscribe
	public void handleAlert(Alert alert) {
		// apply severity filter
		String severityFilter = this.sendSettings.getSeverityFilter();
		String severity = alert.getSeverity().name();
		if (!Pattern.matches(severityFilter, severity)) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("suppressing alert message with severity {}, "
						+ "as it doesn't match the severity filter '{}'.",
						severity, severityFilter);
			}
			return;
		}

		Alert taggedAlert = appendStandardTags(alert);
		String alertMessage = JsonUtils.toPrettyString(JsonUtils
				.toJson(taggedAlert));
		try {
			LOG.debug("sending alert to {}: {}",
					this.sendSettings.getRecipients(), alert);
			SmtpMessage email = new SmtpMessage(
					this.sendSettings.getRecipients(),
					this.sendSettings.getSender(),
					this.sendSettings.getSubject(), alertMessage, UtcTime.now());
			new SmtpSender(email, this.smtpServerSettings).call();
		} catch (Exception e) {
			LOG.error(String.format(
					"failed to send alert message: %s\nAlert message was: %s",
					e.getMessage(), alertMessage), e);
		}
	}

	private Alert appendStandardTags(Alert alert) {
		for (Entry<String, JsonElement> standardMetadata : this.standardMetadata
				.entrySet()) {
			alert = alert.withMetadata(standardMetadata.getKey(),
					standardMetadata.getValue());
		}
		return alert;
	}
}
