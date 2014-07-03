package com.elastisys.scale.commons.util.time;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

/**
 * Exercises the {@link UtcTime} class.
 * 
 * 
 * 
 */
public class TestUtcTime {

	/**
	 * Verify the {@link UtcTime#now()} method.
	 */
	@Test
	public void now() {
		// freeze the clock
		FrozenTime.setFixed(DateTime.parse("2013-01-10T12:00:00.000+01:00"));
		try {
			// now should return the current time
			assertThat(UtcTime.now(),
					is(DateTime.parse("2013-01-10T11:00:00.000Z")));
			// now should be in UTC time
			assertThat(UtcTime.now().getZone(), is(DateTimeZone.UTC));
		} finally {
			// resume clock
			FrozenTime.resumeSystemTime();
		}
	}

	/**
	 * Verify the {@link UtcTime#parse(String)} method.
	 */
	@Test
	public void parse() {
		DateTime parsedTime = UtcTime.parse("2013-01-10T12:00:00.000+03:00");
		assertThat(parsedTime.getZone(), is(DateTimeZone.UTC));
		assertThat(parsedTime, is(DateTime.parse("2013-01-10T09:00:00.000Z")));
	}

	/**
	 * Verify the
	 * {@link UtcTime#parse(String, org.joda.time.format.DateTimeFormatter)}
	 * method.
	 */
	@Test
	public void parseWithFormatter() {
		String timestamp = "22/Jan/2012:23:53:11 +0100";
		DateTimeFormatter formatter = DateTimeFormat.forPattern(
				"dd/MMM/yyyy:HH:mm:ss Z").withLocale(Locale.US);

		DateTime parsedTime = UtcTime.parse(timestamp, formatter);
		assertThat(parsedTime.getZone(), is(DateTimeZone.UTC));
		assertThat(parsedTime, is(DateTime.parse("2012-01-22T22:53:11.000Z")));
	}
}
