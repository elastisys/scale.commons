package com.elastisys.scale.commons.util.time;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * Convenience methods for working with time.
 */
public class TimeUtils {

    /**
     * Null-safe comparison that compares if two {@link DateTime} instances
     * represent the same time instant. In the case of two non-<code>null</code>
     * objects, the semantics is the same as for {@link DateTime#isEqual(long)}.
     * That is, the comparison is based solely on milliseconds, which allows
     * timestamps with different time zones to be correctly compared (which is
     * not the case with {@link DateTime#equals(Object)}).
     *
     * @param instant1
     *            First {@link DateTime} instance. May be <code>null</code>.
     * @param instant2
     *            Second {@link DateTime} instance. May be <code>null</code>.
     * @return <code>true</code> if the {@link DateTime} instances represent the
     *         same time instant.
     *
     */
    public static boolean equal(DateTime instant1, DateTime instant2) {
        return (instant1 == instant2) || ((instant1 != null) && instant1.isEqual(instant2));
    }

    /**
     * Returns a human-readable string representation of a {@link Duration},
     * which expresses the duration in hours, minutes and seconds using the
     * highest possible units.
     *
     * @param duration
     * @return A duration such as "48 hours, 10 minutes, 1 second"
     */
    public static String durationAsString(Duration duration) {
        if (duration.isShorterThan(new Duration(1))) {
            return "0 seconds";
        }

        PeriodFormatter formatter = new PeriodFormatterBuilder().printZeroNever().appendHours()
                .appendSuffix(" hour", " hours").appendSeparator(", ").printZeroNever().appendMinutes()
                .appendSuffix(" minute", " minutes").appendSeparator(", ").printZeroNever().appendSeconds()
                .appendSuffix(" second", " seconds").toFormatter();
        new Period(duration.getStandardDays());
        return formatter.print(duration.toPeriod());
    }
}
