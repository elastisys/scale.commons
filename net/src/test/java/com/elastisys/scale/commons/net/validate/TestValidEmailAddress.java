package com.elastisys.scale.commons.net.validate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.base.Predicate;

/**
 * Exercises the {@link ValidEmailAddress} {@link Predicate}.
 */
public class TestValidEmailAddress {

	@Test
	public void applyToValidEmailAddresses() {
		assertTrue(applyTo("dude@company.com"));
		assertTrue(applyTo("dude@company"));
		assertTrue(applyTo("dude.2@company-internal.com"));
		assertTrue(applyTo("some.dude-3@company.uk.org"));
	}

	@Test
	public void applyToInvalidEmailAddresses() {
		assertFalse(applyTo("dude@"));
		assertFalse(applyTo("dude@.company.com"));
		assertFalse(applyTo("dude|company.com"));
		assertFalse(applyTo("http://dude.company.com"));
	}

	private boolean applyTo(String emailAddress) {
		// make sure that same result is obtained no matter if static function
		// or object is called on
		boolean functionResult = ValidEmailAddress.isValid(emailAddress);
		boolean objectResult = new ValidEmailAddress().apply(emailAddress);
		assertEquals("calls on function and object gave different results!",
				functionResult, objectResult);
		return objectResult;
	}
}
