package com.elastisys.scale.commons.net.retryable;

/**
 * A strategy that, on every failed attempt of a {@link Retryable}, gets to
 * determine if we are to give up.
 *
 * @see Retryable
 */
public interface StopStrategy {

	/**
	 * Decides on whether or not we are to give up a {@link Retryable} after a
	 * given number of failed attempts and elapsed time. A <code>true</code>
	 * return value means we will make another attempt, while a
	 * <code>false</code> return value means the {@link Retryable} should fail.
	 *
	 * @param failedAttempts
	 *            The number of failed attempts thus far.
	 * @param elapsedTimeInMillis
	 *            The time in milliseconds since the first attempt.
	 * @return A boolean indicating if it is time to give up.
	 */
	boolean giveUp(int failedAttempts, long elapsedTimeInMillis);

}
