package com.elastisys.scale.commons.net.smtp.alerter;

import static com.elastisys.scale.commons.net.smtp.alerter.SendSettings.DEFAULT_SEVERITY_FILTER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Exercises the {@link SendSettings} class.
 *
 * 
 *
 */
public class TestSendSettings {

	@Test
	public void createValidSendSettings() {
		// with default severity filter
		SendSettings settings = new SendSettings(
				Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", null);
		assertThat(settings.getRecipients(),
				is(Arrays.asList("recipient@elastisys.com")));
		assertThat(settings.getSender(), is("sender@elastisys.com"));
		assertThat(settings.getSubject(), is("subject"));
		assertThat(settings.getSeverityFilter(), is(DEFAULT_SEVERITY_FILTER));
		settings.validate();

		// specify severity filter
		settings = new SendSettings(Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", "INFO|WARN|ERROR");
		assertThat(settings.getSeverityFilter(), is("INFO|WARN|ERROR"));
		settings.validate();

		// no recipients
		List<String> noRecipients = Arrays.asList();
		settings = new SendSettings(noRecipients, "sender@elastisys.com",
				"subject", null);
		assertThat(settings.getRecipients(), is(noRecipients));
		settings.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithNullRecipientsList() {
		new SendSettings(null, "sender@elastisys.com", "subject", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithNullRecipient() {
		new SendSettings(Arrays.asList("recipient@elastisys.com", null),
				"sender@elastisys.com", "subject", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithIllegalRecipientAddress() {
		new SendSettings(Arrays.asList("recipient.elastisys.com"),
				"sender@elastisys.com", "subject", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithIllegalSenderAddress() {
		new SendSettings(Arrays.asList("recipient@elastisys.com"),
				"sender.elastisys.com", "subject", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithIllegalSeverityFilter() {
		new SendSettings(Arrays.asList("recipient@elastisys.com"),
				"sender@elastisys.com", "subject", "**");
	}

}
