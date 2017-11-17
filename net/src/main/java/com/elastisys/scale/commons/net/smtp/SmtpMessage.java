package com.elastisys.scale.commons.net.smtp;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.joda.time.DateTime;

import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Represents an simple, text-based, (SMTP) email message to be sent.
 *
 * @see SmtpSender
 */
public class SmtpMessage {
    /** The email recipient addresses (the {@code To:} header). */
    private final List<InternetAddress> to;
    /** The email sender address (the {@code From:} header). */
    private final InternetAddress from;
    /** The email subject line (the {@code Subject:} header). */
    private final String subject;
    /** The email message content. */
    private final String content;
    /** The date header field (the {@code Date:} header). */
    private final DateTime dateSent;

    /**
     * Constructs an {@link SmtpMessage} to be sent by a {@link SmtpSender}.
     *
     * @param recipients
     *            The email recipient addresses (the {@code To:} header).
     * @param sender
     *            The email sender address (the {@code From:} header).
     * @param subject
     *            The email subject line (the {@code Subject:} header). May be
     *            <code>null</code>.
     * @param content
     *            The email message content.
     * @param dateSent
     *            The date header field (the {@code Date:} header). Can be
     *            <code>null</code>, in which case the current time is used.
     *
     * @throws IllegalArgumentException
     *             If incorrect arguments were given.
     */
    public SmtpMessage(List<String> recipients, String sender, String subject, String content, DateTime dateSent)
            throws IllegalArgumentException {
        checkArgument(recipients != null, "recipients cannot be null");
        checkArgument(!recipients.isEmpty(), "empty list of recipients");
        checkArgument(sender != null, "missing sender address");

        try {
            this.to = new ArrayList<>();
            for (String recipient : recipients) {
                checkArgument(recipient != null, "recipient address cannot be null");
                this.to.add(new InternetAddress(recipient, true));
            }
            this.from = new InternetAddress(sender, true);
        } catch (AddressException e) {
            throw new IllegalArgumentException("illegal email address(es) given: " + e.getMessage(), e);
        }
        this.subject = subject;
        this.content = content;
        this.dateSent = Optional.ofNullable(dateSent).orElse(UtcTime.now());
        validate();
    }

    /**
     * Performs basic validation of this object. If the object is valid, the
     * method returns. If the object is incorrectly set up an
     * {@link IllegalArgumentException} is thrown.
     *
     * @throws IllegalArgumentException
     */
    public void validate() throws IllegalArgumentException {
        checkArgument(!this.to.isEmpty(), "empty list of recipients");
        checkArgument(this.from != null, "missing sender address");
        checkArgument(this.content != null, "content cannot be null");
    }

    /**
     * Returns the email recipient addresses (the {@code To:} header).
     *
     * @return the to
     */
    public List<InternetAddress> getTo() {
        return this.to;
    }

    /**
     * Returns the email sender address (the {@code From:} header).
     *
     * @return the from
     */
    public InternetAddress getFrom() {
        return this.from;
    }

    /**
     * Returns the email subject line (the {@code Subject:} header).
     *
     * @return the subject
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Returns the email message content.
     *
     * @return the content
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Returns the date header field (the {@code Date:} header).
     *
     * @return the dateSent
     */
    public DateTime getDateSent() {
        return this.dateSent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.to, this.from, this.subject, this.content, this.dateSent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SmtpMessage) {
            SmtpMessage that = (SmtpMessage) obj;
            return Objects.equals(this.to, that.to) //
                    && Objects.equals(this.from, that.from) //
                    && Objects.equals(this.subject, that.subject) //
                    && Objects.equals(this.content, that.content) //
                    && Objects.equals(this.dateSent, that.dateSent);
        }
        return super.equals(obj);
    }
}