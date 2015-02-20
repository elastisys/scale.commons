package com.elastisys.scale.commons.net.retryable;

/**
 * Thrown by a {@link Retryable} that have decided to give up.
 *
 * @see Retryable
 */
public class GaveUpException extends Exception {
	private static final long serialVersionUID = 1L;

	public GaveUpException() {
		super();
	}

	public GaveUpException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GaveUpException(String message, Throwable cause) {
		super(message, cause);
	}

	public GaveUpException(String message) {
		super(message);
	}

	public GaveUpException(Throwable cause) {
		super(cause);
	}

}
