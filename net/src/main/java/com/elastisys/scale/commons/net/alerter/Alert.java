package com.elastisys.scale.commons.net.alerter;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents a system event that can be sent by an {@link Alerter}.
 *
 * @see Alerter
 */
public class Alert {
	/**
	 * The topic that this {@link Alert} is categorized under. Although a topic
	 * can be an arbitrary {@link String}, a useful convention is for a topic to
	 * be structured as a {@code /}-separated path. For example,
	 * {@code /cloudpool/size/CHANGED}.
	 */
	private final String topic;
	/** The severity of this {@link Alert}. */
	private final AlertSeverity severity;
	/** The time at which the triggering event occurred. */
	private final DateTime timestamp;
	/** A message with details about the triggering event. */
	private final String message;
	/**
	 * Additional JSON meta data about the {@link Alert} as a {@link Map} of
	 * meta data keys mapped to a {@link JsonObject}s.
	 */
	private final Map<String, JsonElement> metadata;

	/**
	 * Constructs a new {@link Alert} without metadata tags.
	 *
	 * @param topic
	 *            The topic that this {@link Alert} is categorized under.
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
				new HashMap<String, JsonElement>(0));
	}

	/**
	 * Constructs a new {@link Alert} with a collection of tags.
	 *
	 * @param topic
	 *            The topic that this {@link Alert} is categorized under.
	 * @param severity
	 *            The severity of this {@link Alert}.
	 * @param timestamp
	 *            The time at which the triggering event occurred.
	 * @param message
	 *            A message with details about the triggering event.
	 * @param metadata
	 *            Additional JSON meta data about the {@link Alert} as a
	 *            {@link Map} of meta data keys mapped to {@link JsonObject}s.
	 */
	public Alert(String topic, AlertSeverity severity, DateTime timestamp,
			String message, Map<String, JsonElement> metadata) {
		checkNotNull(topic, "topic cannot be null");
		checkNotNull(severity, "severity cannot be null");
		checkNotNull(timestamp, "timestamp cannot be null");
		checkNotNull(message, "message cannot be null");
		checkNotNull(metadata, "metadata cannot be null");

		this.topic = topic;
		this.severity = severity;
		this.timestamp = timestamp;
		this.message = message;
		this.metadata = Maps.newTreeMap();
		this.metadata.putAll(metadata);
	}

	/**
	 * Returns the topic that this {@link Alert} is categorized under.
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
	 * Returns meta data about the {@link Alert} as a {@link Map} of meta data
	 * keys mapped to a {@link JsonObject}.
	 *
	 * @return
	 */
	public Map<String, JsonElement> getMetadata() {
		return this.metadata;
	}

	/**
	 * Builds a copy of this {@link Alert} with an additional metadata tag
	 * added. The original object (this) remains unchanged.
	 *
	 * @param tag
	 *            The tag key.
	 * @param value
	 *            The tag value.
	 * @return A field-by-field copy with an additional tag.
	 */
	public Alert withMetadata(String tag, JsonElement value) {
		return withMetadata(ImmutableMap.of(tag, value));
	}

	/**
	 * Builds a copy of this {@link Alert} with additional metadata tags. The
	 * original object (this) remains unchanged.
	 *
	 * @param additionalTags
	 *            The additional metadata tags.
	 * @return A field-by-field copy with additional metadata tags.
	 */
	public Alert withMetadata(Map<String, JsonElement> additionalTags) {
		Map<String, JsonElement> extendedTags = Maps.newHashMap(getMetadata());
		extendedTags.putAll(additionalTags);

		return new Alert(this.topic, this.severity, this.timestamp,
				this.message, extendedTags);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("topic", this.topic)
				.add("severity", this.severity).add("timestamp", this.timestamp)
				.add("message", this.message).add("metadata", this.metadata)
				.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.topic, this.severity, this.timestamp,
				this.message, this.metadata);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Alert) {
			Alert that = (Alert) other;
			return Objects.equal(this.topic, that.topic)
					&& Objects.equal(this.severity, that.severity)
					&& Objects.equal(this.timestamp, that.timestamp)
					&& Objects.equal(this.message, that.message)
					&& Objects.equal(this.metadata, that.metadata);
		} else {
			return false;
		}
	}

}
