package com.elastisys.scale.commons.util.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 * Utility class for working with UTC (Coordinated Universal Time) time.
 * <p/>
 * Rather than working directly with the {@link DateTime} class to get the
 * current time or parse a time expression, which typically renders a timestamp
 * in the local timezone, this class can be used to work directly with UTC time.
 * 
 * 
 */
public class UtcTime {
	/**
	 * Returns the current time as an UTC timestamp.
	 * 
	 * @return The current UTC time.
	 */
	public static DateTime now() {
		return DateTime.now(DateTimeZone.UTC);
	}

	/**
	 * Parses a ISO8601-compliant time expression and returns the corresponding
	 * UTC time as a {@link DateTime} object.
	 * 
	 * @param timeString
	 *            A ISO8601-formatted time, such as
	 *            {@code2014-03-05T07:09:10.724+01:00}.
	 * @return The UTC time that corresponds to the timeString.
	 */
	public static DateTime parse(String timeString) {
		return DateTime.parse(timeString).withZone(DateTimeZone.UTC);
	}

	/**
	 * Parses a time expression with a given {@link DateTimeFormatter} and
	 * returns the corresponding UTC time as a {@link DateTime} object.
	 * 
	 * @param timeString
	 *            A time expression that can be understood by the
	 *            {@link DateTimeFormatter}.
	 * @param formatter
	 *            The {@link DateTimeFormatter} that will parse the time
	 *            expression.
	 * @return The UTC time that corresponds to the timeString.
	 */
	public static DateTime parse(String timeString, DateTimeFormatter formatter) {
		return DateTime.parse(timeString, formatter).withZone(DateTimeZone.UTC);
	}

}
