package com.elastisys.scale.commons.security.jwt;

/**
 * Thrown by an {@link AuthTokenHeaderValidator} to indicate that an
 * {@code Authorization} header authentication token could not be validated.
 * <p/>
 * The error {@code message} gives a high-level description of the problem (such
 * as 'failed to validate Authorization token') and, in some cases, the detail
 * fields may contain additional information on what went wrong (for example,
 * 'token has expired').
 *
 * @see AuthTokenHeaderValidator
 */
public class AuthTokenValidationException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Any error details that further qualify the root of the error. For
	 * example, 'token has expired'. This field may be <code>null</code>.
	 */
	private final String detail;

	public AuthTokenValidationException(String message, String detail) {
		super(message);
		this.detail = detail;
	}

	public AuthTokenValidationException(String message, String detail,
			Throwable cause) {
		super(message, cause);
		this.detail = detail;
	}

	/**
	 * Any error details that further qualify the root of the error. For
	 * example, 'token has expired'. This field may be <code>null</code>
	 *
	 * @return
	 */
	public String getDetail() {
		return this.detail;
	}
}
