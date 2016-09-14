package com.elastisys.scale.commons.server;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.Servlet;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.security.Constraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * A flexible Builder for creating an embedded Jetty {@link Server} that
 * publishes one or more {@link Servlet}s.
 * <p/>
 * The builder allows a {@link Server} to be created with a range of possible
 * security configurations. In addition to (or as alternative to) server-level
 * security settings, the {@link ServletServerBuilder} allows application/
 * {@link Servlet}-specific security (authentication mechanism and
 * authorization) to be configured.
 * <p/>
 * This builder extends on the {@link BaseServerBuilder} through the delegate
 * pattern.
 *
 * @see BaseServerBuilder
 *
 *
 *
 */
public class ServletServerBuilder {
    static Logger LOG = LoggerFactory.getLogger(ServletServerBuilder.class);

    /**
     * The {@link BaseServerBuilder} that this builder "extends", to which this
     * builder delegates the building of the base server (without application).
     */
    private BaseServerBuilder baseServerBuilder;

    private List<ServletDefinition> servletDefinitions = Lists.newArrayList();

    /**
     * Constructs a new {@link ServletServerBuilder}.
     *
     * @param serverBuilder
     *            The builder that builds the "base server" (that is, a server
     *            without an application).
     */
    protected ServletServerBuilder(BaseServerBuilder serverBuilder) {
        this.baseServerBuilder = serverBuilder;
    }

    /**
     * Creates a new {@link ServletServerBuilder}.
     *
     * @return
     */
    public static ServletServerBuilder create() {
        return new ServletServerBuilder(BaseServerBuilder.create());
    }

    /**
     * Builds a {@link Server} object from the parameters passed to the
     * {@link ServletServerBuilder}.
     *
     * @return
     */
    public Server build() {
        // build base server
        Server server = this.baseServerBuilder.build();

        // build each servlet to be published
        HandlerList servletHandlers = new HandlerList();
        for (ServletDefinition servletDefinition : this.servletDefinitions) {
            // create the servlet request handler
            ServletContextHandler servletHandler = createServletHandler(servletDefinition);

            // add security handler if security settings were specified
            if (servletDefinition.isRequireHttps() || servletDefinition.isRequireBasicAuth()) {
                ConstraintSecurityHandler securityHandler = createSecurityHandler(server, servletDefinition);
                servletHandler.setSecurityHandler(securityHandler);
            }
            if (servletDefinition.isSupportCors()) {
                addCrossOriginFilter(servletHandler);
            }
            servletHandlers.addHandler(servletHandler);
        }
        server.setHandler(servletHandlers);

        return server;
    }

    /**
     * Adds support for
     * <a href="http://en.wikipedia.org/wiki/Cross-origin_resource_sharing"
     * >CORS</a> to the {@link Servlet} by adding a {@link CrossOriginFilter}.
     *
     * @param servletHandler
     */
    private void addCrossOriginFilter(ServletContextHandler servletHandler) {
        LOG.debug("enabling CORS support");
        EnumSet<DispatcherType> dispatches = EnumSet.of(DispatcherType.ASYNC, DispatcherType.ERROR,
                DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.REQUEST);
        CrossOriginFilter corsFilter = new CrossOriginFilter();
        FilterHolder filterHolder = new FilterHolder(corsFilter);
        // use defaults for filter parameters except for the below ones
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,HEAD,DELETE,OPTIONS");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
        servletHandler.addFilter(filterHolder, "/*", dispatches);
    }

    /**
     * Creates a {@link ConstraintSecurityHandler} for the server that
     * configures (secure) transport guarantees, authentication, authorization,
     * etc according to the builder security settings.
     *
     * @param server
     *            The {@link Server} for which the security handler is created.
     * @return
     */
    private ConstraintSecurityHandler createSecurityHandler(Server server, ServletDefinition servletDefinition) {
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setServer(server);

        Constraint constraint = new Constraint();
        constraint.setName("security" + servletDefinition.hashCode());

        if (servletDefinition.isRequireBasicAuth()) {
            // add basic authentication and role-based authorization based on
            // the credentials store (realm file)
            LoginService loginService = new HashLoginService("elastisys:scale security realm",
                    servletDefinition.getRealmFile());
            securityHandler.getServer().addBean(loginService);
            securityHandler.setAuthenticator(new BasicAuthenticator());
            securityHandler.setLoginService(loginService);
            constraint.setAuthenticate(true);
            constraint.setRoles(new String[] { servletDefinition.getRequireRole() });
        }

        // require confidential transport: HTTP requests will be redirected to
        // the secure (https) port.
        if (servletDefinition.isRequireHttps()) {
            constraint.setDataConstraint(Constraint.DC_CONFIDENTIAL);
        }

        // apply constraint to all pages/web resources
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setConstraint(constraint);
        mapping.setPathSpec("/*");
        securityHandler.setConstraintMappings(Lists.newArrayList(mapping));

        return securityHandler;
    }

    /**
     * Adds a {@link Servlet} that is to be published.
     *
     * @param servletDefinition
     *            The servlet definition. See {@link ServletDefinition.Builder}.
     * @return
     */
    public ServletServerBuilder addServlet(ServletDefinition servletDefinition) {
        this.servletDefinitions.add(servletDefinition);
        return this;
    }

    /**
     * Creates a request {@link Handler} for a {@link Servlet} that is to be
     * published.
     *
     * @param servletDefinition
     * @return
     */
    private ServletContextHandler createServletHandler(ServletDefinition servletDefinition) {
        Servlet servlet = servletDefinition.getServlet();
        String servletPath = servletDefinition.getServletPath();

        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        // context path where servlet is "mounted"
        servletHandler.setContextPath(servletPath);
        // the request paths to listen for: "<servletPath>/*"
        // which will match:
        // - /<servletPath>
        // - /<servletPath>/
        // - /<servletPath>/some/path
        // etc ...
        // String pathSpec = servletPath + "/*";
        // pathSpec = pathSpec.replaceAll("/+", "/");
        String pathSpec = "/*";
        ServletHolder servletHolder = new ServletHolder(servlet);
        Map<String, String> initParams = servletDefinition.getInitParameters();
        servletHolder.setInitParameters(initParams);
        servletHandler.addServlet(servletHolder, pathSpec);
        LOG.debug("adding servlet '{}' at context path '{}' with path spec '{}' and init-params {}", servlet,
                servletPath, pathSpec, initParams);
        return servletHandler;
    }

    /**
     * @param port
     * @return
     * @see com.elastisys.scale.commons.server.BaseServerBuilder#httpPort(java.lang.Integer)
     */
    public ServletServerBuilder httpPort(Integer port) {
        this.baseServerBuilder.httpPort(port);
        return this;
    }

    /**
     * @param port
     * @return
     * @see com.elastisys.scale.commons.server.BaseServerBuilder#httpsPort(java.lang.Integer)
     */
    public ServletServerBuilder httpsPort(Integer port) {
        this.baseServerBuilder.httpsPort(port);
        return this;
    }

    /**
     * @param pathOrUri
     * @return
     * @see com.elastisys.scale.commons.server.BaseServerBuilder#sslKeyStorePath(java.lang.String)
     */
    public ServletServerBuilder sslKeyStorePath(String pathOrUri) {
        this.baseServerBuilder.sslKeyStorePath(pathOrUri);
        return this;
    }

    /**
     * @param type
     * @return
     * @see com.elastisys.scale.commons.server.BaseServerBuilder#sslKeyStoreType(com.elastisys.scale.commons.SslKeyStoreType.ssl.KeyStoreType)
     */
    public ServletServerBuilder sslKeyStoreType(SslKeyStoreType type) {
        this.baseServerBuilder.sslKeyStoreType(type);
        return this;
    }

    /**
     * @param password
     * @return
     * @see com.elastisys.scale.commons.server.BaseServerBuilder#sslKeyStorePassword(java.lang.String)
     */
    public ServletServerBuilder sslKeyStorePassword(String password) {
        this.baseServerBuilder.sslKeyStorePassword(password);
        return this;
    }

    /**
     * @param password
     * @return
     * @see com.elastisys.scale.commons.server.BaseServerBuilder#sslKeyPassword(java.lang.String)
     */
    public ServletServerBuilder sslKeyPassword(String password) {
        this.baseServerBuilder.sslKeyPassword(password);
        return this;
    }

    /**
     * @param pathOrUri
     * @return
     * @see com.elastisys.scale.commons.server.BaseServerBuilder#sslTrustStorePath(java.lang.String)
     */
    public ServletServerBuilder sslTrustStorePath(String pathOrUri) {
        this.baseServerBuilder.sslTrustStorePath(pathOrUri);
        return this;
    }

    /**
     * @param type
     * @return
     * @see com.elastisys.scale.commons.server.BaseServerBuilder#sslTrustStoreType(com.elastisys.scale.commons.SslKeyStoreType.ssl.KeyStoreType)
     */
    public ServletServerBuilder sslTrustStoreType(SslKeyStoreType type) {
        this.baseServerBuilder.sslTrustStoreType(type);
        return this;
    }

    /**
     * @param password
     * @return
     * @see com.elastisys.scale.commons.server.BaseServerBuilder#sslTrustStorePassword(java.lang.String)
     */
    public ServletServerBuilder sslTrustStorePassword(String password) {
        this.baseServerBuilder.sslTrustStorePassword(password);
        return this;
    }

    /**
     * @param requireCertAuthentication
     * @return
     * @see com.elastisys.scale.commons.server.BaseServerBuilder#sslRequireClientCert(boolean)
     */
    public ServletServerBuilder sslRequireClientCert(boolean requireCertAuthentication) {
        this.baseServerBuilder.sslRequireClientCert(requireCertAuthentication);
        return this;
    }

}
