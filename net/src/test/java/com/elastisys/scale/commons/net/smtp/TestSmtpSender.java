package com.elastisys.scale.commons.net.smtp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Exercise the mail sending of the {@link SmtpSender} with a mocked JavaMail
 * implementation, which only sends email messages to an in-memory mail box.
 * 
 * 
 * 
 */
public class TestSmtpSender {

	@Before
	public void setUp() {
		// clear Mock JavaMail box
		Mailbox.clearAll();

		// freeze current time in tests
		FrozenTime.setFixed(UtcTime.parse("2014-03-12T12:00:00Z"));
	}

	@Test
	public void sendUnauthenticatedNoSsl() throws Exception {
		Mailbox mailbox = Mailbox.get("recipient@elastisys.com");
		assertTrue(mailbox.isEmpty());

		SmtpSender sender = new SmtpSender(new SmtpMessage(
				Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", "content", null),
				new SmtpClientConfig("some.smtp.host", 25, null, false));
		sender.call();

		// check mailbox after sending
		mailbox = Mailbox.get("recipient@elastisys.com");
		assertThat(mailbox.size(), is(1));
		Object expectedContent = "content";
		assertThat(mailbox.get(0).getContent(), is(expectedContent));
		assertThat(mailbox.get(0).getSubject(), is("subject"));
		assertThat(mailbox.get(0).getSentDate(), is(FrozenTime.now().toDate()));
	}

	@Test
	public void sendAuthenticatedWithSsl() throws Exception {
		Mailbox mailbox = Mailbox.get("recipient@elastisys.com");
		assertTrue(mailbox.isEmpty());

		SmtpSender sender = new SmtpSender(new SmtpMessage(
				Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", "content", null),
				new SmtpClientConfig("some.smtp.host", 25,
						new SmtpClientAuthentication("user", "pass"), true));
		sender.call();

		// check mailbox after sending
		mailbox = Mailbox.get("recipient@elastisys.com");
		assertThat(mailbox.size(), is(1));
		Object expectedContent = "content";
		assertThat(mailbox.get(0).getContent(), is(expectedContent));
		assertThat(mailbox.get(0).getSubject(), is("subject"));
		assertThat(mailbox.get(0).getSentDate(), is(FrozenTime.now().toDate()));
	}

}
