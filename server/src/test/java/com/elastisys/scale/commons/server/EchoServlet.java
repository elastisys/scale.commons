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
 * Dummy {@link Servlet} used in tests.
 * 
 * 
 * 
 */
public class EchoServlet extends HttpServlet {
	static final Logger logger = LoggerFactory.getLogger(EchoServlet.class);

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String body = IO.toString(request.getInputStream(),
				Charsets.UTF_8.displayName());
		logger.debug("received {} request: {}\n  Body: '{}'",
				request.getMethod(), request.getRequestURI(), body);

		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(body);
	}
}
