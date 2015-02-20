package com.elastisys.scale.commons.net.retryable;

import java.util.concurrent.TimeUnit;

/**
 * A collection of different {@link StopStrategy} implementations.
 *
 * @see StopStrategy
 * @see Retryable
 */
public class StopStrategies {

	/**
	 * A {@link StopStrategy} that will never give up, but will retry forever
	 * until successful.
	 *
	 * @return
	 */
	public static StopStrategy never() {
		return new StopStrategy() {
			@Override
			public boolean giveUp(int failedAttempts, long elapsedTimeInMillis) {
				return false;
			}
		};
	}

	/**
	 * A {@link StopStrategy} that will give up after having retried for a
	 * certain time.
	 *
	 * @param maxElapsedTime
	 *            The maximum elapsed time before giving up.
	 * @param unit
	 *            The unit of the duration.
	 * @return
	 */
	public static StopStrategy afterTime(final int maxElapsedTime,
			final TimeUnit unit) {
		return new StopStrategy() {
			@Override
			public boolean giveUp(int failedAttempts, long elapsedTimeInMillis) {
				long maxInMillis = TimeUnit.MILLISECONDS.convert(
						maxElapsedTime, unit);
				return elapsedTimeInMillis > maxInMillis;
			}
		};
	}

	/**
	 * A {@link StopStrategy} that will give up after a certain number of
	 * attempts.
	 *
	 * @param maxAttempts
	 *            The maximum number of attempts to carry out before giving up.
	 * @return
	 */
	public static StopStrategy afterAttempts(final int maxAttempts) {
		return new StopStrategy() {
			@Override
			public boolean giveUp(int failedAttempts, long elapsedTimeInMillis) {
				return failedAttempts >= maxAttempts;
			}
		};
	}
}
