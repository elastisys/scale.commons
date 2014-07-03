package com.elastisys.scale.commons.net.smtp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Exercises the {@link SmtpServerSettings} class.
 * 
 * 
 * 
 */
public class TestSmtpServerSettings {

	@Test
	public void createValidServerSettings() {
		// no auth, no ssl
		SmtpServerSettings settings = new SmtpServerSettings("localhost", 25,
				null, false);
		settings.validate();
		assertThat(settings.getSmtpHost(), is("localhost"));
		assertThat(settings.getSmtpPort(), is(25));
		assertThat(settings.getAuthentication(), is(nullValue()));
		assertThat(settings.isUseSsl(), is(false));

		// no auth, ssl
		settings = new SmtpServerSettings("localhost", 25, null, true);
		settings.validate();
		assertThat(settings.getSmtpHost(), is("localhost"));
		assertThat(settings.getSmtpPort(), is(25));
		assertThat(settings.getAuthentication(), is(nullValue()));
		assertThat(settings.isUseSsl(), is(true));

		// auth, no ssl
		settings = new SmtpServerSettings("localhost", 25,
				new ClientAuthentication("user", "pass"), false);
		settings.validate();
		assertThat(settings.getSmtpHost(), is("localhost"));
		assertThat(settings.getSmtpPort(), is(25));
		assertThat(settings.getAuthentication(), is(new ClientAuthentication(
				"user", "pass")));
		assertThat(settings.isUseSsl(), is(false));

		// auth, ssl
		settings = new SmtpServerSettings("localhost", 25,
				new ClientAuthentication("user", "pass"), false);
		settings.validate();
		assertThat(settings.getSmtpHost(), is("localhost"));
		assertThat(settings.getSmtpPort(), is(25));
		assertThat(settings.getAuthentication(), is(new ClientAuthentication(
				"user", "pass")));
		assertThat(settings.isUseSsl(), is(false));

		// three-arg constructor
		settings = new SmtpServerSettings("localhost", 25,
				new ClientAuthentication("user", "pass"));
		settings.validate();
		assertThat(settings.getSmtpHost(), is("localhost"));
		assertThat(settings.getSmtpPort(), is(25));
		assertThat(settings.getAuthentication(), is(new ClientAuthentication(
				"user", "pass")));
		assertThat(settings.isUseSsl(), is(false));
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithNullSmtpHost() {
		new SmtpServerSettings(null, 25, new ClientAuthentication("user",
				"pass"), false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithNullSmtpPort() {
		new SmtpServerSettings("localhost", null, new ClientAuthentication(
				"user", "pass"), false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithIllegalSmtpPort() {
		new SmtpServerSettings("localhost", -1, new ClientAuthentication(
				"user", "pass"), false);
	}

}
