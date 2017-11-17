package com.elastisys.scale.commons.net.alerter.multiplexing;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.json.types.TimeInterval;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.Alerter;
import com.elastisys.scale.commons.net.alerter.filtering.FilteringAlerter;
import com.elastisys.scale.commons.net.alerter.http.HttpAlerterConfig;
import com.elastisys.scale.commons.net.alerter.smtp.SmtpAlerterConfig;
import com.google.gson.annotations.SerializedName;

/**
 * Describes a number of SMTP and HTTP {@link Alerter}s and for how long to
 * suppress duplicate {@link Alert}s from being re-sent. Two {@link Alert}s are
 * considered equal if they share topic, message and metadata tags (see
 * {@link FilteringAlerter#DEFAULT_IDENTITY_FUNCTION}).
 *
 * @see MultiplexingAlerter
 */
public class AlertersConfig {
    /** Default duplicate suppression. */
    public static final TimeInterval DEFAULT_DUPLICATE_SUPPRESSION = new TimeInterval(2L, TimeUnit.HOURS);

    /** A list of configured SMTP email {@link Alerter}s. */
    @SerializedName("smtp")
    private final List<SmtpAlerterConfig> smtpAlerters;

    /** A list of HTTP(S) webhook {@link Alerter}s. */
    @SerializedName("http")
    private final List<HttpAlerterConfig> httpAlerters;

    /**
     * Duration of time to suppress duplicate {@link Alert}s from being re-sent.
     * {@link Alert} equality is determined by the identity function that was
     * supplied on creation of the {@link FilteringAlerter}.
     */
    private final TimeInterval duplicateSuppression;

    /**
     * Constructs a new {@link AlertersConfig} instance with default duplicate
     * suppression.
     *
     * @param smtpAlerters
     *            A list of configured SMTP email {@link Alerter}s. A
     *            <code>null</code> value is equivalent to an empty list.
     * @param httpAlerters
     *            A list of HTTP(S) webhook {@link Alerter}s. A
     *            <code>null</code> value is equivalent to an empty list.
     */
    public AlertersConfig(List<SmtpAlerterConfig> smtpAlerters, List<HttpAlerterConfig> httpAlerters) {
        this(smtpAlerters, httpAlerters, null);
    }

    /**
     * Constructs a new {@link AlertersConfig} instance.
     *
     * @param smtpAlerters
     *            A list of configured SMTP email {@link Alerter}s. A
     *            <code>null</code> value is equivalent to an empty list.
     * @param httpAlerters
     *            A list of HTTP(S) webhook {@link Alerter}s. A
     *            <code>null</code> value is equivalent to an empty list.
     * @param duplicateSuppression
     *            Duration of time to suppress duplicate {@link Alert}s from
     *            being re-sent. {@link Alert} equality is determined by the
     *            identity function that was supplied on creation of the
     *            {@link FilteringAlerter}. May be <code>null</code>. Default: 5
     *            minutes.
     */
    public AlertersConfig(List<SmtpAlerterConfig> smtpAlerters, List<HttpAlerterConfig> httpAlerters,
            TimeInterval duplicateSuppression) {
        this.smtpAlerters = smtpAlerters;
        this.httpAlerters = httpAlerters;
        this.duplicateSuppression = duplicateSuppression;
    }

    /**
     * Returns the configured SMTP email {@link Alerter}s.
     *
     * @return
     */
    public List<SmtpAlerterConfig> getSmtpAlerters() {
        if (this.smtpAlerters == null) {
            return Collections.emptyList();
        }
        return this.smtpAlerters;
    }

    /**
     * Returns the configured HTTP(S) webhook {@link Alerter}s.
     *
     * @return
     */
    public List<HttpAlerterConfig> getHttpAlerters() {
        if (this.httpAlerters == null) {
            return Collections.emptyList();
        }
        return this.httpAlerters;
    }

    /**
     * Duration of time to suppress duplicate {@link Alert}s from being re-sent.
     * {@link Alert} equality is determined by the identity function that was
     * supplied on creation of the {@link FilteringAlerter}.
     *
     * @return
     */
    public TimeInterval getDuplicateSuppression() {
        return Optional.ofNullable(this.duplicateSuppression).orElse(DEFAULT_DUPLICATE_SUPPRESSION);
    }

    /**
     * Performs basic validation of this configuration.
     *
     * @throws IllegalArgumentException
     */
    public void validate() throws IllegalArgumentException {
        try {
            for (SmtpAlerterConfig smtpAlerterConfig : getSmtpAlerters()) {
                smtpAlerterConfig.validate();
            }
            for (HttpAlerterConfig httpAlerterConfig : getHttpAlerters()) {
                httpAlerterConfig.validate();
            }
            getDuplicateSuppression().validate();
        } catch (Exception e) {
            throw new IllegalArgumentException("alerter: " + e.getMessage(), e);
        }

    }

    @Override
    public int hashCode() {
        return Objects.hash(getSmtpAlerters(), getHttpAlerters(), getDuplicateSuppression());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AlertersConfig) {
            AlertersConfig that = (AlertersConfig) obj;
            return Objects.equals(getSmtpAlerters(), that.getSmtpAlerters()) //
                    && Objects.equals(getHttpAlerters(), that.getHttpAlerters()) //
                    && Objects.equals(getDuplicateSuppression(), that.getDuplicateSuppression());
        }
        return false;
    }

    @Override
    public String toString() {
        return JsonUtils.toPrettyString(JsonUtils.toJson(this));
    }

}