package com.elastisys.scale.commons.net.alerter.http;

import static java.lang.String.format;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.Alerter;
import com.elastisys.scale.commons.net.alerter.SeverityFilter;
import com.elastisys.scale.commons.net.alerter.smtp.SmtpAlerter;
import com.elastisys.scale.commons.net.http.client.AuthenticatedHttpClient;
import com.google.common.base.Objects;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonElement;

/**
 * An alerter that {@code POST}s JSON-formatted {@link Alert} messages to a list
 * of HTTP(S) endpoints, using authentication credentials if supplied.
 * <p/>
 * The {@link HttpAlerter} is intended to be registered with an {@link EventBus}
 * . Whenever an {@link Alert} event is posted on the {@link EventBus}, it is
 * forwarded to the specified list of recipients.
 * <p/>
 * Note that it is the responsibility of the {@link SmtpAlerter} creator to
 * register the alerter to (and unregister the alerter from) an {@link EventBus}
 * .
 */
public class HttpAlerter implements Alerter {

	private static final Logger LOG = LoggerFactory
			.getLogger(HttpAlerter.class);

	/** Configuration. */
	private final HttpAlerterConfig config;
	/**
	 * Standard meta data to append to every received {@link Alert} before
	 * sending to the destination endpoint(s).
	 */
	private final Map<String, JsonElement> standardMetadata;

	/**
	 * Constructs an {@link SmtpAlerter} configured to send {@link Alert} events
	 * through a given mail server to a given list of recipients.
	 *
	 * @param smtpServerSettings
	 *            SMTP server settings.
	 * @param sendSettings
	 *            Common settings for the {@link Alert} emails to be sent.
	 * @param standardMetadata
	 *            Standard meta data to add to every {@link Alert} before
	 *            sending to the final receiver. Can be <code>null</code>.
	 */
	public HttpAlerter(HttpAlerterConfig config,
			Map<String, JsonElement> standardMetadata) {
		this.config = config;
		this.standardMetadata = standardMetadata;
	}

	@Subscribe
	@Override
	public void handleAlert(Alert alert) throws RuntimeException {
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

		// post message to destinations
		Alert taggedAlert = appendStandardTags(alert);
		String message = JsonUtils
				.toPrettyString(JsonUtils.toJson(taggedAlert));
		for (String destinationUrl : this.config.getDestinationUrls()) {
			try {
				LOG.debug("sending alert to {}: {}", destinationUrl, alert);
				AuthenticatedHttpClient httpClient = new AuthenticatedHttpClient(
						LOG, this.config.getAuth().getBasicCredentials(),
						this.config.getAuth().getCertificateCredentials(),
						this.config.getConnectTimeout(),
						this.config.getSocketTimeout());
				HttpPost request = new HttpPost(destinationUrl);
				request.setEntity(new StringEntity(message, APPLICATION_JSON));
				httpClient.execute(request);
			} catch (Exception e) {
				LOG.warn(
						format("failed to send alert to %s: %s\nAlert message was: %s",
								destinationUrl, e.getMessage(), message),
						e);
			}
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.config, this.standardMetadata);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HttpAlerter) {
			HttpAlerter that = (HttpAlerter) obj;
			return Objects.equal(this.config, that.config) && Objects
					.equal(this.standardMetadata, that.standardMetadata);

		}
		return false;
	}

	private Alert appendStandardTags(Alert alert) {
		if (this.standardMetadata == null) {
			return alert;
		}

		for (Entry<String, JsonElement> standardMetadata : this.standardMetadata
				.entrySet()) {
			alert = alert.withMetadata(standardMetadata.getKey(),
					standardMetadata.getValue());
		}
		return alert;
	}

}
