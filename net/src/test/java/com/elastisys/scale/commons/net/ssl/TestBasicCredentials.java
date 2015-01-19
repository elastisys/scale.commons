package com.elastisys.scale.commons.net.ssl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests that exercise the {@link BasicCredentials} class.
 */
public class TestBasicCredentials {

	/**
	 * Check basic sanity of accessor methods, equals comparison and hashCode.
	 */
	@Test
	public void testBasicSanity() {
		BasicCredentials credentials = new BasicCredentials("johndoe", "secret");
		assertThat(credentials.getUsername(), is("johndoe"));
		assertThat(credentials.getPassword(), is("secret"));
		credentials.validate();

		// comparison of equivalent credentials
		BasicCredentials copy = new BasicCredentials("johndoe", "secret");
		assertTrue(credentials.equals(copy));
		assertTrue(credentials.hashCode() == copy.hashCode());

		// comparison of non-equivalent credentials
		BasicCredentials other = new BasicCredentials("johnnydoe", "secret2");
		assertFalse(credentials.equals(other));
		assertFalse(credentials.hashCode() == other.hashCode());
	}

	@Test(expected = IllegalArgumentException.class)
	public void withMissingUsername() {
		new BasicCredentials(null, "secret");
	}

	@Test(expected = IllegalArgumentException.class)
	public void withMissingPassword() {
		new BasicCredentials("johndoe", null);
	}

}
