package com.elastisys.scale.commons.net.retryable;

/**
 * A strategy that, after every failed {@link Retryable} attempt, gets to decide
 * how long to wait before the next attempt.
 *
 * @see Retryable
 */
public interface DelayStrategy {

    /**
     * Called to introduce a delay (that is, sleep) after a failed attempt.
     *
     * @param failedAttempts
     *            The number of failed attempts thus far.
     * @param elapsedTimeMillis
     *            The elapsed time in milliseconds since the first attempt.
     */
    void introduceDelay(int failedAttempts, long elapsedTimeMillis);
}
