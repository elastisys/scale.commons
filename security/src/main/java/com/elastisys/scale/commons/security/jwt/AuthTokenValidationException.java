package com.elastisys.scale.commons.security.jwt;

import java.util.Objects;

/**
 * Thrown on failure to validate an authentication token.
 * <p/>
 * The error {@code message} gives a high-level description of the problem (such
 * as 'failed to validate Authorization token') and, in some cases, the
 * {@code detail} fields may contain additional information on what went wrong
 * (for example, 'token has expired').
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

    public AuthTokenValidationException(String message, String detail, Throwable cause) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AuthTokenValidationException) {
            AuthTokenValidationException that = (AuthTokenValidationException) obj;
            return Objects.equals(getMessage(), that.getMessage()) //
                    && Objects.equals(getCause(), that.getCause()) //
                    && Objects.equals(this.detail, that.detail);

        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + this.detail;
    }
}
