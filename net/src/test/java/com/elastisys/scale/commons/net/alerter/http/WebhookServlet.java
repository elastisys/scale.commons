package com.elastisys.scale.commons.net.alerter.http;

import java.io.IOException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

/**
 * A {@link Servlet} used in tests that saves a list of all received POST
 * message bodys.
 */
public class WebhookServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	static final Logger logger = LoggerFactory.getLogger(WebhookServlet.class);

	private final int responseCode;
	private final List<Alert> receivedAlerts = Lists.newArrayList();

	/**
	 * @param responseCode
	 *            The HTTP response code that the servlet will always respond.
	 *            For example, {@link HttpServletResponse#SC_OK}. with.
	 */
	public WebhookServlet(int responseCode) {
		super();
		this.responseCode = responseCode;
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String body = IO.toString(request.getInputStream(),
				Charsets.UTF_8.displayName());
		logger.debug("received {} request: {}\n  Body: '{}'",
				request.getMethod(), request.getRequestURI(), body);
		this.receivedAlerts.add(JsonUtils.toObject(
				JsonUtils.parseJsonString(body), Alert.class));

		response.setContentType("text/html;charset=utf-8");
		response.setStatus(this.responseCode);
		response.getWriter().close();
	}

	/**
	 * Forget any received {@link Alert}s.
	 */
	public void clear() {
		this.receivedAlerts.clear();
	}

	public List<Alert> getReceivedMessages() {
		return this.receivedAlerts;
	}
}
