package com.elastisys.scale.commons.net.ssl;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * A builder class that can be used to construct {@link SSLContext}s for SSL
 * clients that require different combinations of (1) client certificate
 * authentication and (2) server certificate authentication.
 * <p/>
 * The builder allows for optional client authentication, with a certificate
 * from a {@link KeyStore}, as well as optional authentication of the server
 * certificate against a trust store.
 * 
 * 
 * 
 */
public class SslContextBuilder {

	/**
	 * {@link KeyManagerFactory} acting as the source of authentication keys in
	 * case client certificate authentication is requested.
	 */
	private Optional<KeyManagerFactory> keyManagerFactory = Optional.absent();
	/**
	 * {@link TrustManager}s acting as the sources of peer authentication trust
	 * decisions when server authentication is requested.
	 */
	private Optional<List<TrustManager>> trustManagers = Optional.absent();

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

			// add server certificate authentication if specified, otherwise
			// all server certificates are trusted
			TrustManager[] trustManagers = new TrustManager[] { SslUtils
					.insecureTrustManager() };
			if (this.trustManagers.isPresent()) {
				trustManagers = this.trustManagers.get().toArray(
						new TrustManager[0]);
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
	 * @param password
	 *            The client key password.
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 */
	public SslContextBuilder clientAuthentication(KeyStore keyStore,
			String password) throws NoSuchAlgorithmException,
			UnrecoverableKeyException, KeyStoreException {
		KeyManagerFactory keyManagerFactory = KeyManagerFactory
				.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, password.toCharArray());
		this.keyManagerFactory = Optional.of(keyManagerFactory);
		return this;
	}

	/**
	 * The {@link SSLContext} should not perform server certificate
	 * authentication (trust all server certificates and don't perform host name
	 * verification).
	 * <p/>
	 * This is similar to using the {@code --insecure} flag in {@code curl}.
	 * 
	 * @return
	 */
	public SslContextBuilder noServerAuthentication() {
		List<TrustManager> insecureTrustManager = Lists.newArrayList(SslUtils
				.insecureTrustManager());
		this.trustManagers = Optional.of(insecureTrustManager);
		return this;
	}

	/**
	 * The {@link SSLContext} should authenticate the server certificate against
	 * the certificates in a trust store.
	 * 
	 * @param trustStore
	 *            The client's trust store, which contains certificates from
	 *            other parties that the client trusts, or from Certificate
	 *            Authorities that are trusted to identify other parties.
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	public SslContextBuilder serverAuthentication(KeyStore trustStore)
			throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);
		TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		List<TrustManager> trustManagerList = Lists.newArrayList(trustManagers);
		this.trustManagers = Optional.of(trustManagerList);
		return this;
	}

}
