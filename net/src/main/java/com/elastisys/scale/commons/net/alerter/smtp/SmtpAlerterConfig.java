package com.elastisys.scale.commons.net.alerter.smtp;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.AlertSeverity;
import com.elastisys.scale.commons.net.alerter.SeverityFilter;
import com.elastisys.scale.commons.net.smtp.SmtpClientConfig;
import com.elastisys.scale.commons.net.validate.ValidEmailAddress;

/**
 * Common email send settings for the {@link SmtpAlerter}.
 *
 * @see SmtpAlerter
 */
public class SmtpAlerterConfig {
    /**
     * The default severity filter to apply to {@link Alert}s. This filter
     * accepts any severity.
     */
    public static final String DEFAULT_SEVERITY_FILTER = ".*";

    /** The email recipients to use in sent mails ({@code To:}). */
    private final List<String> recipients;
    /** The email sender to use in sent mails ({@code From:}). */
    private final String sender;
    /** The email subject line. */
    private final String subject;
    /**
     * The regular expression used to filter {@link Alert}s. {@link Alert}s with
     * an {@link AlertSeverity} that doesn't match the filter expression are
     * suppressed and not sent.
     */
    private final String severityFilter;

    /** SMTP client connection settings. */
    private final SmtpClientConfig smtpClientConfig;

    /**
     * Constructs common email settings for {@link Alert} emails sent by an
     * {@link SmtpAlerter}.
     *
     * @param recipients
     *            The email recipients to use in sent mails ({@code To:}).
     * @param sender
     *            The email sender to use in sent mails ({@code From:}).
     * @param subject
     *            The email subject line.
     * @param severityFilter
     *            The regular expression used to filter {@link Alert}s.
     *            {@link Alert}s with an {@link AlertSeverity} that doesn't
     *            match the filter expression are suppressed and not sent. Set
     *            to <code>null</code> to accept any severity.
     * @param smtpClientConfig
     *            SMTP client connection settings.
     */
    public SmtpAlerterConfig(List<String> recipients, String sender, String subject, String severityFilter,
            SmtpClientConfig smtpClientConfig) {
        this.recipients = recipients;
        this.sender = sender;
        this.subject = subject;
        this.severityFilter = severityFilter;
        this.smtpClientConfig = smtpClientConfig;
        validate();
    }

    /**
     * Returns the email recipients to use in sent mails ({@code To:}).
     *
     * @return
     */
    public List<String> getRecipients() {
        return this.recipients;
    }

    /**
     * Returns the email sender to use in sent mails ({@code From:}).
     *
     * @return
     */
    public String getSender() {
        return this.sender;
    }

    /**
     * Returns the email subject line.
     *
     * @return
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Returns the regular expression used to filter {@link Alert}s.
     * {@link Alert}s with an {@link AlertSeverity} that doesn't match the
     * filter expression are suppressed and not sent.
     *
     * @return
     */
    public SeverityFilter getSeverityFilter() {
        return new SeverityFilter(Optional.ofNullable(this.severityFilter).orElse(DEFAULT_SEVERITY_FILTER));
    }

    /**
     * Returns SMTP client connection settings.
     *
     * @return
     */
    public SmtpClientConfig getSmtpClientConfig() {
        return this.smtpClientConfig;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.recipients, this.sender, this.subject, getSeverityFilter(), this.smtpClientConfig);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SmtpAlerterConfig) {
            SmtpAlerterConfig that = (SmtpAlerterConfig) obj;
            return Objects.equals(this.recipients, that.recipients) //
                    && Objects.equals(this.sender, that.sender) //
                    && Objects.equals(this.subject, that.subject) //
                    && Objects.equals(getSeverityFilter(), that.getSeverityFilter()) //
                    && Objects.equals(this.smtpClientConfig, that.smtpClientConfig);
        }
        return false;
    }

    /**
     * Performs basic validation of this object. If the object is valid, the
     * method returns. If the object is incorrectly set up an
     * {@link IllegalArgumentException} is thrown.
     *
     * @throws IllegalArgumentException
     */
    public void validate() throws IllegalArgumentException {
        checkArgument(this.subject != null, "smtpAlerter: missing subject");
        checkArgument(this.recipients != null, "smtpAlerter: missing recipients");
        for (String recipient : this.recipients) {
            checkArgument(recipient != null, "smtpAlerter: recipients: recipient cannot be null");
            checkArgument(ValidEmailAddress.isValid(recipient), "smtpAlerter: recipients: illegal email address '%s'",
                    recipient);
        }
        checkArgument(this.sender != null, "smtpAlerter: missing sender");
        checkArgument(ValidEmailAddress.isValid(this.sender), "smtpAlerter: illegal sender address '%s'", this.sender);
        getSeverityFilter();
        checkArgument(this.smtpClientConfig != null, "smtpAlerter: missing smtpClientConfig");
        try {
            this.smtpClientConfig.validate();
        } catch (Exception e) {
            throw new IllegalArgumentException("smtpAlerter: " + e.getMessage(), e);
        }
    }
}
