package com.elastisys.scale.commons.net.validate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.google.common.base.Predicate;

/**
 * {@link Predicate} that determines if an email address is valid.
 */
public class ValidEmailAddress implements Predicate<String> {

	/**
	 * Validates a given email address for correctness.
	 *
	 * @param emailAddress
	 * @return
	 */
	public static boolean validate(String emailAddress) {
		return new ValidEmailAddress().apply(emailAddress);
	}

	@Override
	public boolean apply(String emailAddress) {
		try {
			InternetAddress address = new InternetAddress(emailAddress);
			address.validate();
			return true;
		} catch (AddressException ex) {
			return false;
		}
	}

}
