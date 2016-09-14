package com.elastisys.scale.commons.net.alerter.multiplexing;

import static com.google.common.base.Objects.equal;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.json.types.TimeInterval;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.Alerter;
import com.elastisys.scale.commons.net.alerter.filtering.FilteringAlerter;
import com.elastisys.scale.commons.net.alerter.http.HttpAlerterConfig;
import com.elastisys.scale.commons.net.alerter.smtp.SmtpAlerterConfig;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
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
    public static final TimeInterval DEFAULT_DUPLICATE_SUPPRESSION = new TimeInterval(5L, TimeUnit.MINUTES);

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
        return Optional.fromNullable(this.duplicateSuppression).or(DEFAULT_DUPLICATE_SUPPRESSION);
    }

    /**
     * Performs basic validation of this configuration.
     *
     * @throws IllegalArgumentException
     */
    public void validate() throws IllegalArgumentException {
        for (SmtpAlerterConfig smtpAlerterConfig : getSmtpAlerters()) {
            try {
                smtpAlerterConfig.validate();
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format("illegal alerters configuration: smtp alerter: %s", e.getMessage()), e);
            }
        }
        for (HttpAlerterConfig httpAlerterConfig : getHttpAlerters()) {
            try {
                httpAlerterConfig.validate();
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format("illegal alerters configuration: http alerter: %s", e.getMessage()), e);
            }
        }
        try {
            getDuplicateSuppression().validate();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("illegal alerters configuration: duplicate suppression: %s", e.getMessage()), e);
        }

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getSmtpAlerters(), this.getHttpAlerters(), this.getDuplicateSuppression());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AlertersConfig) {
            AlertersConfig that = (AlertersConfig) obj;
            return equal(this.getSmtpAlerters(), that.getSmtpAlerters())
                    && equal(this.getHttpAlerters(), that.getHttpAlerters())
                    && equal(this.getDuplicateSuppression(), that.getDuplicateSuppression());
        }
        return false;
    }

    @Override
    public String toString() {
        return JsonUtils.toPrettyString(JsonUtils.toJson(this));
    }

}