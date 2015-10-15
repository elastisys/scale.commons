package com.elastisys.scale.commons.rest.types;

import com.elastisys.scale.commons.json.JsonUtils;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;

/**
 * REST API JSON response type that represents an error.
 */
public class ErrorType {

	/** The error message */
	private String message;

	/** The error detail (such as a stacktrace). */
	private String detail;

	public ErrorType() {
	}

	/**
	 * Constructs an {@link ErrorType} with an error message and without any
	 * error detail.
	 *
	 * @param message
	 */
	public ErrorType(String message) {
		this(message, "");
	}

	/**
	 * Constructs an {@link ErrorType} with a specified error message and a
	 * details message consisting of an {@link Exception} stacktrace.
	 *
	 * @param e
	 */
	public ErrorType(String message, Exception e) {
		this(message, Throwables.getStackTraceAsString(e));
	}

	/**
	 * Constructs an {@link ErrorType} with error message and details
	 * (stacktrace) message taken from an {@link Exception}.
	 *
	 * @param e
	 */
	public ErrorType(Exception e) {
		this(e.getMessage(), Throwables.getStackTraceAsString(e));
	}

	/**
	 * Constructs an {@link ErrorType} with an error message and an error
	 * detail.
	 *
	 * @param message
	 * @param detail
	 */
	public ErrorType(String message, String detail) {
		this.message = message;
		this.detail = detail;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDetail() {
		return this.detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.message, this.detail);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ErrorType) {
			ErrorType other = (ErrorType) obj;
			return Objects.equal(this.message, other.message)
					&& Objects.equal(this.detail, other.detail);
		}
		return false;
	}

	@Override
	public String toString() {
		try {
			return JsonUtils.toPrettyString(JsonUtils.toJson(this));
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to convert to string: " + e.getMessage(), e);
		}
	}
}
