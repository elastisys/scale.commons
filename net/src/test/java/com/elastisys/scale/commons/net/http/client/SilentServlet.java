package com.elastisys.scale.commons.net.http.client;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * A servlet that will respond with 200 to all requests (GET and POST) with a
 * {@code 204 (No Content)} response without any message body in the response.
 */
public class SilentServlet extends HttpServlet {
	static final Logger logger = LoggerFactory
			.getLogger(HelloWorldServlet.class);

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String body = IO.toString(request.getInputStream(),
				Charsets.UTF_8.displayName());
		logger.debug("received {} request: {}\n  Body: '{}'",
				request.getMethod(), request.getRequestURI(), body);

		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		return;
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String body = IO.toString(request.getInputStream(),
				Charsets.UTF_8.displayName());
		logger.debug("received {} request: {}\n  Body: '{}'",
				request.getMethod(), request.getRequestURI(), body);

		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		return;
	}

}
