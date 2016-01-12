package com.elastisys.scale.commons.net.smtp.lab;

import java.util.Arrays;
import java.util.List;

import com.elastisys.scale.commons.net.smtp.SmtpClientAuthentication;
import com.elastisys.scale.commons.net.smtp.SmtpClientConfig;
import com.elastisys.scale.commons.net.smtp.SmtpMessage;
import com.elastisys.scale.commons.net.smtp.SmtpSender;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Simple lab program for sending emails via an unauthenticated SMTP session
 * with a SMTP server not requiring client authentication.
 * <p/>
 * Some parameters need to be set as environment variables.
 * <p/>
 */
public class UnauthenticatedSmtpSenderLab {

	// TODO: make sure ${EMAIL_ADDRESS} is set
	private static final List<String> RECIPIENTS = Arrays
			.asList(System.getenv("EMAIL_ADDRESS"));
	// TODO: make sure ${EMAIL_SERVER} is set
	private static final String MAIL_SERVER = System.getenv("EMAIL_SERVER");
	private static final int MAIL_PORT = 25;
	private static final SmtpClientAuthentication AUTH = null;
	private static final boolean USE_SSL = false;

	public static void main(String[] args) throws Exception {
		String content = "Hello!\nTesting 1, 2, 3.";

		SmtpSender requester = new SmtpSender(
				new SmtpMessage(RECIPIENTS, "noreply@elastisys.com",
						"testing 1, 2, 3", content, UtcTime.now()),
				new SmtpClientConfig(MAIL_SERVER, MAIL_PORT, AUTH, USE_SSL,
						5000, 5000));

		System.out.println("sending email ...");
		requester.call();
		System.out.println("done.");
	}

}
