package com.elastisys.scale.commons.util.time;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
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

    /**
     * For {@link TimeUtils#splitInterval(Interval, Duration)}, a zero-length
     * interval should not be possible to split further.
     */
    @Test
    public void splitZeroInterval() {
        Interval interval = new Interval(0, 0);
        List<Interval> subIntervals = TimeUtils.splitInterval(interval, Duration.standardSeconds(1));
        assertThat(subIntervals.size(), is(1));
        assertThat(subIntervals.get(0), is(interval));
    }

    /**
     * When the maxDuration is equal to the interval length, the original
     * interval should be returned.
     */
    @Test
    public void splitWhenIntervalIsSameLengthAsMaxDuration() {
        Interval interval = new Interval(0, 1);
        List<Interval> subIntervals = TimeUtils.splitInterval(interval, Duration.millis(1));
        assertThat(subIntervals.size(), is(1));
        assertThat(subIntervals, is(Arrays.asList(new Interval(0, 1))));
    }

    /**
     * When maxDuration is longer than the interval length, the original
     * interval is to be returned.
     */
    @Test
    public void splitWhenIntervalIsShorterThanMaxDuration() {
        Interval interval = new Interval(0, 1);
        List<Interval> subIntervals = TimeUtils.splitInterval(interval, Duration.millis(2));
        assertThat(subIntervals.size(), is(1));
        assertThat(subIntervals, is(Arrays.asList(new Interval(0, 1))));
    }

    /**
     * Make a split that splits the interval into evenly sized chunks.
     */
    @Test
    public void splitWhenAllSubIntervalsAreSameLength() {
        Interval interval = new Interval(0, 9);
        List<Interval> subIntervals = TimeUtils.splitInterval(interval, Duration.millis(3));
        assertThat(subIntervals, is(Arrays.asList(new Interval(0, 3), new Interval(3, 6), new Interval(6, 9))));
    }

    /**
     * In case of an uneven split, the last chunk gets truncated.
     */
    @Test
    public void splitWhenSplitIsUneven() {
        Interval interval = new Interval(0, 10);
        List<Interval> subIntervals = TimeUtils.splitInterval(interval, Duration.millis(4));
        assertThat(subIntervals, is(Arrays.asList(new Interval(0, 4), new Interval(4, 8), new Interval(8, 10))));
    }

    /**
     * For {@link TimeUtils#splitInterval(Interval, Duration)}, one must specify
     * a non-null {@link Interval}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void splitIntervalWithNullInterval() {
        TimeUtils.splitInterval(null, Duration.standardDays(1));
    }

    /**
     * For {@link TimeUtils#splitInterval(Interval, Duration)}, one must specify
     * a non-null {@link Duration}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void splitIntervalWithNullDuration() {
        TimeUtils.splitInterval(new Interval(0, 1), null);
    }

    /**
     * For {@link TimeUtils#splitInterval(Interval, Duration)}, one must specify
     * a positive {@link Duration}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void splitIntervalWithZeroDuration() {
        TimeUtils.splitInterval(new Interval(0, 1), Duration.standardSeconds(0));
    }
}
