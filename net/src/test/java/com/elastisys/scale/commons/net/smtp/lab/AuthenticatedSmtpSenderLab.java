package com.elastisys.scale.commons.net.smtp.lab;

import java.util.Arrays;
import java.util.List;

import com.elastisys.scale.commons.net.smtp.SmtpClientAuthentication;
import com.elastisys.scale.commons.net.smtp.SmtpClientConfig;
import com.elastisys.scale.commons.net.smtp.SmtpMessage;
import com.elastisys.scale.commons.net.smtp.SmtpSender;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Simple lab program for sending emails via a authenticated SMTP session with a
 * Gmail SMTP server.
 * <p/>
 * Some parameters need to be set as environment variables.
 */
public class AuthenticatedSmtpSenderLab {

    // TODO: make sure ${EMAIL_ADDRESS} is set
    private static final List<String> RECIPIENTS = Arrays.asList(System.getenv("EMAIL_ADDRESS"));

    private static final String MAIL_SERVER = "smtp.gmail.com";
    private static final int MAIL_PORT = 465;
    // TODO: make sure ${EMAIL_USER} is set
    // TODO: make sure ${EMAIL_PASSWORD} is set
    private static final SmtpClientAuthentication AUTH = new SmtpClientAuthentication(System.getenv("EMAIL_USER"),
            System.getenv("EMAIL_PASSWORD"));
    private static final boolean USE_SSL = true;

    public static void main(String[] args) throws Exception {
        String content = "Hello!\nTesting 1, 2, 3.";

        SmtpSender requester = new SmtpSender(
                new SmtpMessage(RECIPIENTS, "noreply@elastisys.com", "testing 1, 2, 3", content, UtcTime.now()),
                new SmtpClientConfig(MAIL_SERVER, MAIL_PORT, AUTH, USE_SSL, 10000, 10000));

        System.out.println("sending email ...");
        requester.call();
        System.out.println("done.");
    }

}
