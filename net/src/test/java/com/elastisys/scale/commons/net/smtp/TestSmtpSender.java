package com.elastisys.scale.commons.net.smtp;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Exercise the mail sending of the {@link SmtpSender} with a mocked JavaMail
 * implementation, which only sends email messages to an in-memory mail box.
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

		SmtpSender sender = new SmtpSender(
				new SmtpMessage(Arrays.asList("recipient@elastisys.com"),
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

		SmtpSender sender = new SmtpSender(
				new SmtpMessage(Arrays.asList("recipient@elastisys.com"),
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

	/**
	 * Test sending an {@link SimpleEmail} rather than a {@link SmtpMessage}.
	 */
	@Test
	public void sendSimpleEmail() throws Exception {
		Mailbox mailbox = Mailbox.get("recipient@elastisys.com");
		assertTrue(mailbox.isEmpty());

		SimpleEmail email = new SimpleEmail();
		email.setFrom("sender@elastisys.com");
		email.setSubject("subject");
		email.setMsg("content");
		email.addTo("recipient@elastisys.com");
		email.setSentDate(UtcTime.now().toDate());
		SmtpSender sender = new SmtpSender(email,
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

	/**
	 * Test sending an {@link HtmlEmail}.
	 */
	@Test
	public void sendHtmlEmail() throws Exception {
		Mailbox mailbox = Mailbox.get("recipient@elastisys.com");
		assertTrue(mailbox.isEmpty());

		HtmlEmail email = new HtmlEmail();
		email.setFrom("sender@elastisys.com");
		email.setSubject("subject");
		email.setHtmlMsg("<h1>Hi</h1>\nHope you're doing well.");
		email.setTextMsg("Hi! Your mail client doesn't seem to support HTML.");
		email.addTo("recipient@elastisys.com");
		email.setSentDate(UtcTime.now().toDate());
		SmtpSender sender = new SmtpSender(email,
				new SmtpClientConfig("some.smtp.host", 25,
						new SmtpClientAuthentication("user", "pass"), true));
		sender.call();

		// check mailbox after sending
		mailbox = Mailbox.get("recipient@elastisys.com");
		assertThat(mailbox.size(), is(1));
		Message message = mailbox.get(0);
		Object content = message.getContent();
		assertThat(content, instanceOf(MimeMultipart.class));
		MimeMultipart mimeContent = (MimeMultipart) content;
		Object firstPart = mimeContent.getBodyPart(0).getContent();
		Object secondPart = mimeContent.getBodyPart(1).getContent();
		assertThat(firstPart,
				is("Hi! Your mail client doesn't seem to support HTML."));
		assertThat(secondPart, is("<h1>Hi</h1>\nHope you're doing well."));
	}
}
