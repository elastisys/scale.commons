package com.elastisys.scale.commons.net.retryable;

/**
 * Thrown by a {@link Retryable} that have decided to give up.
 *
 * @see Retryable
 */
public class GaveUpException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * The total number of attempts that were made prior before giving up.
	 */
	private final int attempts;
	/** The elapsed time in milliseconds since the first attempt. */
	private final long elapsedTimeMillis;

	public GaveUpException(int attempts, long elapsedTimeMillis, String message,
			Throwable cause) {
		super(message, cause);
		this.attempts = attempts;
		this.elapsedTimeMillis = elapsedTimeMillis;
	}

	public GaveUpException(int attempts, long elapsedTimeMillis,
			String message) {
		super(message);
		this.attempts = attempts;
		this.elapsedTimeMillis = elapsedTimeMillis;
	}

	/**
	 * Returns the total number of attempts that were made prior before giving
	 * up.
	 *
	 * @return
	 */
	public int getAttempts() {
		return this.attempts;
	}

	/**
	 * Returns the elapsed time in milliseconds since the first attempt.
	 *
	 * @return
	 */
	public long getElapsedTimeMillis() {
		return this.elapsedTimeMillis;
	}
}
