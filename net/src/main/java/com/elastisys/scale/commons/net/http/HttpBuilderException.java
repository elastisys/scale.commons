package com.elastisys.scale.commons.net.http;

/**
 * Exception thrown by a {@link HttpBuilder} on failure to build a {@link Http}
 * instance.
 * 
 * @see HttpBuilder
 */
public class HttpBuilderException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public HttpBuilderException() {
		super();
	}

	public HttpBuilderException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public HttpBuilderException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public HttpBuilderException(String message) {
		super(message);
	}

	public HttpBuilderException(Throwable cause) {
		super(cause);
	}

}