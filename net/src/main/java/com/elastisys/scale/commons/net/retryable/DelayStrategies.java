package com.elastisys.scale.commons.net.retryable;

import java.util.concurrent.TimeUnit;

import com.elastisys.scale.commons.util.concurrent.Sleep;

/**
 * A collection of different {@link DelayStrategy} implementations.
 *
 * @see DelayStrategy
 * @see Retryable
 */
public class DelayStrategies {

    /**
     * A {@link DelayStrategy} that doesn't introduce any wait time between
     * attempts.
     *
     * @return
     */
    public static DelayStrategy noDelay() {
        return (failedAttempts, elapsedTimeMillis) -> {
            // introduce no delay: do nothing
        };
    }

    /**
     * A {@link DelayStrategy} that always waits a fixed time between every
     * attempt.
     *
     * @param duration
     *            The time to sleep between attempts.
     * @param unit
     *            The unit of the duration.
     * @return
     */
    public static DelayStrategy fixed(final long duration, final TimeUnit unit) {
        return (failedAttempts, elapsedTimeMillis) -> Sleep.forTime(duration, unit);
    }

    /**
     * A {@link DelayStrategy} that implements an exponential backoff where the
     * delay between attempts will grow exponentially as follows:
     *
     * <pre>
     * initialDelay * 2^0
     * initialDelay * 2^1
     * initialDelay * 2^2
     * initialDelay * 2^3
     * ...
     * </pre>
     *
     * @param initialDelay
     *            The delay after the first attempt.
     * @param unit
     *            The unit of the duration.
     * @return
     */
    public static DelayStrategy exponentialBackoff(final long initialDelay, final TimeUnit unit) {
        return (failedAttempts, elapsedTimeMillis) -> {
            long sleepTime = initialDelay * (1 << failedAttempts - 1);
            Sleep.forTime(sleepTime, unit);
        };
    }

}
