package com.elastisys.scale.commons.net.alerter.http;

import java.util.Objects;
import java.util.Optional;

import javax.naming.ConfigurationException;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;

/**
 * Authentiaction configuration for a {@link HttpAlerter}.
 *
 * @see HttpAlerter
 * @see HttpAlerterConfig
 */
public class HttpAuthConfig {
    /**
     * Username/password credentials for basic authentication. May be
     * <code>null</code> if no BASIC authentication is desired.
     */
    private final BasicCredentials basicCredentials;
    /**
     * Certificate credentials for certificate-based client authentication. May
     * be <code>null</code> if no client certificate authentication is desired.
     */
    private final CertificateCredentials certificateCredentials;

    /**
     * @param basicCredentials
     *            Username/password credentials for basic authentication. May be
     *            <code>null</code> if no BASIC authentication is desired.
     * @param certificateCredentials
     *            Certificate credentials for certificate-based client
     *            authentication. May be <code>null</code> if no client
     *            certificate authentication is desired.
     */
    public HttpAuthConfig(BasicCredentials basicCredentials, CertificateCredentials certificateCredentials) {
        this.basicCredentials = basicCredentials;
        this.certificateCredentials = certificateCredentials;
    }

    /**
     * Returns credentials for HTTP Basic Authentication, if set.
     *
     * @return
     */
    public Optional<BasicCredentials> getBasicCredentials() {
        return Optional.ofNullable(this.basicCredentials);
    }

    /**
     * Returns credentials for SSL/TLS certificate-based authentication, if set.
     *
     * @return
     */
    public Optional<CertificateCredentials> getCertificateCredentials() {
        return Optional.ofNullable(this.certificateCredentials);
    }

    /**
     * Makes a basic sanity check verifying that all values are non-
     * <code>null</code>. If a value is missing for any field a
     * {@link ConfigurationException} is thrown.
     *
     * @throws IllegalArgumentException
     *             If any configuration field is missing.
     */
    public void validate() throws IllegalArgumentException {
        try {
            // validate client credentials
            if (getBasicCredentials().isPresent()) {
                getBasicCredentials().get().validate();
            }
            if (getCertificateCredentials().isPresent()) {
                getCertificateCredentials().get().validate();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("auth: " + e.getMessage(), e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.basicCredentials, this.certificateCredentials);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HttpAuthConfig) {
            HttpAuthConfig that = (HttpAuthConfig) obj;
            return Objects.equals(this.basicCredentials, that.basicCredentials)
                    && Objects.equals(this.certificateCredentials, that.certificateCredentials);
        }
        return false;
    }

    @Override
    public String toString() {
        return JsonUtils.toString(JsonUtils.toJson(this, true));
    }
}
