package com.elastisys.scale.commons.net.smtp;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Exercises the {@link SmtpMessage} class.
 * 
 * 
 * 
 */
public class TestSmtpMessage {

    /** Simulated "current time" in the tests. */
    private static final DateTime TEST_TIME = UtcTime.parse("2014-03-12T15:00:00.000Z");

    @Before
    public void onSetup() {
        // freeze current time in tests
        FrozenTime.setFixed(TEST_TIME);
    }

    @Test
    public void createValidSmtpMessage() throws AddressException {
        DateTime timestamp = UtcTime.parse("2014-03-12T12:00:00.000Z");
        SmtpMessage message = new SmtpMessage(asList("recipient@elastisys.com"), "sender@elastisys.com", "subject",
                "content", timestamp);
        assertThat(message.getTo(), is(asList(email("recipient@elastisys.com"))));
        assertThat(message.getFrom(), is(email("sender@elastisys.com")));
        assertThat(message.getSubject(), is("subject"));
        assertThat(message.getContent(), is("content"));
        assertThat(message.getDateSent(), is(timestamp));

        // use default timestamp
        message = new SmtpMessage(asList("recipient@elastisys.com"), "sender@elastisys.com", "subject", "content",
                null);
        assertThat(message.getTo(), is(asList(email("recipient@elastisys.com"))));
        assertThat(message.getFrom(), is(email("sender@elastisys.com")));
        assertThat(message.getSubject(), is("subject"));
        assertThat(message.getContent(), is("content"));
        assertThat(message.getDateSent(), is(FrozenTime.now()));

        // multiple recipients
        message = new SmtpMessage(asList("recipient1@elastisys.com", "recipient2@elastisys.com"),
                "sender@elastisys.com", "subject", "content", null);
        assertThat(message.getTo(), is(asList(email("recipient1@elastisys.com"), email("recipient2@elastisys.com"))));

        // no subject
        message = new SmtpMessage(asList("recipient@elastisys.com"), "sender@elastisys.com", null, "content", null);
        assertThat(message.getSubject(), is(nullValue()));
    }

    @Test
    public void equality() {
        DateTime t1 = UtcTime.parse("2014-03-12T12:00:00.000Z");
        SmtpMessage m1 = new SmtpMessage(asList("recipient@elastisys.com"), "sender@elastisys.com", "subject",
                "content", t1);

        SmtpMessage differentRecipients = new SmtpMessage(asList("recipient2@elastisys.com"), "sender@elastisys.com",
                "subject", "content", t1);
        SmtpMessage differentSender = new SmtpMessage(asList("recipient@elastisys.com"), "sender2@elastisys.com",
                "subject", "content", t1);
        SmtpMessage differentSubject = new SmtpMessage(asList("recipient@elastisys.com"), "sender@elastisys.com",
                "subject2", "content", t1);
        SmtpMessage differentContent = new SmtpMessage(asList("recipient@elastisys.com"), "sender@elastisys.com",
                "subject2", "content2", t1);
        SmtpMessage differentTime = new SmtpMessage(asList("recipient@elastisys.com"), "sender@elastisys.com",
                "subject", "content", t1.plusSeconds(1));

        assertFalse(m1.equals(differentRecipients));
        assertFalse(m1.equals(differentSender));
        assertFalse(m1.equals(differentSubject));
        assertFalse(m1.equals(differentContent));
        assertFalse(m1.equals(differentTime));
        assertFalse(m1.equals(null));

        SmtpMessage m1Copy = new SmtpMessage(asList("recipient@elastisys.com"), "sender@elastisys.com", "subject",
                "content", t1);
        assertTrue(m1.equals(m1Copy));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullRecipientList() {
        new SmtpMessage(null, "sender@elastisys.com", "subject", "content", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithEmptyRecipientList() {
        List<String> empty = asList();
        new SmtpMessage(empty, "sender@elastisys.com", "subject", "content", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullRecipientAddress() {
        new SmtpMessage(asList("recipient@elastisys.com", null), "sender@elastisys.com", "subject", "content", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullContent() {
        new SmtpMessage(asList("recipient@elastisys.com"), "sender@elastisys.com", "subject", null, null);
    }

    private InternetAddress email(String address) throws AddressException {
        return new InternetAddress(address, true);
    }
}
