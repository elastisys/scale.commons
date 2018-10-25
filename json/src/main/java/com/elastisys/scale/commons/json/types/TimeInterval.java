package com.elastisys.scale.commons.json.types;

import static com.elastisys.scale.commons.util.precond.Preconditions.checkArgument;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.elastisys.scale.commons.json.JsonUtils;

/**
 * Represents a duration of time. For example, "10 minutes".
 */
public class TimeInterval {

    /** The time value. For example, {@code 10}. */
    private final Long time;
    /**
     * Time unit. For example, "minutes". Possible values given by
     * {@link TimeUnit}.
     */
    private final String unit;

    /**
     * Creates a new {@link TimeInterval}.
     *
     * @param time
     *            The time value. For example, {@code 10}.
     * @param unit
     *            Time unit. For example, "minutes".
     */
    public TimeInterval(Long time, TimeUnit unit) {
        checkArgument(time != null, "null time");
        checkArgument(unit != null, "null time unit");
        this.time = time;
        this.unit = unit.name().toLowerCase();
        validate();
    }

    /**
     * Creates a new {@link TimeInterval}.
     *
     * @param time
     *            The time value. For example, {@code 10}.
     * @param unit
     *            Time unit. For example, "minutes". Possible values given by
     *            {@link TimeUnit}.
     */
    public TimeInterval(Long time, String unit) {
        checkArgument(time != null, "null time");
        checkArgument(unit != null, "null time unit");
        this.time = time;
        this.unit = unit.toLowerCase();
        validate();
    }

    /**
     * The time value. For example, {@code 10}.
     *
     * @return
     */
    public Long getTime() {
        return this.time;
    }

    /**
     * The time unit. For example, "minutes". Possible values given by
     * {@link TimeUnit}.
     *
     * @return
     */
    public TimeUnit getUnit() {
        return TimeUnit.valueOf(this.unit.toUpperCase());
    }

    /**
     * Returns the {@link TimeInterval} length in seconds.
     *
     * @return
     */
    public long getSeconds() {
        return TimeUnit.SECONDS.convert(this.time, getUnit());
    }

    /**
     * Returns the {@link TimeInterval} length in milliseconds.
     *
     * @return
     */
    public long getMillis() {
        return TimeUnit.MILLISECONDS.convert(this.time, getUnit());
    }

    /**
     * Returns the {@link TimeInterval} length in nanoseconds.
     *
     * @return
     */
    public long getNanos() {
        return TimeUnit.NANOSECONDS.convert(this.time, getUnit());
    }

    /**
     * Returns a {@link TimeInterval} that spans the given number of wallclock
     * seconds.
     *
     * @param seconds
     * @return
     */
    public static TimeInterval seconds(long seconds) {
        return new TimeInterval(seconds, TimeUnit.SECONDS);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.time, this.unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeInterval) {
            TimeInterval that = (TimeInterval) obj;
            // two TimeIntervals are considered equal if they represent the same
            // duration
            if (this.time != null && this.unit != null && that.time != null && that.unit != null) {
                return getNanos() == that.getNanos();
            }
            // if some fields are null, make a field-by-field comparison
            return Objects.equals(this.time, that.time) && Objects.equals(this.unit, that.unit);
        }
        return false;
    }

    @Override
    public String toString() {
        return JsonUtils.toString(JsonUtils.toJson(this));
    }

    public void validate() {
        checkArgument(this.time != null, "no time set");
        checkArgument(this.time >= 0, "time interval must be non-negative");
        // make sure the specified unit is permitted
        getUnit();
    }

}
