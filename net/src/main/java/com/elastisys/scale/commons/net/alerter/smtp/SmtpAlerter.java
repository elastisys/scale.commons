package com.elastisys.scale.commons.net.alerter.smtp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.Alerter;
import com.elastisys.scale.commons.net.alerter.SeverityFilter;
import com.elastisys.scale.commons.net.smtp.SmtpMessage;
import com.elastisys.scale.commons.net.smtp.SmtpSender;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.base.Objects;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonElement;

/**
 * An alerter that sends the {@link Alert} event to a list of email recipients
 * whenever its {@link #handleAlert(Alert)} method is invoked.
 * <p/>
 * The {@link SmtpAlerter} is intended to be registered with an {@link EventBus}
 * . Whenever an {@link Alert} event is posted on the {@link EventBus}, it is
 * forwarded to the specified list of recipients.
 * <p/>
 * Note that it is the responsibility of the {@link SmtpAlerter} creator to
 * register the alerter to (and unregister the alerter from) an {@link EventBus}
 * .
 */
public class SmtpAlerter implements Alerter {
	static final Logger LOG = LoggerFactory.getLogger(SmtpAlerter.class);

	/**
	 * Configuration that governs how {@link Alert} emails are to be sent.
	 */
	private final SmtpAlerterConfig config;
	/**
	 * Standard meta data to append to every received {@link Alert} before
	 * sending to the final receiver.
	 */
	private final Map<String, JsonElement> standardMetadata;

	/**
	 * Constructs an {@link SmtpAlerter} configured to send {@link Alert} events
	 * through a given mail server to a given list of recipients.
	 *
	 * @param config
	 *            Configuration that governs how {@link Alert} emails are to be
	 *            sent.
	 */
	public SmtpAlerter(SmtpAlerterConfig config) {
		this(config, new HashMap<String, JsonElement>());
	}

	/**
	 * Constructs an {@link SmtpAlerter} configured to send {@link Alert} events
	 * through a given mail server to a given list of recipients.
	 *
	 * @param config
	 *            Configuration that governs how {@link Alert} emails are to be
	 *            sent.
	 * @param standardMetadata
	 *            Standard meta data to add to every {@link Alert} before
	 *            sending to the final receiver.
	 */
	public SmtpAlerter(SmtpAlerterConfig config,
			Map<String, JsonElement> standardMetadata) {
		this.config = config;
		this.standardMetadata = standardMetadata;
	}

	/**
	 * Forwards an {@link Alert} to the list of email recipients that this
	 * {@link SmtpAlerter} has been set up with, unless the {@link Alert} has a
	 * severity that doesn't match the severity filter in the
	 * {@link SmtpAlerterConfig} in which case the message will be suppressed.
	 * <p/>
	 * If this {@link SmtpAlerter} has been registered with an {@link EventBus}
	 * , all {@link Alert} events posted on the {@link EventBus} will
	 * automatically be passed to this method.
	 *
	 * @param alert
	 */
	@Override
	@Subscribe
	public void handleAlert(Alert alert) {
		// apply severity filter
		SeverityFilter severityFilter = this.config.getSeverityFilter();
		if (severityFilter.shouldSuppress(alert)) {
			if (LOG.isTraceEnabled()) {
				LOG.trace(
						"suppressing alert message with severity {}, "
								+ "as it doesn't match the severity filter '{}'.",
						alert.getSeverity().name(),
						severityFilter.getFilterExpression());
			}
			return;
		}

		Alert taggedAlert = appendStandardTags(alert);
		String alertMessage = JsonUtils
				.toPrettyString(JsonUtils.toJson(taggedAlert));
		try {
			LOG.debug("sending alert to {}: {}", this.config.getRecipients(),
					alert);
			SmtpMessage email = new SmtpMessage(this.config.getRecipients(),
					this.config.getSender(), this.config.getSubject(),
					alertMessage, UtcTime.now());
			new SmtpSender(email, this.config.getSmtpClientConfig()).call();
		} catch (Exception e) {
			LOG.error(String.format(
					"failed to send alert message: %s\nAlert message was: %s",
					e.getMessage(), alertMessage), e);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.config, this.standardMetadata);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SmtpAlerter) {
			SmtpAlerter that = (SmtpAlerter) obj;
			return Objects.equal(this.config, that.config) && Objects
					.equal(this.standardMetadata, that.standardMetadata);

		}
		return false;
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
