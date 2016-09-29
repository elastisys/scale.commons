package com.elastisys.scale.commons.cli.server;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import com.elastisys.scale.commons.cli.CommandLineOptions;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Range;

/**
 * A base class for HTTP(S) server command-line option parsing.
 * <p/>
 * Server executable main-classes can extend this base class to add
 * server-specific command-line options.
 * <p/>
 * The {@link #validate()} method can be called to make sure that provided
 * options were valid.
 * <p/>
 * It is intended for use with the {@code args4j} parser library.
 */
public abstract class BaseServerCliOptions implements CommandLineOptions {

    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    @Option(name = "--http-port", metaVar = "PORT", usage = "Enables a HTTP port on the server.")
    public Integer httpPort = null;

    @Option(name = "--https-port", metaVar = "PORT", usage = "Enables a HTTPS port on the server. "
            + "Note: when specified, an --ssl-keystore is required.")
    public Integer httpsPort = null;

    @Option(name = "--ssl-keystore", metaVar = "PATH", usage = "The location of the server's SSL key store (PKCS12 format). "
            + "Note: when specified, an --ssl-keystore-password is required.")
    public String sslKeyStore = null;
    @Option(name = "--ssl-keystore-password", metaVar = "PASSWORD", usage = "The password that protects the SSL key store.")
    public String sslKeyStorePassword = null;

    @Option(name = "--require-cert", usage = "Require SSL clients to authenticate with a certificate. "
            + "Note: when specified, an --ssl-truststore is required.")
    public boolean requireClientCert = false;

    @Option(name = "--ssl-truststore", metaVar = "PATH", usage = "The location of a SSL trust store (JKS format), containing trusted client certificates. "
            + "Note: when specified, a --ssl-trustore-password is required.")
    public String sslTrustStore = null;
    @Option(name = "--ssl-truststore-password", metaVar = "PASSWORD", usage = "The password that protects the SSL trust store.")
    public String sslTrustStorePassword = null;

    @Option(name = "--require-basicauth", usage = "Require clients to authenticate using basic HTTP authentication."
            + "Note: when specified, --realm-file and --require-role are required.")
    public boolean requireBasicAuth = false;

    @Option(name = "--realm-file", metaVar = "PATH", usage = "A credential store with users, passwords, and roles according to the format prescribed by the Jetty HashLoginService.")
    public String realmFile = null;

    @Option(name = "--require-role", metaVar = "ROLE", usage = "The role that an authenticated user must be assigned (in the --realm-file) to be granted access to the server.")
    public String requireRole = null;

    @Option(name = "--help", usage = "Display help text.")
    public boolean help = false;

    @Option(name = "--version", usage = "Displays the version of the server.")
    public boolean version = false; // default

    /** Positional command-line arguments end up here. */
    @Argument
    public List<String> positionalArgs = new ArrayList<String>();

    /**
     * Returns <code>true</code> if the {@code --help} flag is set.
     *
     * @return
     */
    @Override
    public boolean isHelpFlagSet() {
        return this.help;
    }

    @Override
    public boolean isVersionFlagSet() {
        return this.version;
    }

    /**
     * Returns the version of the executable.
     */
    @Override
    public abstract String getVersion();

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("httpPort", this.httpPort).add("httpsPort", this.httpsPort)//
                .add("sslKeyStore", this.sslKeyStore).add("sslKeyStorePassword", this.sslKeyStorePassword)//
                .add("requireClientCert", this.requireClientCert)//
                .add("sslTrustStore", this.sslTrustStore).add("sslTrustStorePassword", this.sslTrustStorePassword)
                .add("requireBasicAuth", this.requireBasicAuth) //
                .add("realmFile", this.realmFile).add("requireRole", this.requireRole)//
                .add("help", this.help).add("version", this.version).toString();
    }

    /**
     * Validates that the given combination of command-line options are valid.
     * If not, an {@link IllegalArgumentException} is thrown.
     *
     * @throws IllegalArgumentException
     */
    @Override
    public void validate() throws IllegalArgumentException {
        checkArgument(this.httpPort != null || this.httpsPort != null,
                "neither --http-port nor --https-port specified");
        if (this.httpPort != null) {
            checkArgument(Range.closed(MIN_PORT, MAX_PORT).contains(this.httpPort),
                    "--http-port: allowed port range is [1,65535]");
        }

        if (this.httpsPort != null) {
            checkArgument(Range.closed(MIN_PORT, MAX_PORT).contains(this.httpsPort),
                    "--https-port: allowed port range is [1,65535]");
            checkArgument(this.sslKeyStore != null, "--ssl-keystore is required when a https port is specified");
            checkArgument(this.sslKeyStorePassword != null, "no --ssl-keystore-password specified");
        }

        if (this.requireBasicAuth) {
            checkArgument(this.realmFile != null, "--realm-file is required when --require-basicauth is specified");
            checkArgument(this.requireRole != null, "--require-role is required when --require-basicauth is specified");
        }

        if (this.requireClientCert) {
            checkArgument(this.sslTrustStore != null, "--ssl-truststore is required when --require-cert is specified");
            checkArgument(this.sslTrustStorePassword != null,
                    "--ssl-truststore-password is required when --require-cert is specified");
        }
    }
}
