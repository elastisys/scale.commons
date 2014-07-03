package com.elastisys.scale.commons.net.smtp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Exercises the {@link ClientAuthentication} class.
 * 
 * 
 * 
 */
public class TestClientAuthentication {

	@Test
	public void fieldAccess() {
		assertThat(new ClientAuthentication("user", "pass").getUserName(),
				is("user"));
		assertThat(new ClientAuthentication("user", "pass").getPassword(),
				is("pass"));
	}

	@Test
	public void equality() {
		ClientAuthentication auth1 = new ClientAuthentication("user1", "pass1");
		ClientAuthentication auth1Copy = new ClientAuthentication("user1",
				"pass1");

		ClientAuthentication sameUserWrongPassword = new ClientAuthentication(
				"user1", "pass2");
		ClientAuthentication samePasswordWrongUser = new ClientAuthentication(
				"user2", "pass1");

		assertTrue(auth1.equals(auth1));
		assertTrue(auth1.equals(auth1Copy));
		assertTrue(auth1Copy.equals(auth1));

		assertFalse(auth1.equals(sameUserWrongPassword));
		assertFalse(auth1.equals(samePasswordWrongUser));

		// can handle null
		assertFalse(auth1.equals(null));
	}

	@Test
	public void testHashCode() {
		ClientAuthentication auth1 = new ClientAuthentication("user1", "pass1");
		ClientAuthentication auth1Copy = new ClientAuthentication("user1",
				"pass1");
		ClientAuthentication sameUserWrongPassword = new ClientAuthentication(
				"user1", "pass2");
		ClientAuthentication samePasswordWrongUser = new ClientAuthentication(
				"user2", "pass1");

		assertTrue(auth1.hashCode() == auth1.hashCode());
		assertTrue(auth1.hashCode() == auth1Copy.hashCode());
		assertFalse(auth1.hashCode() == samePasswordWrongUser.hashCode());
		assertFalse(auth1.hashCode() == sameUserWrongPassword.hashCode());
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithNullUsername() {
		new ClientAuthentication(null, "pass");
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithNullPassword() {
		new ClientAuthentication("user", null);
	}

}
