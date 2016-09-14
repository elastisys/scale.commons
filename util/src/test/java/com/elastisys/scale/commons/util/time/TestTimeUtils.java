package com.elastisys.scale.commons.util.time;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

/**
 * Verify behavior of {@link TimeUtils} class.
 *
 *
 */
public class TestTimeUtils {

    @Test
    public void testEqual() {
        DateTime utcInstant = DateTime.parse("2013-01-01T10:00:00.000Z");
        DateTime cetInstant = DateTime.parse("2013-01-01T11:00:00.000+01:00");
        DateTime eetInstant = DateTime.parse("2013-01-01T12:00:00.000+02:00");

        assertTrue(TimeUtils.equal(null, null));
        assertTrue(TimeUtils.equal(utcInstant, utcInstant));
        assertTrue(TimeUtils.equal(cetInstant, cetInstant));
        assertTrue(TimeUtils.equal(eetInstant, eetInstant));

        assertTrue(TimeUtils.equal(utcInstant, cetInstant));
        assertTrue(TimeUtils.equal(cetInstant, eetInstant));
        assertTrue(TimeUtils.equal(utcInstant, eetInstant));

        assertFalse(TimeUtils.equal(null, utcInstant));
        assertFalse(TimeUtils.equal(utcInstant, null));
    }

    @Test
    public void testDurationAsString() {
        Duration twoDays = new Duration(2 * 24 * 3600 * 1000);
        Duration oneDayPlus = new Duration(1 * 24 * 3600 * 1000 + 60 * 55 * 1000 + 10 * 1000);
        Duration oneDay = new Duration(1 * 24 * 3600 * 1000);
        Duration hours = new Duration(23 * 3600 * 1000 + 1800 * 1000 + 1 * 1000);
        Duration hour = new Duration(1 * 3600 * 1000);
        Duration minutes = new Duration(55 * 60 * 1000 + 30 * 1000);
        Duration minute = new Duration(1 * 60 * 1000);
        Duration seconds = new Duration(55 * 1000);
        Duration second = new Duration(1 * 1000);
        Duration zero = new Duration(0);
        Duration negative = new Duration(-100);

        assertThat(TimeUtils.durationAsString(twoDays), is("48 hours"));
        assertThat(TimeUtils.durationAsString(oneDayPlus), is("24 hours, 55 minutes, 10 seconds"));
        assertThat(TimeUtils.durationAsString(oneDay), is("24 hours"));
        assertThat(TimeUtils.durationAsString(hours), is("23 hours, 30 minutes, 1 second"));
        assertThat(TimeUtils.durationAsString(hour), is("1 hour"));
        assertThat(TimeUtils.durationAsString(minutes), is("55 minutes, 30 seconds"));
        assertThat(TimeUtils.durationAsString(minute), is("1 minute"));
        assertThat(TimeUtils.durationAsString(seconds), is("55 seconds"));
        assertThat(TimeUtils.durationAsString(second), is("1 second"));
        assertThat(TimeUtils.durationAsString(zero), is("0 seconds"));
        assertThat(TimeUtils.durationAsString(negative), is("0 seconds"));
    }
}
