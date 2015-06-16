package com.elastisys.scale.commons.net.smtp;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Callable;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Callable} that sends an email message ({@link SmtpMessage}) to a
 * certain SMTP server ({@link SmtpClientConfig}).
 * <p/>
 * The task returns <code>true</code> if the message was successfully sent.
 * <p/>
 * For details on JavaMail and security, refer to <a
 * href="https://javamail.java.net/docs/SSLNOTES.txt">these notes</a>.
 */
public class SmtpSender implements Callable<Boolean> {
	static final Logger LOG = LoggerFactory.getLogger(SmtpSender.class);

	/** The message to send. */
	private final SmtpMessage message;
	/** The SMTP server settings to use. */
	private final SmtpClientConfig serverSettings;

	/**
	 * Constructs a new {@link SmtpSender} that will send an email message to a
	 * given SMTP server.
	 *
	 * @param message
	 *            The email message to send.
	 * @param serverSettings
	 *            The SMTP server settings to use.
	 */
	public SmtpSender(SmtpMessage message, SmtpClientConfig serverSettings) {
		this.message = message;
		this.serverSettings = serverSettings;
	}

	@Override
	public Boolean call() throws Exception {
		sendMessage(this.message, this.serverSettings);
		return true;
	}

	private void sendMessage(SmtpMessage smtpMessage, SmtpClientConfig settings)
			throws EmailException {
		checkNotNull(this.message, "alert message cannot be null");
		if (LOG.isTraceEnabled()) {
			LOG.trace("sending email to {} with server settings {}",
					smtpMessage.getTo(), settings);
		}

		Email email = new SimpleEmail();
		email.setHostName(settings.getSmtpHost());
		email.setSmtpPort(settings.getSmtpPort());
		email.setFrom(smtpMessage.getFrom().toString());
		email.setSubject(smtpMessage.getSubject());
		email.setMsg(smtpMessage.getContent());
		email.setTo(smtpMessage.getTo());
		email.setSentDate(smtpMessage.getDateSent().toDate());
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
		email.send();

		if (LOG.isTraceEnabled()) {
			LOG.trace("email sent to " + smtpMessage.getTo());
		}
	}

}