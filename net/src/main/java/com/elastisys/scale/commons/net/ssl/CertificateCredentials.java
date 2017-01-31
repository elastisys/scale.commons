package com.elastisys.scale.commons.net.ssl;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;

import com.elastisys.scale.commons.net.http.client.AuthenticatedHttpClient;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * Represents client credentials for certificate-based SSL authentication (as
 * described
 * <a href="http://docs.oracle.com/javaee/6/tutorial/doc/glien.html">here</a>.
 *
 * @see AuthenticatedHttpClient
 */
public class CertificateCredentials {
    /** Default keystore type. */
    private static final KeyStoreType DEFAULT_KEYSTORE_TYPE = KeyStoreType.PKCS12;

    /** The type of the key store. */
    private final KeyStoreType keystoreType;
    /** File system path to the SSL key store. */
    private final String keystorePath;
    /** The password used to protect the integrity of the SSL key store. */
    private final String keystorePassword;
    /**
     * The password used to recover keys within the key store. This field may be
     * <code>null</code>, in which case the {@link #keystorePassword} will be
     * used (as it is not uncommon for the {@link #keyPassword} to be the same
     * as the {@link #keystorePassword}).
     */
    private final String keyPassword;

    /**
     * Constructs {@link CertificateCredentials} that are read from a PKCS12 key
     * store.
     *
     * @param keystorePath
     *            File system path to the SSL key store.
     * @param keystorePassword
     *            The password used to protect the integrity of the SSL key
     *            store. This will also be used as the {@link #keyPassword}.
     */
    public CertificateCredentials(String keystorePath, String keystorePassword) {
        this(DEFAULT_KEYSTORE_TYPE, keystorePath, keystorePassword, null);
    }

    /**
     * Constructs {@link CertificateCredentials} that are read from a PKCS12 key
     * store.
     *
     * @param keystorePath
     *            File system path to the SSL key store.
     * @param keystorePassword
     *            The password used to protect the integrity of the SSL key
     *            store.
     * @param keyPassword
     *            The password used to recover keys within the key store. This
     *            field may be <code>null</code>, in which case the
     *            {@link #keystorePassword} will be used (as it is not uncommon
     *            for the {@link #keyPassword} to be the same as the
     *            {@link #keystorePassword})
     */
    public CertificateCredentials(String keystorePath, String keystorePassword, String keyPassword) {
        this(DEFAULT_KEYSTORE_TYPE, keystorePath, keystorePassword, keyPassword);
    }

    /**
     * Constructs {@link CertificateCredentials} that are read from a key store.
     *
     * @param keystoreType
     *            The type of the key store (for example, "JKS" or "PKCS12").
     * @param keystorePath
     *            File system path to the SSL key store.
     * @param keystorePassword
     *            The password used to protect the integrity of the SSL key
     *            store.
     * @param keyPassword
     *            The password used to recover keys within the key store. This
     *            field may be <code>null</code>, in which case the
     *            {@link #keystorePassword} will be used (as it is not uncommon
     *            for the {@link #keyPassword} to be the same as the
     *            {@link #keystorePassword})
     */
    public CertificateCredentials(KeyStoreType keystoreType, String keystorePath, String keystorePassword,
            String keyPassword) {
        checkArgument(keystoreType != null, "certificate credentials missing keystore type");
        checkArgument(keystorePath != null, "certificate credentials missing keystore path");
        checkArgument(new File(keystorePath).isFile(), "certificate credentials keystore path '%s' is not a file",
                keystorePath);
        checkArgument(keystorePassword != null, "certificate credentials missing keystorePassword");

        this.keystoreType = keystoreType;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.keyPassword = keyPassword;
    }

    /**
     * Returns the type of the key store.
     *
     * @return
     */
    public KeyStoreType getKeystoreType() {
        // in case the object was parsed from JSON, it may be that keystoreType
        // is null
        return Optional.fromNullable(this.keystoreType).or(DEFAULT_KEYSTORE_TYPE);
    }

    /**
     * Returns the file system path to the SSL key store.
     *
     * @return
     */
    public String getKeystorePath() {
        return this.keystorePath;
    }

    /**
     * Returns the password used to protect the integrity of the SSL key store.
     *
     * @return
     */
    public String getKeystorePassword() {
        return this.keystorePassword;
    }

    /**
     * Returns the password used to recover keys within the key store.
     *
     * @return
     */
    public String getKeyPassword() {
        return Optional.fromNullable(this.keyPassword).or(this.keystorePassword);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getKeystoreType(), this.keystorePath, this.keystorePassword, this.keyPassword);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CertificateCredentials) {
            CertificateCredentials that = (CertificateCredentials) obj;
            return Objects.equal(getKeystoreType(), that.getKeystoreType())
                    && Objects.equal(this.keystorePath, that.keystorePath)
                    && Objects.equal(this.keystorePassword, that.keystorePassword)
                    && Objects.equal(this.keyPassword, that.keyPassword);
        }
        return false;
    }

    /**
     * Performs a basic sanity check to verify that the combination of
     * parameters is valid. If validation fails an
     * {@link IllegalArgumentException} is thrown.
     *
     * @throws IllegalArgumentException
     *             If any configuration field is missing.
     */
    public void validate() throws IllegalArgumentException {
        checkArgument(this.keystorePath != null, "certificateCredentials: missing keystore path");
        checkArgument(new File(this.keystorePath).isFile(), "certificateCredentials: keystore path '%s' is not a file",
                this.keystorePath);
        checkArgument(this.keystorePassword != null, "certificateCredentials: missing keystore password");
    }
}
