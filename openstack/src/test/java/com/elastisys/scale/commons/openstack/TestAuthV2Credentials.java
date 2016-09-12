package com.elastisys.scale.commons.openstack;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Exercises the {@link AuthV2Credentials} class.
 */
public class TestAuthV2Credentials {

	@Test
	public void creation() {
		AuthV2Credentials credentials = new AuthV2Credentials("tenant", "user", "pass");
		assertThat(credentials.getTenantName(), is("tenant"));
		assertThat(credentials.getUserName(), is("user"));
		assertThat(credentials.getPassword(), is("pass"));
		credentials.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingTenantName() {
		new AuthV2Credentials(null, "user", "pass");
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingUserName() {
		new AuthV2Credentials("tenant", null, "pass");
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingPassword() {
		new AuthV2Credentials("tenant", "user", null);
	}
}
