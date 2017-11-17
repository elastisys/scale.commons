package com.elastisys.scale.commons.json.types;

import java.util.Objects;

import com.elastisys.scale.commons.json.JsonUtils;
import com.google.common.base.Throwables;

/**
 * A JSON type typically used in REST API responses to convey error information.
 */
public class ErrorType {

    /** A human-readable description of the error. */
    private final String message;

    /**
     * A more detailed error description. This could, for example, be a
     * stacktrace.
     */
    private final String detail;

    /**
     * Constructs an {@link ErrorType} with the empty string as message and
     * detail.
     */
    public ErrorType() {
        this("", "");
    }

    /**
     * Constructs an {@link ErrorType} with an error message and without any
     * error detail.
     *
     * @param message
     *            A human-readable description of the error
     */
    public ErrorType(String message) {
        this(message, "");
    }

    /**
     * Constructs an {@link ErrorType} with a specified error message and a
     * {@code detail} consisting of an {@link Exception} stacktrace.
     *
     * @param message
     *            A human-readable description of the error.
     * @param exception
     */
    public ErrorType(String message, Exception exception) {
        this(message, Throwables.getStackTraceAsString(exception));
    }

    /**
     * Constructs an {@link ErrorType} with error message and details
     * (stacktrace) message taken from an {@link Exception}.
     *
     * @param exception
     */
    public ErrorType(Exception exception) {
        this(exception.getMessage(), Throwables.getStackTraceAsString(exception));
    }

    /**
     * Constructs an {@link ErrorType} with an error message and an error
     * detail.
     *
     * @param message
     *            A human-readable description of the error.
     * @param detail
     *            A more detailed error description. This could, for example, be
     *            a stacktrace.
     */
    public ErrorType(String message, String detail) {
        this.message = message;
        this.detail = detail;
    }

    /**
     * Returns a human-readable description of the error.
     *
     * @return
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Returns a more detailed error description. This could, for example, be a
     * stacktrace.
     *
     * @return
     */
    public String getDetail() {
        return this.detail;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.message, this.detail);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            ErrorType other = (ErrorType) obj;
            return Objects.equals(this.message, other.message) && Objects.equals(this.detail, other.detail);
        }
        return false;
    }

    @Override
    public String toString() {
        return JsonUtils.toPrettyString(JsonUtils.toJson(this));
    }
}
