package com.elastisys.scale.commons.server;

import static com.elastisys.scale.commons.server.BaseServerBuilder.checkArgument;
import static java.lang.String.format;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;

/**
 * A {@link ServletDefinition} declares how a certain {@link Servlet} should be
 * deployed on a {@link Server} built by a {@link ServletServerBuilder}.
 * <p/>
 * {@link ServletDefinition} instances can be built with its associated
 * {@link Builder}.The builder allows a {@link ServletDefinition}s to be created
 * with a range of possible security configurations, such as authentication
 * mechanism and authorization.
 *
 * @see ServletServerBuilder
 *
 *
 *
 */
public class ServletDefinition {

    /** The {@link Servlet} that is to be published. */
    private final Servlet servlet;
    /**
     * The context path on which the {@link Servlet} will be published.
     */
    private final String servletPath;

    /**
     * Boolean indicating if all access to the {@link Servlet} must take place
     * over a secure port. That is, any request attempts over HTTP (should the
     * server port be open) will be redirected to the secure (HTTPS) port.
     */
    private final boolean requireHttps;

    /**
     * Boolean indicating if {@link Servlet} clients must provide
     * username/password credentials according to the HTTP BASIC authentication
     * scheme.
     * <p/>
     * <i>Note: user credentials need to be specified via the {@link #realmFile}
     * option and role-based authorization via the {@link #requireRole}
     * option.</i>
     */
    private final boolean requireBasicAuth;
    /**
     * A credentials store that stores users, passwords, and roles for the
     * {@link Servlet}. The file follows the format prescribed by the Jetty
     * {@link HashLoginService}.
     * <p/>
     * <i/>Note: this option is only required when HTTP BASIC authentication is
     * specified</i>.
     */
    private final String realmFile;
    /**
     * The role that an authenticated user must be assigned to be granted access
     * to the {@link Servlet}. Users are assigned roles in the
     * {@link #realmFile}.
     * <p/>
     * <i/>Note: this option is only required when HTTP BASIC authentication is
     * specified</i>.
     */
    private final String requireRole;

    /**
     * Indicates if this {@link Servlet} is to accept cross-origin requests from
     * web pages loaded from a different web domain. Default is
     * <code>true</code>. For details, see
     * <a href="http://en.wikipedia.org/wiki/Cross-origin_resource_sharing"
     * >CORS</a>.
     */
    private final boolean supportCors;

    /**
     * Init parameters to be passed to the {@link Servlet} on start-up. These
     * are the parameters typically found in {@code <init-param>} tags in the
     * {@code web.xml} file.
     */
    private final Map<String, String> initParameters;

    private ServletDefinition(Servlet servlet, String servletPath, boolean requireHttps, boolean requireBasicAuth,
            String realmFile, String requireRole, boolean supportCors, Map<String, String> initParameters) {
        this.servlet = servlet;
        this.servletPath = servletPath;
        this.requireHttps = requireHttps;
        this.requireBasicAuth = requireBasicAuth;
        this.realmFile = realmFile;
        this.requireRole = requireRole;
        this.supportCors = supportCors;
        this.initParameters = initParameters;
    }

    /**
     * Returns the {@link Servlet} that is to be published.
     *
     * @return
     */
    public Servlet getServlet() {
        return this.servlet;
    }

    /**
     * Returns the context path on which the {@link Servlet} will be published.
     *
     * @return the servletPath
     */
    public String getServletPath() {
        return this.servletPath;
    }

    /**
     * Returns a boolean indicating if all access to the {@link Servlet} must
     * take place over a secure port. That is, any request attempts over HTTP
     * (should the server port be open) will be redirected to the secure (HTTPS)
     * port.
     *
     * @return
     */
    public boolean isRequireHttps() {
        return this.requireHttps;
    }

    /**
     * Returns a boolean indicating if {@link Servlet} clients must provide
     * username/password credentials according to the HTTP BASIC authentication
     * scheme.
     * <p/>
     * <i>Note: user credentials are specified in {@link #realmFile} and
     * role-based authorization in {@link #requireRole}.</i>
     *
     * @return
     */
    public boolean isRequireBasicAuth() {
        return this.requireBasicAuth;
    }

    /**
     * Returns a credentials store that stores users, passwords, and roles for
     * the {@link Servlet}. The file follows the format prescribed by the Jetty
     * {@link HashLoginService}.
     * <p/>
     * <i/>Note: only relevant when HTTP BASIC authentication is specified</i>.
     *
     * @return
     */
    public String getRealmFile() {
        return this.realmFile;
    }

    /**
     * Returns the role that an authenticated user must be assigned to be
     * granted access to the {@link Servlet}. Users are assigned roles in the
     * {@link #realmFile}.
     * <p/>
     * <i/>Note: only relevant when HTTP BASIC authentication is specified</i>.
     *
     * @return
     */
    public String getRequireRole() {
        return this.requireRole;
    }

    /**
     * Returns a {@link Boolean} indicating if the {@link Servlet} is to accept
     * cross-origin requests from web pages loaded from a different web domain.
     * For details, see
     * <a href="http://en.wikipedia.org/wiki/Cross-origin_resource_sharing"
     * >CORS</a>.
     *
     * @return
     */
    public boolean isSupportCors() {
        return this.supportCors;
    }

    /**
     * Returns the init parameters to be passed to the {@link Servlet} on
     * start-up. These are the parameters typically found in
     * {@code <init-param>} tags in the {@code web.xml} file.
     *
     * @return
     */
    public Map<String, String> getInitParameters() {
        return this.initParameters;
    }

    /**
     * A builder for {@link ServletDefinition}s.
     *
     *
     *
     */
    public static class Builder {
        /** The {@link Servlet} that is to be published. */
        private Servlet servlet = null;
        /**
         * The context path on which the {@link Servlet} will be published.
         * Defaults to: {@code /}.
         */
        private String servletPath = "/";

        /**
         * Requires all access to the {@link Servlet} to be over a secure port.
         * That is, any request attempts over HTTP (should the server port be
         * open) will be redirected to the secure (HTTPS) port. Default: false.
         */
        private boolean requireHttps = false;

        /**
         * Require {@link Servlet} clients to provide username/password
         * credentials according to the HTTP BASIC authentication scheme.
         * Default: <code>false</code>.
         * <p/>
         * <i>Note: user credentials need to be specified via the
         * {@link #realmFile} option and role-based authorization via the
         * {@link #requireRole} option.</i>
         */
        private boolean requireBasicAuth = false;
        /**
         * A credentials store that stores users, passwords, and roles for the
         * {@link Servlet}. The file follows the format prescribed by the Jetty
         * {@link HashLoginService}.
         * <p/>
         * <i/>Note: this option is only required when HTTP BASIC authentication
         * is specified</i>.
         */
        private String realmFile = null;
        /**
         * The role that an authenticated user must be assigned to be granted
         * access to the {@link Servlet}. Users are assigned roles in the
         * {@link #realmFile}.
         * <p/>
         * <i/>Note: this option is only required when HTTP BASIC authentication
         * is specified</i>.
         */
        private String requireRole = null;

        /**
         * Indicates if this {@link Servlet} is to accept cross-origin requests
         * from web pages loaded from a different web domain. Default is
         * <code>true</code>. For details, see
         * <a href="http://en.wikipedia.org/wiki/Cross-origin_resource_sharing"
         * >CORS</a>.
         */
        private boolean supportCors = true;

        /**
         * Init parameters to be passed to the {@link Servlet} on start-up.
         * These are the parameters typically found in {@code <init-param>} tags
         * in the {@code web.xml} file.
         */
        private Map<String, String> initParameters = new HashMap<>();

        /**
         * Creates a new {@link ServletDefinition} builder.
         */
        public Builder() {
        }

        /**
         * Builds a {@link Server} object from the parameters passed to the
         * {@link BaseServerBuilder}.
         *
         * @return
         */
        public ServletDefinition build() {
            checkArgument(this.servlet != null, "missing servlet");
            checkArgument(this.servletPath != null, "missing a servlet path");

            validateSecurityConfig();

            return new ServletDefinition(this.servlet, this.servletPath, this.requireHttps, this.requireBasicAuth,
                    this.realmFile, this.requireRole, this.supportCors, this.initParameters);
        }

        private void validateSecurityConfig() {
            if (this.requireBasicAuth) {
                checkArgument(this.realmFile != null,
                        "a security realm file must be specified " + "when basic authentication is specified");
                checkArgument(new File(this.realmFile).isFile(),
                        format("specified security realm file '%s' is not " + "a valid file", this.realmFile));
                checkArgument(this.requireRole != null,
                        "a required user role must be specified when basic " + "authentication is specified");
            }
        }

        /**
         * Set the {@link Servlet} that is to be published.
         *
         * @param servlet
         * @return
         */
        public Builder servlet(Servlet servlet) {
            this.servlet = servlet;
            return this;
        }

        /**
         *
         * Set the context path on which the {@link Servlet} will be published.
         * Defaults to: {@code /}.
         *
         * @param servletPath
         * @return
         */
        public Builder servletPath(String servletPath) {
            this.servletPath = servletPath;
            return this;
        }

        /**
         * Set to <code>true</code> to require all access to the {@link Servlet}
         * to be over a secure port. That is, any request attempts over HTTP
         * (should the server port be open) will be redirected to the secure
         * (HTTPS) port. Default: true.
         *
         * @param requireSecureAccess
         * @return
         */
        public Builder requireHttps(boolean requireSecureAccess) {
            this.requireHttps = requireSecureAccess;
            return this;
        }

        /**
         * Set to <code>true</code> to require {@link Servlet} clients to
         * provide username/password credentials according to the HTTP BASIC
         * authentication scheme. Default: <code>false</code>.
         * <p/>
         * <i>Note: user credentials need to be specified via the
         * {@link #realmFile} option and role-based authorization via the
         * {@link #requireRole} option.</i>
         *
         * @param requireBasicAuth
         * @return
         */
        public Builder requireBasicAuth(boolean requireBasicAuth) {
            this.requireBasicAuth = requireBasicAuth;
            return this;
        }

        /**
         * A credentials store that stores users, passwords, and roles for the
         * {@link Servlet}. The file follows the format prescribed by the Jetty
         * {@link HashLoginService}.
         * <p/>
         * <i/>Note: this option is only required when {@link #requireBasicAuth}
         * is set</i>.
         *
         * @param realmFile
         * @return
         */
        public Builder realmFile(String realmFile) {
            this.realmFile = realmFile;
            return this;
        }

        /**
         * Set the role that an authenticated user must be assigned to be
         * granted access to the {@link Servlet}. Users are assigned roles in
         * the {@link #realmFile}.
         * <p/>
         * <i/>Note: this option is only required when {@link #requireBasicAuth}
         * is set</i>.
         *
         * @param role
         * @return
         */
        public Builder requireRole(String role) {
            this.requireRole = role;
            return this;
        }

        /**
         * Indicates if the {@link Servlet} is to accept cross-origin requests
         * from web pages loaded from a different web domain. Default is
         * <code>true</code>. For details, see
         * <a href="http://en.wikipedia.org/wiki/Cross-origin_resource_sharing"
         * >CORS</a>.
         *
         * @param supportCors
         *            <code>true</code> if cross-origin requests are to be
         *            supported, <code>false</code> otherwise.
         * @return
         */
        public Builder supportCors(boolean supportCors) {
            this.supportCors = supportCors;
            return this;
        }

        /**
         * Adds an init parameter to be passed to the {@link Servlet} on
         * start-up. These are the parameters typically found in
         * {@code <init-param>} tags in the {@code web.xml} file.
         *
         * @param parameter
         * @param value
         * @return
         */
        public Builder addInitParameter(String parameter, String value) {
            this.initParameters.put(parameter, value);
            return this;
        }
    }
}
