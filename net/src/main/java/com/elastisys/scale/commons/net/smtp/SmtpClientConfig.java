package com.elastisys.scale.commons.net.smtp;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * Represents SMTP client connection settings.
 *
 * @see SmtpSender
 */
public class SmtpClientConfig {
    /** Default connection timeout in ms. */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    /** Default socket I/O timeout in ms. */
    public static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    /** Default SMTP server port. */
    public static final int DEFAULT_SMTP_PORT = 25;

    /** The SMTP server host to send mails through. */
    private final String smtpHost;
    /** The SMTP server port to send mails through. Default is 25. */
    private final Integer smtpPort;
    /**
     * Optional username/password used to authenticate with the SMTP server. Set
     * to <code>null</code> to disable password authentication.
     */
    private final SmtpClientAuthentication authentication;
    /**
     * If <code>true</code>, enables the use of SSL for SMTP connections.
     * <i>NOTE: should only be used for cases when an SMTP server port only
     * supports SSL connections (typically over port 465). For cases where the
     * server port supports both SSL and non-SSL connections, set this to
     * <code>false</code>.<i/>. Default is <code>false</code>.
     */
    private final Boolean useSsl;

    /** Connection timeout in ms. */
    private final Integer connectionTimeout;
    /** Socket I/O timeout in ms. */
    private final Integer socketTimeout;

    /**
     * Constructs an {@link SmtpClientConfig} instance to be used by a
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
    public SmtpClientConfig(String smtpHost, Integer smtpPort, SmtpClientAuthentication authentication) {
        this(smtpHost, smtpPort, authentication, false, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Constructs an {@link SmtpClientConfig} instance to be used by a
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
    public SmtpClientConfig(String smtpHost, Integer smtpPort, SmtpClientAuthentication authentication,
            Boolean useSsl) {
        this(smtpHost, smtpPort, authentication, useSsl, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Constructs an {@link SmtpClientConfig} instance to be used by a
     * {@link SmtpSender}.
     *
     * @param smtpHost
     *            The SMTP server host.
     * @param smtpPort
     *            The SMTP server port. Defaults to 25.
     * @param authentication
     *            Optional security credentials, if password authentication is
     *            required. Set to <code>null</code> to disable password
     *            authentication.
     * @param useSsl
     *            If <code>true</code>, enables/forces the use of SSL for SMTP
     *            connections. <i>NOTE: should only be used for cases when an
     *            SMTP server port only supports SSL connections (typically over
     *            port 465). For cases where the server port supports both SSL
     *            and non-SSL connections, set this to <code>false</code>.<i/>.
     *            Setting <code>null</code> is equivalent to <code>false</code>.
     * @param connectionTimeout
     *            Connection timeout in ms.
     * @param socketTimeout
     *            Socket I/O timeout in ms.
     */
    public SmtpClientConfig(String smtpHost, Integer smtpPort, SmtpClientAuthentication authentication, Boolean useSsl,
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
        return Optional.fromNullable(this.smtpPort).or(DEFAULT_SMTP_PORT);
    }

    /**
     * Returns optional security credentials, if password authentication is
     * required. A <code>null</code> value means that password authentication is
     * disabled.
     *
     * @return the authentication
     */
    public SmtpClientAuthentication getAuthentication() {
        return this.authentication;
    }

    /**
     * Returns <code>true</code> if SSL connections are enabled.
     *
     * @return
     */
    public boolean isUseSsl() {
        return Optional.fromNullable(this.useSsl).or(false);
    }

    /**
     * Returns the connection timeout in ms.
     *
     * @return
     */
    public Integer getConnectionTimeout() {
        return Optional.fromNullable(this.connectionTimeout).or(DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * Returns the socket I/O timeout in ms.
     *
     * @return
     */
    public Integer getSocketTimeout() {
        return Optional.fromNullable(this.socketTimeout).or(DEFAULT_SOCKET_TIMEOUT);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.smtpHost, getSmtpPort(), this.authentication, isUseSsl(), getConnectionTimeout(),
                getSocketTimeout());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SmtpClientConfig) {
            SmtpClientConfig that = (SmtpClientConfig) obj;
            return Objects.equal(this.smtpHost, that.smtpHost) && Objects.equal(getSmtpPort(), that.getSmtpPort())
                    && Objects.equal(this.authentication, that.authentication)
                    && Objects.equal(isUseSsl(), that.isUseSsl())
                    && Objects.equal(getConnectionTimeout(), that.getConnectionTimeout())
                    && Objects.equal(getSocketTimeout(), that.getSocketTimeout());
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
        checkArgument(this.smtpHost != null, "smtpClientConfig: missing smtpHost");
        checkArgument(getSmtpPort() > 0, "smtpClientConfig: smtpPort cannot be negative");
        checkArgument(getConnectionTimeout() >= 0, "smtpClientConfig: negative connectionTimeout");
        checkArgument(getSocketTimeout() >= 0, "smtpClientConfig: negative socketTimeout");

        if (this.authentication != null) {
            try {
                this.authentication.validate();
            } catch (Exception e) {
                throw new IllegalArgumentException("smtpClientConfig: authentication: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("smtpHost", this.smtpHost).add("smtpPort", getSmtpPort())
                .add("useSsl", isUseSsl()).add("authentication", this.authentication)
                .add("connectionTimeout", getConnectionTimeout()).add("socketTimeout", getSocketTimeout()).toString();
    }
}