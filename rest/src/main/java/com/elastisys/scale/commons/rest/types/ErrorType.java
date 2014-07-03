package com.elastisys.scale.commons.rest.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Throwables;

/**
 * REST API JSON response type that represents an error.
 * 
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorType {

	/** The error message */
	private String message;

	/** The error detail (such as a stacktrace). */
	private String detail;

	public ErrorType() {
		// empty constructor mandated by JAXB
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
}
