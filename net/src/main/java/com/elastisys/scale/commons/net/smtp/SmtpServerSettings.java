package com.elastisys.scale.commons.net.smtp;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Represents SMTP server connection settings.
 * 
 * @see SmtpSender
 * 
 */
public class SmtpServerSettings {
	/** Default connection timeout in ms. */
	public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
	/** Default socket I/O timeout in ms. */
	public static final int DEFAULT_SOCKET_TIMEOUT = 30000;

	/** The SMTP server host to send mails through. */
	private final String smtpHost;
	/** The SMTP server port to send mails through. */
	private final Integer smtpPort;
	/**
	 * Optional username/password used to authenticate with the SMTP server. Set
	 * to <code>null</code> to disable password authentication.
	 */
	private final ClientAuthentication authentication;
	/**
	 * If <code>true</code>, enables the use of SSL for SMTP connections.
	 * <i>NOTE: should only be used for cases when an SMTP server port only
	 * supports SSL connections (typically over port 465). For cases where the
	 * server port supports both SSL and non-SSL connections, set this to
	 * <code>false</code>.<i/>
	 */
	private final Boolean useSsl;

	/** Connection timeout in ms. */
	private final Integer connectionTimeout;
	/** Socket I/O timeout in ms. */
	private final Integer socketTimeout;

	/**
	 * Constructs an {@link SmtpServerSettings} instance to be used by a
	 * {@link SmtpSender}. The created server settings works on servers that
	 * support non-SSL connections or SSL connections (via the STARTTLS
	 * command). If STARTTLS is supported, a TLS connection will be created.
	 * 
	 * @param smtpHost
	 *            The SMTP server host.
	 * @param smtpPort
	 *            The SMTP server port.
	 * @param authentication
	 *            Optional security credentials, if password authentication is
	 *            required. Set to <code>null</code> to disable password
	 *            authentication.
	 */
	public SmtpServerSettings(String smtpHost, Integer smtpPort,
			ClientAuthentication authentication) {
		this(smtpHost, smtpPort, authentication, false,
				DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
	}

	/**
	 * Constructs an {@link SmtpServerSettings} instance to be used by a
	 * {@link SmtpSender}.
	 * 
	 * @param smtpHost
	 *            The SMTP server host.
	 * @param smtpPort
	 *            The SMTP server port.
	 * @param authentication
	 *            Optional security credentials, if password authentication is
	 *            required. Set to <code>null</code> to disable password
	 *            authentication.
	 * @param useSsl
	 *            If <code>true</code>, enables/forces the use of SSL for SMTP
	 *            connections. <i>NOTE: should only be used for cases when an
	 *            SMTP server port only supports SSL connections (typically over
	 *            port 465). For cases where the server port supports both SSL
	 *            and non-SSL connections, set this to <code>false</code>.<i/>
	 */
	public SmtpServerSettings(String smtpHost, Integer smtpPort,
			ClientAuthentication authentication, Boolean useSsl) {
		this(smtpHost, smtpPort, authentication, useSsl,
				DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
	}

	/**
	 * Constructs an {@link SmtpServerSettings} instance to be used by a
	 * {@link SmtpSender}.
	 * 
	 * @param smtpHost
	 *            The SMTP server host.
	 * @param smtpPort
	 *            The SMTP server port.
	 * @param authentication
	 *            Optional security credentials, if password authentication is
	 *            required. Set to <code>null</code> to disable password
	 *            authentication.
	 * @param useSsl
	 *            If <code>true</code>, enables/forces the use of SSL for SMTP
	 *            connections. <i>NOTE: should only be used for cases when an
	 *            SMTP server port only supports SSL connections (typically over
	 *            port 465). For cases where the server port supports both SSL
	 *            and non-SSL connections, set this to <code>false</code>.<i/>
	 * @param connectionTimeout
	 *            Connection timeout in ms.
	 * @param socketTimeout
	 *            Socket I/O timeout in ms.
	 */
	public SmtpServerSettings(String smtpHost, Integer smtpPort,
			ClientAuthentication authentication, Boolean useSsl,
			int connectionTimeout, int socketTimeout) {
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.authentication = authentication;
		this.useSsl = useSsl;
		this.connectionTimeout = connectionTimeout;
		this.socketTimeout = socketTimeout;

		validate();
	}

	/**
	 * Returns the SMTP server host to send mails through.
	 * 
	 * @return the smtpHost
	 */
	public String getSmtpHost() {
		return this.smtpHost;
	}

	/**
	 * Returns the SMTP server port to send mails through.
	 * 
	 * @return the smtpPort
	 */
	public Integer getSmtpPort() {
		return this.smtpPort;
	}

	/**
	 * Returns optional security credentials, if password authentication is
	 * required. A <code>null</code> value means that password authentication is
	 * disabled.
	 * 
	 * @return the authentication
	 */
	public ClientAuthentication getAuthentication() {
		return this.authentication;
	}

	/**
	 * Returns <code>true</code> if SSL connections are enabled.
	 * 
	 * @return
	 */
	public boolean isUseSsl() {
		return this.useSsl;
	}

	/**
	 * Returns the connection timeout in ms.
	 * 
	 * @return
	 */
	public Integer getConnectionTimeout() {
		return this.connectionTimeout;
	}

	/**
	 * Returns the socket I/O timeout in ms.
	 * 
	 * @return
	 */
	public Integer getSocketTimeout() {
		return this.socketTimeout;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.smtpHost, this.smtpPort,
				this.authentication, this.useSsl, this.connectionTimeout,
				this.socketTimeout);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SmtpServerSettings) {
			SmtpServerSettings that = (SmtpServerSettings) obj;
			return Objects.equal(this.smtpHost, that.smtpHost)
					&& Objects.equal(this.smtpPort, that.smtpPort)
					&& Objects.equal(this.authentication, that.authentication)
					&& Objects.equal(this.useSsl, that.useSsl)
					&& Objects.equal(this.connectionTimeout,
							that.connectionTimeout)
					&& Objects.equal(this.socketTimeout, that.socketTimeout);
		}
		return super.equals(obj);
	}

	/**
	 * Performs basic validation of this object. If the object is valid, the
	 * method returns. If the object is incorrectly set up an
	 * {@link IllegalArgumentException} is thrown.
	 * 
	 * @throws IllegalArgumentException
	 */
	public void validate() throws IllegalArgumentException {
		Preconditions.checkArgument(this.smtpHost != null, "missing smtpHost");
		Preconditions.checkArgument(this.smtpPort != null, "missing smtpPort");
		Preconditions.checkArgument(this.smtpPort > 0, "missing smtpPort");
		Preconditions.checkArgument(this.useSsl != null, "missing useSsl");
		Preconditions.checkArgument(this.connectionTimeout >= 0,
				"negative connectionTimeout");
		Preconditions.checkArgument(this.socketTimeout >= 0,
				"negative socketTimeout");
		if (this.authentication != null) {
			this.authentication.validate();
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("smtpHost", this.smtpHost)
				.add("smtpPort", this.smtpPort).add("useSsl", this.useSsl)
				.add("authentication", this.authentication)
				.add("connectionTimeout", this.connectionTimeout)
				.add("socketTimeout", this.socketTimeout).toString();
	}
}