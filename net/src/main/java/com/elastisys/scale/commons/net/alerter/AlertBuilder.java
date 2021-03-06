package com.elastisys.scale.commons.net.alerter;

import static com.elastisys.scale.commons.util.precond.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.joda.time.DateTime;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AlertBuilder {

    /**
     * The topic that this {@link Alert} is categorized under. Although a topic
     * can be an arbitrary {@link String}, a useful convention is for a topic to
     * be structured as a {@code /}-separated path. For example,
     * {@code /cloudpool/size/CHANGED}.
     */
    private String topic;
    /** The severity of this {@link Alert}. */
    private AlertSeverity severity;
    /** The time at which the triggering event occurred. */
    private DateTime timestamp;
    /** A human-readable description of the triggering event. */
    private String message;
    /**
     * Optional human-readable message carrying additional details about the
     * alert.
     */
    private String details;

    /**
     * Additional JSON meta data about the {@link Alert} as a {@link Map} of
     * meta data keys mapped to a {@link JsonObject}s.
     */
    private Map<String, JsonElement> metadata = new HashMap<>();

    private AlertBuilder() {
    }

    public static AlertBuilder create() {
        return new AlertBuilder();
    }

    public Alert build() {
        checkArgument(this.topic != null && !this.topic.isEmpty(), "topic is required");
        checkArgument(this.severity != null, "severity is required");
        checkArgument(this.message != null, "message is required");

        this.timestamp = Optional.ofNullable(this.timestamp).orElse(UtcTime.now());

        return new Alert(this.topic, this.severity, this.timestamp, this.message, this.details, this.metadata);
    }

    /**
     * Sets the topic of the {@link Alert}. Although a topic can be an arbitrary
     * {@link String}, a useful convention is for a topic to be structured as a
     * {@code /}-separated path. For example, {@code /cloudpool/size/CHANGED}.
     *
     * @param topic
     * @return
     */
    public AlertBuilder topic(String topic) {
        this.topic = topic;
        return this;
    }

    /**
     * Sets the severity of the {@link Alert}.
     *
     * @param severity
     * @return
     */
    public AlertBuilder severity(AlertSeverity severity) {
        this.severity = severity;
        return this;
    }

    /**
     * Sets a human-readable description of the alert.
     *
     * @param message
     * @return
     */
    public AlertBuilder message(String message) {
        this.message = message;
        return this;
    }

    /**
     * Sets a human-readable message carrying additional details about the
     * alert.
     *
     * @param details
     * @return
     */
    public AlertBuilder details(String details) {
        this.details = details;
        return this;
    }

    /**
     * Sets the {@link Alert} time stamp.
     *
     * @param timestamp
     * @return
     */
    public AlertBuilder timestamp(DateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Adds a metadata tag to the {@link Alert}. The value object must be
     * JSON-serializable via Gson.
     *
     * @param key
     * @param value
     * @return
     */
    public AlertBuilder addMetadata(String key, Object value) {
        this.metadata.put(key, JsonUtils.toJson(value));
        return this;
    }

    /**
     * Adds a collection of metadata tags to the {@link Alert}.
     *
     * @param metadata
     * @return
     */
    public AlertBuilder addMetadata(Map<String, JsonElement> metadata) {
        this.metadata.putAll(metadata);
        return this;
    }
}
