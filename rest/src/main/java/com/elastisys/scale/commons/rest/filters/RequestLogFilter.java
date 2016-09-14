package com.elastisys.scale.commons.rest.filters;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Jersey {@link ContainerRequestContext} filter that outputs each incoming
 * request. For example, <code>GET /api/resource by 1.2.3.4</code>.
 */
public class RequestLogFilter implements ContainerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(RequestLogFilter.class);

    /** Have request injected by Jersey. */
    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.info("{} {} by {}", this.request.getMethod(), requestContext.getUriInfo().getAbsolutePath(),
                this.request.getRemoteHost());
    }

}
