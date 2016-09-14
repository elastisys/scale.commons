package com.elastisys.scale.commons.net.smtp;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.elastisys.scale.commons.net.host.HostUtils;
import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

/**
 * Exercise the mail sending of the {@link SmtpSender} with an embedded SMTP
 * mail server.
 */
public class TestSmtpSender {

    /** Trusted user on SSL server. */
    private static final String USERNAME = "user";
    /** Trusted user's password on SSL server. */
    private static final String PASSWORD = "password";

    /** Port where fake SMTP server is listening */
    private static int SMTP_PORT;
    /** Port where fake SMTP server is listening for SSL */
    private static int SMTP_SSL_PORT;

    static {
        List<Integer> freePorts = HostUtils.findFreePorts(2);
        SMTP_PORT = freePorts.get(0);
        SMTP_SSL_PORT = freePorts.get(1);
    }

    /** Fake email SMTP server without SSL. */
    private GreenMail insecureMailServer;

    /** Fake email SMTP server with SSL. */
    private GreenMail sslMailServer;

    @Before
    public void setUp() {
        startServers();

        // freeze current time in tests
        FrozenTime.setFixed(UtcTime.parse("2014-03-12T12:00:00Z"));
    }

    /**
     * Start the mail servers used in the test.
     */
    private void startServers() {
        this.insecureMailServer = SmtpTestServerUtil.startSmtpServer(SMTP_PORT);

        this.sslMailServer = SmtpTestServerUtil.startSslStmpServer(SMTP_SSL_PORT, USERNAME, PASSWORD);
    }

    @After
    public void onTearDown() {
        stopServers();
    }

    private void stopServers() {
        if (this.insecureMailServer != null) {
            this.insecureMailServer.stop();
        }
        if (this.sslMailServer != null) {
            this.sslMailServer.stop();
        }
    }

    @Test
    public void sendUnauthenticatedNoSsl() throws Exception {
        assertThat(this.insecureMailServer.getReceivedMessages().length, is(0));
        assertThat(this.sslMailServer.getReceivedMessages().length, is(0));

        SmtpSender sender = new SmtpSender(new SmtpMessage(Arrays.asList("recipient@elastisys.com"),
                "sender@elastisys.com", "subject", "content", null),
                new SmtpClientConfig("localhost", SMTP_PORT, null, false));
        sender.call();

        // check mailbox after sending
        MimeMessage[] receivedMessages = this.insecureMailServer.getReceivedMessages();
        assertThat(receivedMessages.length, is(1));
        assertThat(receivedMessages[0].getSubject(), is("subject"));
        assertThat(receivedMessages[0].getSentDate(), is(FrozenTime.now().toDate()));
        Object expectedContent = "content";
        assertThat(GreenMailUtil.getBody(receivedMessages[0]).trim(), is(expectedContent));

        // nothing sent over SSL
        assertThat(this.sslMailServer.getReceivedMessages().length, is(0));
    }

    @Test
    public void sendAuthenticatedWithSsl() throws Exception {
        assertThat(this.insecureMailServer.getReceivedMessages().length, is(0));
        assertThat(this.sslMailServer.getReceivedMessages().length, is(0));

        SmtpSender sender = new SmtpSender(
                new SmtpMessage(Arrays.asList("recipient@elastisys.com"), "sender@elastisys.com", "subject", "content",
                        null),
                new SmtpClientConfig("localhost", SMTP_SSL_PORT, new SmtpClientAuthentication(USERNAME, PASSWORD),
                        true));
        sender.call();

        // check mailbox after sending
        MimeMessage[] receivedMessages = this.sslMailServer.getReceivedMessages();
        assertThat(receivedMessages.length, is(1));
        assertThat(receivedMessages[0].getSubject(), is("subject"));
        assertThat(receivedMessages[0].getSentDate(), is(FrozenTime.now().toDate()));
        Object expectedContent = "content";
        assertThat(GreenMailUtil.getBody(receivedMessages[0]).trim(), is(expectedContent));

        // nothing sent over regular SMTP
        assertThat(this.insecureMailServer.getReceivedMessages().length, is(0));
    }

    /**
     * Test sending an {@link SimpleEmail} rather than a {@link SmtpMessage}.
     */
    @Test
    public void sendSimpleEmail() throws Exception {
        assertThat(this.sslMailServer.getReceivedMessages().length, is(0));

        SimpleEmail email = new SimpleEmail();
        email.setFrom("sender@elastisys.com");
        email.setSubject("subject");
        email.setMsg("content");
        email.addTo("recipient@elastisys.com");
        email.setSentDate(UtcTime.now().toDate());
        SmtpSender sender = new SmtpSender(email, new SmtpClientConfig("localhost", SMTP_SSL_PORT,
                new SmtpClientAuthentication(USERNAME, PASSWORD), true));
        sender.call();

        // check mailbox after sending
        MimeMessage[] receivedMessages = this.sslMailServer.getReceivedMessages();
        assertThat(receivedMessages.length, is(1));
        assertThat(receivedMessages[0].getSubject(), is("subject"));
        assertThat(receivedMessages[0].getSentDate(), is(FrozenTime.now().toDate()));
        Object expectedContent = "content";
        assertThat(GreenMailUtil.getBody(receivedMessages[0]).trim(), is(expectedContent));
    }

    /**
     * Test sending an {@link HtmlEmail}.
     */
    @Test
    public void sendHtmlEmail() throws Exception {
        assertThat(this.sslMailServer.getReceivedMessages().length, is(0));

        HtmlEmail email = new HtmlEmail();
        email.setFrom("sender@elastisys.com");
        email.setSubject("subject");
        email.setHtmlMsg("<h1>Hi</h1>\nHope you're doing well.");
        email.setTextMsg("Hi! Your mail client doesn't seem to support HTML.");
        email.addTo("recipient@elastisys.com");
        email.setSentDate(UtcTime.now().toDate());
        SmtpSender sender = new SmtpSender(email, new SmtpClientConfig("localhost", SMTP_SSL_PORT,
                new SmtpClientAuthentication(USERNAME, PASSWORD), true));
        sender.call();

        // check mailbox after sending
        MimeMessage[] receivedMessages = this.sslMailServer.getReceivedMessages();
        assertThat(receivedMessages.length, is(1));
        assertThat(receivedMessages[0].getSubject(), is("subject"));
        assertThat(receivedMessages[0].getSentDate(), is(FrozenTime.now().toDate()));
        MimeMessage message = receivedMessages[0];
        Object content = message.getContent();
        assertThat(content, instanceOf(MimeMultipart.class));
        MimeMultipart mimeContent = (MimeMultipart) content;
        Object firstPart = mimeContent.getBodyPart(0).getContent();
        Object secondPart = mimeContent.getBodyPart(1).getContent();
        assertThat(firstPart, is("Hi! Your mail client doesn't seem to support HTML."));
        assertThat(secondPart, is("<h1>Hi</h1>\r\nHope you're doing well."));
    }
}
