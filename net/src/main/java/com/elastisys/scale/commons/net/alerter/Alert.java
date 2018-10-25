package com.elastisys.scale.commons.net.alerter;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.joda.time.DateTime;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.util.collection.Maps;
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
    /** A human-readable description of the triggering event. */
    private final String message;
    /**
     * Optional human-readable message carrying additional details about the
     * alert.
     */
    private final String details;
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
     *            A human-readable description of the triggering event.
     * @param details
     *            Optional human-readable message carrying additional details
     *            about the alert. May be <code>null</code>.
     */
    public Alert(String topic, AlertSeverity severity, DateTime timestamp, String message, String details) {
        this(topic, severity, timestamp, message, details, new HashMap<String, JsonElement>(0));
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
     *            A human-readable description of the triggering event.
     * @param details
     *            Optional human-readable message carrying additional details
     *            about the alert event. May be <code>null</code>.
     * @param metadata
     *            Additional JSON meta data about the {@link Alert} as a
     *            {@link Map} of meta data keys mapped to {@link JsonObject}s.
     */
    public Alert(String topic, AlertSeverity severity, DateTime timestamp, String message, String details,
            Map<String, JsonElement> metadata) {
        requireNonNull(topic, "topic cannot be null");
        requireNonNull(severity, "severity cannot be null");
        requireNonNull(timestamp, "timestamp cannot be null");
        requireNonNull(message, "message cannot be null");
        requireNonNull(metadata, "metadata cannot be null");

        this.topic = topic;
        this.severity = severity;
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.metadata = new TreeMap<>();
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
     * Returns a human-readable description of the triggering event.
     *
     * @return
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Returns a human-readable message carrying additional details about the
     * alert. May be <code>null</code>, if no details were supplied at creation
     * time.
     *
     * @return
     */
    public String getDetails() {
        return this.details;
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
        return withMetadata(Maps.of(tag, value));
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
        Map<String, JsonElement> extendedTags = new HashMap<>(getMetadata());
        extendedTags.putAll(additionalTags);

        return new Alert(this.topic, this.severity, this.timestamp, this.message, this.details, extendedTags);
    }

    @Override
    public String toString() {
        return JsonUtils.toString(JsonUtils.toJson(this));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.topic, this.severity, this.timestamp, this.message, this.details, this.metadata);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Alert) {
            Alert that = (Alert) other;
            return Objects.equals(this.topic, that.topic) //
                    && Objects.equals(this.severity, that.severity) //
                    && Objects.equals(this.timestamp, that.timestamp) //
                    && Objects.equals(this.message, that.message) //
                    && Objects.equals(this.details, that.details) //
                    && Objects.equals(this.metadata, that.metadata);
        } else {
            return false;
        }
    }

}
