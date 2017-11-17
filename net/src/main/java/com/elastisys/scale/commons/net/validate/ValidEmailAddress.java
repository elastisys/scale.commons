package com.elastisys.scale.commons.net.validate;

import java.util.function.Predicate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * {@link Predicate} that determines if an email address is valid.
 */
public class ValidEmailAddress implements Predicate<String> {

    /**
     * Validates a given email address for correctness.
     *
     * @param emailAddress
     *            An email address.
     * @return <code>true</code> if the email address is correct,
     *         <code>false</code> otherwise.
     */
    public static boolean isValid(String emailAddress) {
        return new ValidEmailAddress().test(emailAddress);
    }

    @Override
    public boolean test(String emailAddress) {
        try {
            InternetAddress address = new InternetAddress(emailAddress);
            address.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }

}
