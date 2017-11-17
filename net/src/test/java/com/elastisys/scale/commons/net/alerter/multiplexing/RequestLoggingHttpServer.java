package com.elastisys.scale.commons.net.alerter.multiplexing;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * A simple embedded HTTP server that keeps track (in memory) of all requests it
 * has received.
 * <p/>
 * Intended for test use as a simple HTTP endpoint.
 */
public class RequestLoggingHttpServer implements Closeable {
    static Logger logger = LoggerFactory.getLogger(RequestLoggingHttpServer.class);

    /** The embedded HTTP server. */
    private final Server server;
    private final int httpPort;
    /** Keeps track of all POSTed messages to this HTTP server. */
    private final List<String> postedMessages;

    /**
     * Constructs a new {@link RequestLoggingHttpServer}.
     *
     * @param contextPath
     *            the root URI path to associate the context with
     * @param port
     *            HTTP server listening port
     */
    public RequestLoggingHttpServer(String contextPath, int port) {
        this.httpPort = port;
        this.postedMessages = new LinkedList<>();

        this.server = new Server(this.httpPort);
        this.server.setHandler(new LoggingRequestHandler(this.postedMessages));
    }

    /**
     * Starts the HTTP server.
     *
     * @throws Exception
     */
    public void start() throws Exception {
        this.server.start();
        logger.info("Started HTTP server " + this.httpPort);
    }

    /**
     * Stops the HTTP server.
     *
     * @throws Exception
     */
    public void stop() throws Exception {
        this.server.stop();
        this.server.join();
        logger.info("Stopped HTTP server " + this.httpPort);
    }

    @Override
    public void close() throws IOException {
        try {
            stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getPostedMessages() {
        return this.postedMessages;
    }

    public int getHttpPort() {
        return this.httpPort;
    }

    static class LoggingRequestHandler extends AbstractHandler {
        /** Keeps track of all POSTed messages to this HTTP server. */
        private final List<String> postedMessages;

        public LoggingRequestHandler(List<String> postedMessages) {
            this.postedMessages = postedMessages;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {

            String receivedMessage = IO.toString(request.getInputStream(), Charsets.UTF_8.displayName());
            logger.debug("received {} request: {}\n  Body: '{}'", request.getMethod(), request.getRequestURI(),
                    receivedMessage);
            this.postedMessages.add(receivedMessage);

            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            String responseMessage = "This is the response";
            response.getWriter().println(responseMessage);
        }
    }
}
