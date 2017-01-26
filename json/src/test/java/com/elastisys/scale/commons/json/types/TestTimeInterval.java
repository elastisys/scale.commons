package com.elastisys.scale.commons.json.types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * Exercises the {@link TimeInterval} class.
 */
public class TestTimeInterval {

    @Test
    public void basicSanity() {
        // same value, different constructors
        TimeInterval interval1 = new TimeInterval(10L, "seconds");
        TimeInterval interval2 = new TimeInterval(10L, TimeUnit.SECONDS);

        assertThat(interval1.getTime(), is(10L));
        assertThat(interval1.getUnit(), is(TimeUnit.SECONDS));
        assertThat(interval1, is(interval2));
    }

    /**
     * Should be allowed to give zero as duration.
     */
    @Test
    public void withZeroDuration() {
        new TimeInterval(0L, TimeUnit.MINUTES);
    }

    /**
     * Try out all recognized time units.
     */
    @Test
    public void withDifferentUnits() {
        assertThat(new TimeInterval(10L, "nanoseconds").getUnit(), is(TimeUnit.NANOSECONDS));

        assertThat(new TimeInterval(10L, "microseconds").getUnit(), is(TimeUnit.MICROSECONDS));
        assertThat(new TimeInterval(10L, "milliseconds").getUnit(), is(TimeUnit.MILLISECONDS));
        assertThat(new TimeInterval(10L, "seconds").getUnit(), is(TimeUnit.SECONDS));
        assertThat(new TimeInterval(10L, "minutes").getUnit(), is(TimeUnit.MINUTES));
        assertThat(new TimeInterval(10L, "hours").getUnit(), is(TimeUnit.HOURS));
        assertThat(new TimeInterval(10L, "days").getUnit(), is(TimeUnit.DAYS));
    }

    @Test
    public void getSeconds() {
        assertThat(new TimeInterval(10L, "nanoseconds").getSeconds(), is(0L));

        assertThat(new TimeInterval(10L, "microseconds").getSeconds(), is(0L));
        assertThat(new TimeInterval(10L, "milliseconds").getSeconds(), is(0L));
        assertThat(new TimeInterval(10L, "seconds").getSeconds(), is(10L));
        assertThat(new TimeInterval(10L, "minutes").getSeconds(), is(10 * 60L));
        assertThat(new TimeInterval(10L, "hours").getSeconds(), is(10 * 60 * 60L));
        assertThat(new TimeInterval(10L, "days").getSeconds(), is(10 * 24 * 3600L));
    }

    @Test
    public void getMillis() {
        assertThat(new TimeInterval(10L, "nanoseconds").getMillis(), is(0L));

        assertThat(new TimeInterval(10L, "microseconds").getMillis(), is(0L));
        assertThat(new TimeInterval(10L, "milliseconds").getMillis(), is(10L));
        assertThat(new TimeInterval(10L, "seconds").getMillis(), is(10 * 1000L));
        assertThat(new TimeInterval(10L, "minutes").getMillis(), is(10 * 60 * 1000L));
        assertThat(new TimeInterval(10L, "hours").getMillis(), is(10 * 60 * 60 * 1000L));
        assertThat(new TimeInterval(10L, "days").getMillis(), is(10 * 24 * 3600 * 1000L));
    }

    @Test
    public void getNanos() {
        assertThat(new TimeInterval(10L, "nanoseconds").getNanos(), is(10L));

        assertThat(new TimeInterval(10L, "microseconds").getNanos(), is(10L * 1000));
        assertThat(new TimeInterval(10L, "milliseconds").getNanos(), is(10L * 1000 * 1000));
        long SEC_NANOS = 1000000000;
        assertThat(new TimeInterval(10L, "seconds").getNanos(), is(10 * SEC_NANOS));
        assertThat(new TimeInterval(10L, "minutes").getNanos(), is(10 * 60 * SEC_NANOS));
        assertThat(new TimeInterval(10L, "hours").getNanos(), is(10 * 60 * 60 * SEC_NANOS));
        assertThat(new TimeInterval(10L, "days").getNanos(), is(10 * 24 * 3600 * SEC_NANOS));
    }

    /**
     * Two TimeIntervals are considered equal if they represent the same
     * duration (in milliseconds).
     */
    @Test
    public void equality() {
        assertEquals(TimeInterval.seconds(0), new TimeInterval(0L * 1000 * 1000 * 1000, TimeUnit.NANOSECONDS));
        assertEquals(TimeInterval.seconds(0), new TimeInterval(0L * 1000 * 1000, TimeUnit.MICROSECONDS));
        assertEquals(TimeInterval.seconds(0), new TimeInterval(0L * 1000, TimeUnit.MILLISECONDS));
        assertEquals(TimeInterval.seconds(0), new TimeInterval(0L, TimeUnit.SECONDS));
        assertEquals(TimeInterval.seconds(0), new TimeInterval(0L, TimeUnit.MINUTES));
        assertEquals(TimeInterval.seconds(0), new TimeInterval(0L, TimeUnit.HOURS));

        assertEquals(TimeInterval.seconds(3600), new TimeInterval(3600L * 1000 * 1000 * 1000, TimeUnit.NANOSECONDS));
        assertEquals(TimeInterval.seconds(3600), new TimeInterval(3600L * 1000 * 1000, TimeUnit.MICROSECONDS));
        assertEquals(TimeInterval.seconds(3600), new TimeInterval(3600L * 1000, TimeUnit.MILLISECONDS));
        assertEquals(TimeInterval.seconds(3600), new TimeInterval(3600L, TimeUnit.SECONDS));
        assertEquals(TimeInterval.seconds(3600), new TimeInterval(60L, TimeUnit.MINUTES));
        assertEquals(TimeInterval.seconds(3600), new TimeInterval(1L, TimeUnit.HOURS));

        assertNotEquals(TimeInterval.seconds(1), new TimeInterval(1001L, TimeUnit.MILLISECONDS));
    }

    /**
     * Time unit should be case insensitive.
     */
    @Test
    public void caseInsensitive() {
        assertThat(new TimeInterval(10L, "SeConDs").getUnit(), is(TimeUnit.SECONDS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullTime() {
        new TimeInterval(null, TimeUnit.SECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullTimeUnit() {
        new TimeInterval(10L, (TimeUnit) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullUnitAsString() {
        new TimeInterval(10L, (String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithUnrecognizedUnit() {
        new TimeInterval(10L, "months");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNegativeDuration() {
        new TimeInterval(-1L, TimeUnit.SECONDS);
    }

    /**
     * Verify the behavior of the {@link TimeInterval#seconds(long)} factory
     * method.
     */
    @Test
    public void seconds() {
        assertThat(TimeInterval.seconds(10), is(new TimeInterval(10L, TimeUnit.SECONDS)));
    }
}
