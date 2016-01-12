package com.elastisys.scale.commons.net.smtp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.security.Security;

import javax.mail.internet.MimeMessage;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

/**
 * Utilities for starting SMTP servers for use in testing.
 */
public class SmtpTestServerUtil {

	/**
	 * Start a regular SMTP server.
	 *
	 * @param smtpPort
	 * @return
	 */
	public static GreenMail startSmtpServer(int smtpPort) {

		GreenMail insecureMailServer = new GreenMail(
				new ServerSetup(smtpPort, null, ServerSetup.PROTOCOL_SMTP));
		insecureMailServer.start();
		return insecureMailServer;

	}

	/**
	 * Starts an SSL SMTP server.
	 *
	 * @param sslSmtpPort
	 *            The SSL port to listen to.
	 * @param trustedUserName
	 *            The username that is trusted by the server.
	 * @param trustedPassword
	 *            The password of the trusted user.
	 * @return
	 */
	public static GreenMail startSslStmpServer(int sslSmtpPort,
			String trustedUserName, String trustedPassword) {
		// NOTE: to avoid failed certificate chain verifications in tests
		Security.setProperty("ssl.SocketFactory.provider",
				DummySSLSocketFactory.class.getName());

		GreenMail sslMailServer = new GreenMail(
				new ServerSetup(sslSmtpPort, null, ServerSetup.PROTOCOL_SMTPS));
		sslMailServer.setUser(trustedPassword, trustedPassword);
		sslMailServer.start();
		return sslMailServer;
	}

	/**
	 * Asserts that the given email message is an {@link Alert} with a given
	 * topic and severity.
	 *
	 * @param emailMessage
	 *            The email message.
	 * @param expectedTopic
	 *            The expected {@link Alert} topic.
	 * @param expectedSeverity
	 *            The expected {@link Alert} severity. Can be null, in which
	 *            case it is not checked.
	 */
	public static void assertAlertMail(MimeMessage emailMessage,
			Alert expectedAlert) {
		Alert notificationAlert = extractAlert(emailMessage);
		assertThat(notificationAlert, is(expectedAlert));
	}

	/**
	 * Parses the contents of an email message to an {@link Alert}.
	 *
	 * @param emailMessage
	 * @return
	 */
	public static Alert extractAlert(MimeMessage emailMessage) {
		String notificationMail = GreenMailUtil.getBody(emailMessage);
		Alert notificationAlert = JsonUtils.toObject(
				JsonUtils.parseJsonString(notificationMail), Alert.class);
		return notificationAlert;
	}
}
