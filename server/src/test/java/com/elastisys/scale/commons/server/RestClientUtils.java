package com.elastisys.scale.commons.server;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.filter.LoggingFilter;

/**
 * Utility class for creating REST {@link Client}s with different security
 * settings. All {@link Client}s are insecure in the sense that no server
 * certificates are validated and no hostname verification is performed.
 */
public class RestClientUtils {

    /**
     * Produces a HTTP client with BASIC client authentication.
     *
     * @param userName
     * @param password
     * @return
     */
    public static Client httpBasicAuth(String userName, String password) {
        Client client = ClientBuilder.newBuilder().build();
        client.register(HttpAuthenticationFeature.basic(userName, password));
        client.register(new LoggingFilter());
        return client;
    }

    /**
     * Produces a HTTP client with no client authentication.
     *
     * @return
     */
    public static Client httpNoAuth() {
        Client client = ClientBuilder.newBuilder().build();
        client.register(new LoggingFilter());
        return client;
    }

    /**
     * Produces a HTTPS client with BASIC client authentication.
     *
     * @param userName
     * @param password
     * @return
     */
    public static Client httpsBasicAuth(String userName, String password) {
        SSLContext sslContext = insecureSslContext();
        Client client = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(allowAnyVerifier()).build();
        client.register(HttpAuthenticationFeature.basic(userName, password));
        client.register(new LoggingFilter());
        return client;
    }

    /**
     * Produces a HTTPS client with no client authentication.
     *
     * @return
     */
    public static Client httpsNoAuth() {
        SSLContext sslContext = insecureSslContext();
        Client client = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(allowAnyVerifier()).build();
        client.register(new LoggingFilter());
        return client;
    }

    /**
     * Produces a HTTPS client with client certificate-based authentication.
     *
     * @param keyStorePath
     *            Key store where client certificate is stored.
     * @param keyStorePassword
     *            Key store password.
     * @param keystoreType
     *            The type of key store.
     * @return
     * @throws RuntimeException
     */
    public static Client httpsCertAuth(String keyStorePath, String keyStorePassword, SslKeyStoreType keystoreType)
            throws RuntimeException {

        SSLContext sslContext;
        try (InputStream keystoreFile = new FileInputStream(keyStorePath)) {
            KeyStore keystore = KeyStore.getInstance(keystoreType.name());
            keystore.load(keystoreFile, keyStorePassword.toCharArray());
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .loadKeyMaterial(keystore, keyStorePassword.toCharArray()).build();
        } catch (Exception e) {
            throw new RuntimeException("failed to set up an SSL context: " + e.getMessage(), e);
        }

        Client client = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(allowAnyVerifier()).build();

        client.register(new LoggingFilter());
        return client;
    }

    /**
     * Creates a {@link SSLContext} that trusts all server certificates it is
     * presented with. That is, all certificate chain/host identity checks are
     * disabled.
     * <p/>
     * This is similar to using the <code>--insecure</code> flag in
     * <code>curl</code>.
     *
     * @return
     */
    private static SSLContext insecureSslContext() {
        try {
            return new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        } catch (Exception e) {
            throw new RuntimeException("failed to set up an insecure SSL context: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a {@link HostnameVerifier} that, during SSL handshakes, considers
     * all host names valid.
     *
     * @return
     */
    private static HostnameVerifier allowAnyVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }
}
