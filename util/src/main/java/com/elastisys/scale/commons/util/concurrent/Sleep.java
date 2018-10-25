package com.elastisys.scale.commons.util.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * Sleep utility class.
 */
public class Sleep {

    /**
     * Sleeps for the given amount or time or until the thread is uninterrupted,
     * whichever occurs first. Should the thread be interrupted, the resulting
     * {@link InterruptedException} is silently ignored and the method returns.
     *
     * @param time
     *            The duration to sleep for.
     * @param unit
     *            The unit of the duration.
     */
    public static void forTime(long time, TimeUnit unit) {
        try {
            unit.sleep(time);
        } catch (InterruptedException e) {
        }
    }
}
