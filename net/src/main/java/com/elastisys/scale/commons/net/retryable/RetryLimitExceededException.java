package com.elastisys.scale.commons.net.retryable;

/**
 * Exception that can be used by a {@link RetryHandler} to include as failure
 * cause in a failure {@link Action} when the maximum number of retries have
 * been attempted.
 * 
 * @see RetryHandler
 * 
 * 
 */
public class RetryLimitExceededException extends Exception {

	/**
	 * Constructs a new {@link RetryLimitExceededException}.
	 */
	public RetryLimitExceededException() {
		super();
	}

	/**
	 * Constructs a new {@link RetryLimitExceededException}.
	 * 
	 * @param message
	 * @param cause
	 */
	public RetryLimitExceededException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@link RetryLimitExceededException}.
	 * 
	 * @param message
	 */
	public RetryLimitExceededException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link RetryLimitExceededException}.
	 * 
	 * @param cause
	 */
	public RetryLimitExceededException(Throwable cause) {
		super(cause);
	}

}
