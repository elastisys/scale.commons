package com.elastisys.scale.commons.net.smtp.alerter;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.joda.time.DateTime;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

/**
 * Represents a system event that can be sent by an {@link EmailAlerter}.
 * 
 * @see EmailAlerter
 */
public class Alert {
	/**
	 * The topic that this {@link Alert} is categorized under. Although a topic
	 * can be an arbitrary {@link String}, a useful convention is for a topic to
	 * be structured as a {@code /}-separated path, with the subsystem that
	 * produced the {@link Alert} as the leading path entry. For example,
	 * {@code /cloudAdapter/machinePool/CHANGED}.
	 */
	private final String topic;
	/** The severity of this {@link Alert}. */
	private final AlertSeverity severity;
	/** The time at which the triggering event occurred. */
	private final DateTime timestamp;
	/** A message with details about the triggering event. */
	private final String message;
	/**
	 * A collection of tags that carry additional meta data about the
	 * {@link Alert}.
	 */
	private final SortedMap<String, String> tags;

	/**
	 * Constructs a new {@link Alert} without tags.
	 * 
	 * @param topic
	 *            The topic that this {@link Alert} is categorized under.
	 *            Although a topic can be an arbitrary {@link String}, the
	 *            convention is for a topic to be structured as a {@code /}
	 *            -separated path , with the subsystem that produced the
	 *            {@link Alert} as the leading path entry. For example,
	 *            {@code /cloudAdapter/machinePool/CHANGED}.
	 * @param severity
	 *            The severity of this {@link Alert}.
	 * @param timestamp
	 *            The time at which the triggering event occurred.
	 * @param message
	 *            A message with details about the triggering event.
	 */
	public Alert(String topic, AlertSeverity severity, DateTime timestamp,
			String message) {
		this(topic, severity, timestamp, message,
				new HashMap<String, String>(0));
	}

	/**
	 * Constructs a new {@link Alert} with a collection of tags.
	 * 
	 * @param topic
	 *            The topic that this {@link Alert} is categorized under.
	 *            Although a topic can be an arbitrary {@link String}, the
	 *            convention is for a topic to be structured as a {@code /}
	 *            -separated path , with the subsystem that produced the
	 *            {@link Alert} as the leading path entry. For example,
	 *            {@code /cloudAdapter/machinePool/CHANGED}.
	 * @param severity
	 *            The severity of this {@link Alert}.
	 * @param timestamp
	 *            The time at which the triggering event occurred.
	 * @param message
	 *            A message with details about the triggering event.
	 * @param tags
	 *            A collection of tags that carry additional meta data about the
	 *            {@link Alert}.
	 *            <p/>
	 *            These name-value pairs can, for example, be used to convey
	 *            meta data about the {@link AutoScaler} that produced the
	 *            {@link Alert} (such as {@link AutoScaler} id, host). Such meta
	 *            data is useful to distinguish {@link AutoScaler} messages from
	 *            each other in scenarios where several {@link AutoScaler}
	 *            instances are employed (for example, to achieve high
	 *            availability or to monitor several application layers).
	 */
	public Alert(String topic, AlertSeverity severity, DateTime timestamp,
			String message, Map<String, String> tags) {
		checkNotNull(topic, "topic cannot be null");
		checkNotNull(severity, "severity cannot be null");
		checkNotNull(timestamp, "timestamp cannot be null");
		checkNotNull(message, "message cannot be null");
		checkNotNull(tags, "tags cannot be null");

		this.topic = topic;
		this.severity = severity;
		this.timestamp = timestamp;
		this.message = message;
		this.tags = Maps.newTreeMap();
		this.tags.putAll(tags);
	}

	/**
	 * Returns the topic that this {@link Alert} is categorized under.
	 * <p/>
	 * Although a topic can be an arbitrary {@link String}, the convention is
	 * for a topic to be structured as a {@code /} -separated path , with the
	 * subsystem that produced the {@link Alert} as the leading path entry. For
	 * example, {@code /cloudAdapter/machinePool/CHANGED}.
	 * 
	 * @return
	 */
	public String getTopic() {
		return this.topic;
	}

	/**
	 * Returns the severity of this {@link Alert}.
	 * 
	 * @return
	 */
	public AlertSeverity getSeverity() {
		return this.severity;
	}

	/**
	 * Returns the time at which the triggering event occurred that gave rise to
	 * this {@link Alert}.
	 * 
	 * @return
	 */
	public DateTime getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Returns a message with details about the triggering event of this
	 * {@link Alert}.
	 * 
	 * @return
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Returns the collection of tags that carry additional meta data about the
	 * {@link Alert}.
	 * 
	 * @return
	 */
	public Map<String, String> getTags() {
		return this.tags;
	}

	/**
	 * Builds a copy of this {@link Alert} with an additional tag added. The
	 * original object (this) remains unchanged.
	 * 
	 * @param tag
	 *            The tag key.
	 * @param value
	 *            The tag value.
	 * @return A field-by-field copy with an additional tag.
	 */
	public Alert withTag(String tag, String value) {
		Map<String, String> extendedTags = Maps.newHashMap(getTags());
		extendedTags.put(tag, value);

		return new Alert(this.topic, this.severity, this.timestamp,
				this.message, extendedTags);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("topic", this.topic)
				.add("severity", this.severity)
				.add("timestamp", this.timestamp).add("message", this.message)
				.add("tags", this.tags).toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.topic, this.severity, this.timestamp,
				this.message, this.tags);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Alert) {
			Alert that = (Alert) other;
			return Objects.equal(this.topic, that.topic)
					&& Objects.equal(this.severity, that.severity)
					&& Objects.equal(this.timestamp, that.timestamp)
					&& Objects.equal(this.message, that.message)
					&& Objects.equal(this.tags, that.tags);
		} else {
			return false;
		}
	}

}
