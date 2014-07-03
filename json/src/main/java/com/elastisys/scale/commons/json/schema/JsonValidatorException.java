package com.elastisys.scale.commons.json.schema;

/**
 * An {@link Exception} thrown by a {@link JsonValidator} to indicate a failed
 * attempt to validate a JSON document instance against a JSON schema.
 * 
 * 
 * 
 */
public class JsonValidatorException extends Exception {
	/** Serial version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@link JsonValidatorException}.
	 */
	public JsonValidatorException() {
		super();
	}

	/**
	 * Constructs a new {@link JsonValidatorException}.
	 * 
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public JsonValidatorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Constructs a new {@link JsonValidatorException}.
	 * 
	 * @param message
	 * @param cause
	 */
	public JsonValidatorException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@link JsonValidatorException}.
	 * 
	 * @param message
	 */
	public JsonValidatorException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link JsonValidatorException}.
	 * 
	 * @param cause
	 */
	public JsonValidatorException(Throwable cause) {
		super(cause);
	}

}
