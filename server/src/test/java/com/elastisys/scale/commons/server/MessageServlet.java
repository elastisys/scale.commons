package com.elastisys.scale.commons.server;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * Dummy {@link Servlet} that always responds with the same message.Intended for
 * use in tests.
 * 
 * 
 * 
 */
public class MessageServlet extends HttpServlet {
	static final Logger logger = LoggerFactory.getLogger(MessageServlet.class);

	/** Message that the {@link Servlet} responds to all requests with. */
	private final String message;

	/**
	 * Constructs a new {@link MessageServlet}.
	 * 
	 * @param message
	 *            Message that the {@link Servlet} responds to all requests
	 *            with.
	 */
	public MessageServlet(String message) {
		this.message = message;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String body = IO.toString(request.getInputStream(),
				Charsets.UTF_8.displayName());
		logger.debug("received {} request: {}\n  Body: '{}'",
				request.getMethod(), request.getRequestURI(), body);

		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print(this.message);
	}
}
