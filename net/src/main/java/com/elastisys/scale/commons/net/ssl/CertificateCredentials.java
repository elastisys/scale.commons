package com.elastisys.scale.commons.net.ssl;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;

import com.elastisys.scale.commons.net.http.AuthenticatedHttpRequester;
import com.elastisys.scale.commons.net.http.client.AuthenticatedHttpClient;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * Represents client credentials for certificate-based SSL authentication (as
 * described <a
 * href="http://docs.oracle.com/javaee/6/tutorial/doc/glien.html">here</a>.
 *
 * @see AuthenticatedHttpClient
 * @see AuthenticatedHttpRequester
 */
public class CertificateCredentials {

	/** The type of the key store. */
	private final KeyStoreType keystoreType;
	/** File system path to the SSL key store. */
	private final String keystorePath;
	/** The password used to protect the SSL key store. */
	private final String keystorePassword;
	/**
	 * The password used to protect the certificate's private key within the key
	 * store (only required for password-protected keys).
	 */
	private final String keyPassword;

	/**
	 * Constructs new {@link CertificateCredentials} without a key password.
	 *
	 * @param keystoreType
	 *            The type of the key store.
	 * @param keystorePath
	 *            File system path to the SSL key store.
	 * @param keystorePassword
	 *            The password used to protect the SSL key store.
	 */
	public CertificateCredentials(KeyStoreType keystoreType,
			String keystorePath, String keystorePassword) {
		this(keystoreType, keystorePath, keystorePassword, null);
	}

	/**
	 * Constructs new {@link CertificateCredentials}.
	 *
	 * @param keystoreType
	 *            The type of the key store (for example, "JKS" or "PKCS12").
	 * @param keystorePath
	 *            File system path to the SSL key store.
	 * @param keystorePassword
	 *            The password used to protect the SSL key store.
	 * @param keyPassword
	 *            The password used to protect the certificate's private key
	 *            within the key store. May be <code>null</code>, only required
	 *            for password-protected keys.
	 */
	public CertificateCredentials(KeyStoreType keystoreType,
			String keystorePath, String keystorePassword, String keyPassword) {
		checkArgument(keystoreType != null,
				"certificate credentials missing keystore type");
		checkArgument(keystorePath != null,
				"certificate credentials missing keystore path");
		checkArgument(new File(keystorePath).isFile(),
				"certificate credentials keystore path '%s' is not a file",
				keystorePath);
		checkArgument(keystorePassword != null,
				"certificate credentials missing keystore password");

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
		return this.keystoreType;
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
	 * Returns the password used to protect the SSL key store.
	 *
	 * @return
	 */
	public String getKeystorePassword() {
		return this.keystorePassword;
	}

	/**
	 * Returns the password used to protect the certificate's private key within
	 * the key store (only required for password-protected keys).
	 *
	 * @return
	 */
	public Optional<String> getKeyPassword() {
		return Optional.fromNullable(this.keyPassword);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.keystoreType, this.keystorePath,
				this.keystorePassword, this.keyPassword);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CertificateCredentials) {
			CertificateCredentials that = (CertificateCredentials) obj;
			return Objects.equal(this.keystoreType, that.keystoreType)
					&& Objects.equal(this.keystorePath, that.keystorePath)
					&& Objects.equal(this.keystorePassword,
							that.keystorePassword)
					&& Objects.equal(this.keyPassword, that.keyPassword);
		}
		return false;
	}
}
