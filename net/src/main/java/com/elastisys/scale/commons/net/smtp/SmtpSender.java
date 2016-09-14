package com.elastisys.scale.commons.net.smtp;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Callable;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Callable} that sends an email message ({@link SmtpMessage}) to a
 * certain SMTP server ({@link SmtpClientConfig}).
 * <p/>
 * The task returns <code>true</code> if the message was successfully sent.
 * <p/>
 * For details on JavaMail and security, refer to
 * <a href="https://javamail.java.net/docs/SSLNOTES.txt">these notes</a>.
 */
public class SmtpSender implements Callable<Boolean> {
    static final Logger LOG = LoggerFactory.getLogger(SmtpSender.class);

    /** The {@link Email} to send. */
    private final Email email;
    /** The SMTP send settings to use. */
    private final SmtpClientConfig sendSettings;

    /**
     * Constructs a new {@link SmtpSender} that will send a simple text-based
     * email message via a given SMTP server.
     *
     * @param message
     *            The email message to send.
     * @param sendSettings
     *            The SMTP send settings to use.
     */
    public SmtpSender(SmtpMessage message, SmtpClientConfig sendSettings) {
        this(toSimpleEmail(message), sendSettings);
    }

    /**
     * Constructs a new {@link SmtpSender} that will send a given {@link Email}.
     * The {@link Email} can be either text-based (use {@link SimpleEmail}) or
     * HTML-based (use {@link HtmlEmail}). Note that any send settings (such as
     * server host name, port, authentication, etc) set on the passed
     * {@link Email} will effectively be overridden by the send settings
     * specified in {@code sendSettings}.
     *
     * @param email
     *            The email to be sent.
     * @param sendSettings
     *            The SMTP send settings to use.
     */
    public SmtpSender(Email email, SmtpClientConfig sendSettings) {
        this.email = email;
        this.sendSettings = sendSettings;
    }

    @Override
    public Boolean call() throws Exception {
        send(this.email, this.sendSettings);
        return true;
    }

    /**
     * Sends the given {@link Email} using the given send settings.
     *
     * @param email
     *            {@link Email} to be sent.
     * @param settings
     *            SMTP send settings.
     * @throws SmtpSenderException
     */
    private void send(Email email, SmtpClientConfig settings) throws SmtpSenderException {
        checkNotNull(email, "email message cannot be null");
        if (LOG.isTraceEnabled()) {
            LOG.trace("sending email to {} with server settings {}", email.getToAddresses(), settings);
        }

        applySendSettings(email, settings);
        try {
            email.send();
        } catch (EmailException e) {
            throw new SmtpSenderException(String.format("failed to send email: %s", e.getMessage()), e);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("email sent to " + email.getToAddresses());
        }
    }

    /**
     * Applies {@link SmtpClientConfig} options to the {@link Email} being sent.
     *
     * @param email
     *            Email to be sent.
     * @param settings
     *            Send settings to apply to the email
     */
    private void applySendSettings(Email email, SmtpClientConfig settings) {
        email.setHostName(settings.getSmtpHost());
        email.setSmtpPort(settings.getSmtpPort());
        email.setSslSmtpPort(String.valueOf(settings.getSmtpPort()));
        if (settings.getAuthentication() != null) {
            email.setAuthentication(settings.getAuthentication().getUsername(),
                    settings.getAuthentication().getPassword());
        }
        if (settings.isUseSsl()) {
            // enable the use of SSL for SMTP connections. NOTE: should
            // only be used for cases when the SMTP server port only supports
            // SSL connections (typically over port 465).
            email.setSSLOnConnect(true);
        } else {
            // Support use of the STARTTLS command (see RFC 2487 and RFC 3501)
            // to switch the connection to be secured by TLS for cases where the
            // server supports both SSL and non-SSL connections. This is
            // typically the case for most modern mail servers.
            email.setStartTLSEnabled(true);
        }
        // trust all mail server host certificates
        System.setProperty("mail.smtp.ssl.trust", "*");
        email.setSocketConnectionTimeout(settings.getConnectionTimeout());
        email.setSocketTimeout(settings.getSocketTimeout());
    }

    /**
     * Converts an {@link SmtpMessage} to an {@link SimpleEmail} instance.
     * <p/>
     * Note: that the returned {@link Email} instance need to have additional
     * fields populated (such as server host name) before being sent.
     *
     * @param smtpMessage
     * @return
     * @throws SmtpSenderException
     */
    private static SimpleEmail toSimpleEmail(SmtpMessage smtpMessage) throws SmtpSenderException {
        try {
            SimpleEmail email = new SimpleEmail();
            email.setFrom(smtpMessage.getFrom().toString());
            email.setSubject(smtpMessage.getSubject());
            email.setMsg(smtpMessage.getContent());
            email.setTo(smtpMessage.getTo());
            email.setSentDate(smtpMessage.getDateSent().toDate());
            return email;
        } catch (EmailException e) {
            throw new SmtpSenderException(
                    String.format("failed to convert SmtpMessage to a SimpleEmail: %s", e.getMessage()), e);
        }
    }
}