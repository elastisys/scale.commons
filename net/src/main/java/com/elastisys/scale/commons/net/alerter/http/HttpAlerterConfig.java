package com.elastisys.scale.commons.net.alerter.http;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.AlertSeverity;
import com.elastisys.scale.commons.net.alerter.SeverityFilter;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * {@link HttpAlerter} configuration.
 *
 * @see HttpAlerter
 */
public class HttpAlerterConfig {
	/**
	 * The default severity filter to apply to {@link Alert}s. This filter
	 * accepts any severity.
	 */
	public static final String DEFAULT_SEVERITY_FILTER = ".*";
	/**
	 * The default timeout in milliseconds until a connection is established. A
	 * timeout value of zero is interpreted as an infinite timeout. A negative
	 * value is interpreted as undefined (system default).
	 */
	public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
	/**
	 * The default socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is
	 * the timeout for waiting for data or, put differently, a maximum period
	 * inactivity between two consecutive data packets). A timeout value of zero
	 * is interpreted as an infinite timeout. A negative value is interpreted as
	 * undefined (system default).
	 */
	public static final int DEFAULT_SOCKET_TIMEOUT = 60000;
	/**
	 * The regular expression used to filter {@link Alert}s. {@link Alert}s with
	 * an {@link AlertSeverity} that doesn't match the filter expression are
	 * suppressed and not sent.
	 */
	private final String severityFilter;

	/** The destination endpoints to send {@link Alert}s to. */
	private final List<String> destinationUrls;

	/**
	 * Authentication credentials, or <code>null</code> if no authentication is
	 * to be performed.
	 */
	private final HttpAuthConfig auth;

	/**
	 * The timeout in milliseconds until a connection is established. A timeout
	 * value of zero is interpreted as an infinite timeout. A negative value is
	 * interpreted as undefined (system default).
	 */
	private final Integer connectTimeout;

	/**
	 * The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the
	 * timeout for waiting for data or, put differently, a maximum period
	 * inactivity between two consecutive data packets). A timeout value of zero
	 * is interpreted as an infinite timeout. A negative value is interpreted as
	 * undefined (system default).
	 */
	private final Integer socketTimeout;

	/**
	 * Constructs a new {@link HttpAlerterConfig} with default connection and
	 * socket timeouts.
	 *
	 * @param destinationUrls
	 *            The list of target HTTP(S) URLs to notify.
	 * @param severityFilter
	 *            The regular expression used to filter {@link Alert}s.
	 *            {@link Alert}s with an {@link AlertSeverity} that doesn't
	 *            match the filter expression are suppressed and not sent. Set
	 *            to <code>null</code> to accept any severity.
	 * @param auth
	 *            Authentication credentials. May be <code>null</code> if no
	 *            authentication is to be performed.
	 */
	public HttpAlerterConfig(List<String> destinationUrls,
			String severityFilter, HttpAuthConfig auth) {
		this(destinationUrls, severityFilter, auth, DEFAULT_CONNECTION_TIMEOUT,
				DEFAULT_SOCKET_TIMEOUT);
	}

	/**
	 * Constructs a new {@link HttpAlerterConfig}.
	 *
	 * @param destinationUrls
	 *            The list of target HTTP(S) URLs to notify.
	 * @param severityFilter
	 *            The regular expression used to filter {@link Alert}s.
	 *            {@link Alert}s with an {@link AlertSeverity} that doesn't
	 *            match the filter expression are suppressed and not sent. Set
	 *            to <code>null</code> to accept any severity.
	 * @param auth
	 *            Authentication credentials. May be <code>null</code> if no
	 *            authentication is to be performed.
	 * @param connectTimeout
	 *            The timeout in milliseconds until a connection is established.
	 *            A timeout value of zero is interpreted as an infinite timeout.
	 *            A negative value is interpreted as undefined (system default).
	 *            If <code>null</code>, {@link #DEFAULT_CONNECTION_TIMEOUT} will
	 *            be used.
	 * @param socketTimeout
	 *            The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which
	 *            is the timeout for waiting for data or, put differently, a
	 *            maximum period inactivity between two consecutive data
	 *            packets). A timeout value of zero is interpreted as an
	 *            infinite timeout. A negative value is interpreted as undefined
	 *            (system default). If <code>null</code>,
	 *            {@link #DEFAULT_SOCKET_TIMEOUT} will be used.
	 */
	public HttpAlerterConfig(List<String> destinationUrls,
			String severityFilter, HttpAuthConfig auth, Integer connectTimeout,
			Integer socketTimeout) {
		this.destinationUrls = destinationUrls;
		this.severityFilter = severityFilter;
		this.auth = auth;
		this.connectTimeout = connectTimeout;
		this.socketTimeout = socketTimeout;
		validate();
	}

	/**
	 * Returns the regular expression used to filter {@link Alert}s.
	 * {@link Alert}s with an {@link AlertSeverity} that doesn't match the
	 * filter expression are suppressed and not sent.
	 *
	 * @return
	 */
	public SeverityFilter getSeverityFilter() {
		return new SeverityFilter(Optional.fromNullable(this.severityFilter)
				.or(DEFAULT_SEVERITY_FILTER));
	}

	/**
	 * Returns the destination URL(s) that are to be alerted.
	 *
	 * @return
	 */
	public List<String> getDestinationUrls() {
		return ImmutableList.copyOf(this.destinationUrls);
	}

	/**
	 * Returns the set authentication credentials, if any. A <code>null</code>
	 * value means no authentication.
	 *
	 * @return
	 */
	public HttpAuthConfig getAuth() {
		// if unset, return an auth config with no auth credentials
		return Optional.fromNullable(this.auth).or(new HttpAuthConfig(null, null));
	}

	/**
	 * The timeout in milliseconds until a connection is established. A timeout
	 * value of zero is interpreted as an infinite timeout. A negative value is
	 * interpreted as undefined (system default).
	 *
	 * @return
	 */
	public int getConnectTimeout() {
		return Optional.fromNullable(this.connectTimeout).or(
				DEFAULT_CONNECTION_TIMEOUT);
	}

	/**
	 * The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the
	 * timeout for waiting for data or, put differently, a maximum period
	 * inactivity between two consecutive data packets). A timeout value of zero
	 * is interpreted as an infinite timeout. A negative value is interpreted as
	 * undefined (system default).
	 *
	 * @return
	 */
	public int getSocketTimeout() {
		return Optional.fromNullable(this.socketTimeout).or(
				DEFAULT_SOCKET_TIMEOUT);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.getSeverityFilter(), this.destinationUrls,
				getAuth(), this.getConnectTimeout(), this.getSocketTimeout());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HttpAlerterConfig) {
			HttpAlerterConfig that = (HttpAlerterConfig) obj;
			return Objects.equal(this.getSeverityFilter(),
					that.getSeverityFilter())
					&& Objects
							.equal(this.destinationUrls, that.destinationUrls)
					&& Objects.equal(this.getAuth(), that.getAuth())
					&& Objects.equal(this.getSeverityFilter(),
							that.getSeverityFilter())
					&& Objects.equal(this.getConnectTimeout(),
							that.getConnectTimeout())
					&& Objects.equal(this.getSocketTimeout(),
							that.getSocketTimeout());
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
		checkArgument(this.destinationUrls != null,
				"http alerter: destinationUrl cannot be null");
		for (String url : this.destinationUrls) {
			checkArgument(url != null, "http alerter: URL cannot be null");
			verifyUrl(url);
		}
		this.getAuth().validate();
		this.getSeverityFilter();
	}

	private void verifyUrl(String url) {
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(String.format(
					"illegal URL '%s'", url));
		}
	}
}
