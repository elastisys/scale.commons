package com.elastisys.scale.commons.json.persistence;

/**
 * Thrown by a {@link PersistentState} instance to signal an error related to
 * reading/writing state.
 *
 * @see PersistentState
 */
public class PersistentStateException extends RuntimeException {

	public PersistentStateException() {
		super();
	}

	public PersistentStateException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PersistentStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public PersistentStateException(String message) {
		super(message);
	}

	public PersistentStateException(Throwable cause) {
		super(cause);
	}

}
