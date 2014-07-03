package com.elastisys.scale.commons.net.smtp.alerter;

/**
 * The range of supported {@link Alert} severities.
 *
 */
public enum AlertSeverity {
	/**
	 * Used to categorize {@link Alert}s with debugging information.
	 */
	DEBUG,
	/**
	 * Used to categorize {@link Alert}s with informational messages.
	 */
	INFO,
	/**
	 * Used to categorize {@link Alert}s of normal, but significant conditions.
	 */
	NOTICE,
	/**
	 * Used to categorize {@link Alert}s that may be problem indicators, such as
	 * runtime situations that are undesirable or unexpected, but not
	 * necessarily "wrong".
	 */
	WARN,
	/**
	 * Used to categorize {@link Alert}s that signal runtime errors or
	 * unexpected conditions that may require human intervention to alleviate,
	 * but from which the system can recover and continue operating.
	 */
	ERROR,
	/**
	 * Used to categorize {@link Alert}s that signal errors or unexpected
	 * conditions from which the system cannot recover.
	 */
	FATAL
}
