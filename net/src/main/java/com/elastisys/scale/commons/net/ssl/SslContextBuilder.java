package com.elastisys.scale.commons.net.ssl;

import static com.google.common.base.Preconditions.checkArgument;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

/**
 * A builder class that can be used to construct {@link SSLContext}s for SSL
 * clients that require different combinations of (1) client certificate
 * authentication and (2) server certificate authentication.
 * <p/>
 * The builder allows for optional client authentication, with a certificate
 * from a {@link KeyStore}, as well as optional authentication of the server
 * certificate against a trust store.
 */
public class SslContextBuilder {

	/**
	 * {@link KeyManagerFactory} acting as the source of authentication keys in
	 * case client certificate authentication is requested.
	 */
	private Optional<KeyManagerFactory> keyManagerFactory = Optional.absent();

	/**
	 * <code>true</code> if server authentication is requested,
	 * <code>false</code> otherwise.
	 */
	private boolean verifyHostCert = false;
	/**
	 * A custom trust store to use when server authentication is requested (via
	 * {@link #verifyHostCert}). If none is specified and server authentication
	 * is requested, the server certificate according to the default trust store
	 * configured with the JVM (see the guide on <a href=
	 * "http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html#CustomizingStores">
	 * JSSE</a>).
	 */
	private Optional<KeyStore> trustStore = Optional.absent();

	private SslContextBuilder() {
	}

	/**
	 * Create a new {@link SslContextBuilder} with initially neither client
	 * authentication nor server certificate authentication.
	 *
	 * @return
	 */
	public static SslContextBuilder newBuilder() {
		return new SslContextBuilder();
	}

	/**
	 * Build an {@link SSLContext} from the options provided to the
	 * {@link SslContextBuilder}.
	 *
	 * @return The created {@link SSLContext}.
	 * @throws RuntimeException
	 */
	public SSLContext build() throws RuntimeException {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");

			// add client certificate authentication, if specified
			KeyManager[] keyManagers = new KeyManager[0];
			if (this.keyManagerFactory.isPresent()) {
				keyManagers = this.keyManagerFactory.get().getKeyManagers();
			}

			// add server certificate authentication (null means rely on default
			// server certificate verification)
			TrustManager[] trustManagers = null;
			if (!this.verifyHostCert) {
				trustManagers = new TrustManager[] {
						SslUtils.insecureTrustManager() };
			} else {
				if (this.trustStore.isPresent()) {
					// custom trust store configured: use it
					trustManagers = trustManagerFromTrustStore(
							this.trustStore.get());
				} else {
					// rely on default trust store
					trustManagers = null;
				}
			}

			sslContext.init(keyManagers, trustManagers, new SecureRandom());
			return sslContext;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * The {@link SSLContext} should not authenticate the client with a client
	 * certificate.
	 *
	 * @return
	 */
	public SslContextBuilder noClientAuthentication() {
		this.keyManagerFactory = Optional.absent();
		return this;
	}

	/**
	 * The {@link SSLContext} should authenticate the client with a client
	 * certificate, provided in a {@link KeyStore}.
	 *
	 * @param keyStore
	 *            The client's {@link KeyStore}, containing the client's private
	 *            keys, and the certificates with their corresponding public
	 *            keys.
	 * @param keyPassword
	 *            The password for recovering keys in the key store. Note: this
	 *            password differs from the <i>keystore password</i>, which is
	 *            only used to verify the integrity of the {@link KeyStore} when
	 *            it is loaded.
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 */
	public SslContextBuilder clientAuthentication(KeyStore keyStore,
			String keyPassword) throws NoSuchAlgorithmException,
					UnrecoverableKeyException, KeyStoreException {
		checkArgument(keyStore != null, "null keystore given");
		checkArgument(keyPassword != null,
				"null keyPassword given (keystore keys cannot "
						+ "be recovered without a password)");

		KeyManagerFactory keyManagerFactory = KeyManagerFactory
				.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, keyPassword.toCharArray());
		this.keyManagerFactory = Optional.of(keyManagerFactory);
		return this;
	}

	/**
	 * Enables/disables server authentication. Set to <code>true</code> if host
	 * certificate verification is requested, <code>false</code> otherwise. If
	 * disabled, the server peer will not be verified, which is similar to using
	 * the {@code --insecure} flag in {@code curl}.
	 * <p/>
	 * If enabled, the host certificate is verified against either the
	 * configured trust store (if one has been set via
	 * {@link #serverAuthTrustStore(KeyStore)}) or a against a default trust
	 * store configured with the JVM (see the guide on <a href=
	 * "http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html#CustomizingStores">
	 * JSSE</a>) in case no trust store has been explicitly set.
	 *
	 * @param shouldVerifyHostCert
	 * @return
	 */
	public SslContextBuilder setVerifyHostCert(boolean shouldVerifyHostCert) {
		this.verifyHostCert = shouldVerifyHostCert;
		return this;
	}

	/**
	 * Sets the trust store to use when server authentication is requested (via
	 * {@link #setVerifyHostCert(boolean)}). If <code>null</code> is specified
	 * and server authentication is requested, the server certificate according
	 * to the default trust store configured with the JVM (see the guide on
	 * <a href=
	 * "http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html#CustomizingStores">
	 * JSSE</a>).
	 *
	 * @param trustStore
	 *            The trust store to use for server cert verification. May be
	 *            <code>null</code>, which means rely on default trust store.
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	public SslContextBuilder serverAuthTrustStore(KeyStore trustStore)
			throws NoSuchAlgorithmException, KeyStoreException {
		this.trustStore = Optional.fromNullable(trustStore);
		return this;
	}

	private static TrustManager[] trustManagerFromTrustStore(
			KeyStore trustStore)
					throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);
		return trustManagerFactory.getTrustManagers();
	}

}
