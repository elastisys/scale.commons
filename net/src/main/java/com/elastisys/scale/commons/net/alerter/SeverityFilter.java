package com.elastisys.scale.commons.net.alerter;

import java.util.regex.Pattern;

import com.google.common.base.Objects;

/**
 * A {@link SeverityFilter} is used by an {@link Alerter} to suppress certain
 * {@link Alert}s whose {@link AlertSeverity} doesn't match a given severity
 * filter regular expression, such as {@code INFO|WARN|ERROR}.
 *
 * @see Alert
 * @see Alerter
 */
public class SeverityFilter {

	/**
	 * A regular expression used to filter {@link Alert}s. {@link Alert}s with
	 * an {@link AlertSeverity} that doesn't match the filter expression are
	 * suppressed and not sent.
	 */
	private final String filterExpression;

	/**
	 * Creates a new {@link SeverityFilter}. Throws an
	 * {@link IllegalArgumentException} if the given expression isn't a valid
	 * regular expression.
	 *
	 * @param filterExpression
	 *            A regular expression used to filter {@link Alert}s.
	 *            {@link Alert}s with an {@link AlertSeverity} that doesn't
	 *            match the filter expression are suppressed and not sent.
	 * @throws IllegalArgumentException
	 *             If the filter expression is illegal.
	 */
	public SeverityFilter(String filterExpression)
			throws IllegalArgumentException {
		verifySeverityFilter(filterExpression);
		this.filterExpression = filterExpression;
	}

	/**
	 * Returns the regular expression used to filter {@link Alert}s.
	 * {@link Alert}s with an {@link AlertSeverity} that doesn't match the
	 * filter expression are suppressed and not sent.
	 *
	 * @return
	 */
	public String getFilterExpression() {
		return this.filterExpression;
	}

	/**
	 * Determines if a given {@link Alert} should be suppressed, based on the
	 * set {@link #filterExpression}.
	 *
	 * @param alert
	 *            An {@link Alert}.
	 * @return <code>true</code> if the {@link Alert} should be sent,
	 *         <code>false</code> otherwise.
	 */
	public boolean shouldSuppress(Alert alert) {
		String severity = alert.getSeverity().name();
		return !Pattern.matches(this.filterExpression, severity);
	}

	private void verifySeverityFilter(String severityFilter) {
		try {
			Pattern.compile(severityFilter);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"illegal severity filter expression: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SeverityFilter) {
			SeverityFilter other = (SeverityFilter) obj;
			return Objects.equal(this.filterExpression, other.filterExpression);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.filterExpression);
	}
}
