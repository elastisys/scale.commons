package com.elastisys.scale.commons.net.smtp.alerter;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * Common email send settings for the {@link EmailAlerter}.
 *
 * @see EmailAlerter
 */
public class SendSettings {
	/**
	 * The default severity filter to apply to {@link Alert}s. This
	 * filter accepts any severity.
	 */
	public static final String DEFAULT_SEVERITY_FILTER = ".*";

	/** The email recipients to use in sent mails ({@code To:}). */
	private final List<String> recipients;
	/** The email sender to use in sent mails ({@code From:}). */
	private final String sender;
	/** The email subject line. */
	private final String subject;
	/**
	 * The regular expression used to filter {@link Alert}s.
	 * {@link Alert}s with an {@link AlertSeverity} that doesn't match
	 * the filter expression are suppressed and not sent.
	 */
	private final String severityFilter;

	/**
	 * Constructs common email settings for {@link Alert} emails sent by
	 * an {@link EmailAlerter}.
	 *
	 * @param recipients
	 *            The email recipients to use in sent mails ({@code To:}).
	 * @param sender
	 *            The email sender to use in sent mails ({@code From:}).
	 * @param subject
	 *            The email subject line.
	 * @param severityFilter
	 *            The regular expression used to filter {@link Alert}s.
	 *            {@link Alert}s with an {@link AlertSeverity} that
	 *            doesn't match the filter expression are suppressed and not
	 *            sent. Set to <code>null</code> to accept any severity.
	 */
	public SendSettings(List<String> recipients, String sender, String subject,
			String severityFilter) {
		this.recipients = recipients;
		this.sender = sender;
		this.subject = subject;
		this.severityFilter = severityFilter;
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
	 * {@link Alert}s with an {@link AlertSeverity} that doesn't match
	 * the filter expression are suppressed and not sent.
	 *
	 * @return
	 */
	public String getSeverityFilter() {
		return Optional.fromNullable(this.severityFilter).or(
				DEFAULT_SEVERITY_FILTER);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.recipients, this.sender, this.subject,
				getSeverityFilter());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SendSettings) {
			SendSettings that = (SendSettings) obj;
			return Objects.equal(this.recipients, that.recipients)
					&& Objects.equal(this.sender, that.sender)
					&& Objects.equal(this.subject, that.subject)
					&& Objects.equal(this.getSeverityFilter(),
							that.getSeverityFilter());
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
		checkArgument(this.recipients != null, "recipients list cannot be null");
		for (String recipient : this.recipients) {
			checkArgument(recipient != null, "recipient cannot be null");
			verifyEmailAddress(recipient);
		}
		checkArgument(this.sender != null, "sender cannot be null");
		verifyEmailAddress(this.sender);
		verifySeverityFilter(getSeverityFilter());
	}

	private void verifyEmailAddress(String address) {
		try {
			new InternetAddress(address, true);
		} catch (AddressException e) {
			throw new IllegalArgumentException(String.format(
					"illegal email address '%s'", address));
		}
	}

	private void verifySeverityFilter(String severityFilter) {
		try {
			Pattern.compile(severityFilter);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"illegal severity filter expression: " + e.getMessage(), e);
		}
	}

}
