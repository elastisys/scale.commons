package com.elastisys.scale.commons.net.smtp.lab;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.mail.HtmlEmail;

import com.elastisys.scale.commons.net.smtp.SmtpClientAuthentication;
import com.elastisys.scale.commons.net.smtp.SmtpClientConfig;
import com.elastisys.scale.commons.net.smtp.SmtpSender;

/**
 * Simple lab program for sending HTML-formatted emails with a remote image
 * embedded in the content.
 * <p/>
 * Some parameters need to be set as environment variables.
 * <p/>
 */
public class HtmlWithEmbeddedRemoteImageSmtpSenderLab {

    // TODO: make sure ${EMAIL_ADDRESS} is set
    private static final List<String> RECIPIENTS = Arrays.asList(System.getenv("EMAIL_ADDRESS"));
    // TODO: make sure ${EMAIL_SERVER} is set
    private static final String MAIL_SERVER = System.getenv("EMAIL_SERVER");
    private static final int MAIL_PORT = 25;
    private static final SmtpClientAuthentication AUTH = null;
    private static final boolean USE_SSL = false;

    public static void main(String[] args) throws Exception {
        HtmlEmail email = new HtmlEmail();
        for (String recipient : RECIPIENTS) {
            email.addTo(recipient);
        }
        email.setFrom("noreply@elastisys.com", "Elastisys Testing");
        email.setSubject("Test mail");
        // embed a remote image and get the content id
        String cid = email.embed(new File("src/test/resources/img/elastisys-logo.png"), "image");
        email.setHtmlMsg("<html><img height=200 src=\"cid:" + cid + "\"/><h1>Congratulations</h1>" + "\n"
                + "You are our one millionth customer!</html>");

        SmtpSender requester = new SmtpSender(email,
                new SmtpClientConfig(MAIL_SERVER, MAIL_PORT, AUTH, USE_SSL, 5000, 5000));

        System.out.println("sending email ...");
        requester.call();
        System.out.println("done.");
    }

}
