package com.elastisys.scale.commons.net.ssl;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.ssl.TrustStrategy;

public class SslUtils {
	/**
	 * Creates a promiscuous {@link HostnameVerifier} that accepts all host
	 * names without further verification.
	 *
	 * @return
	 */
	public static HostnameVerifier allowAllHostNames() {
		// Install host name verifier that always approves host names
		HostnameVerifier alwaysAllowHostVerifier = (hostname, session) -> true;
		return alwaysAllowHostVerifier;
	}

	/**
	 * Creates an all-trusting {@link SSLContext} that trusts all server
	 * certificates it is presented with. The created {@link SSLContext} will
	 * not attempt to authenticate the client to the server.
	 * <p/>
	 * The resulting {@link SSLContext} is similar to to using the
	 * <code>--insecure</code> flag in <code>curl</code>.
	 *
	 * @see SslContextBuilder
	 * @return
	 */
	public static SSLContext trustAllCertsSslContext() {
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager trustAllCerts = insecureTrustManager();

			// Install the all-trusting trust manager
			SSLContext trustAllCertsSslContext = SSLContext.getInstance("TLS");
			trustAllCertsSslContext.init(new KeyManager[0],
					new TrustManager[] { trustAllCerts }, new SecureRandom());

			return trustAllCertsSslContext;
		} catch (Exception e) {
			throw new RuntimeException("failed to create an insecure "
					+ "(trust-all-certs) ssl context: " + e.getMessage(), e);
		}
	}

	/**
	 * Creates a {@link TrustManager} that trusts all server certificates it is
	 * presented with. That is, all certificate chain/host identity checks are
	 * disabled.
	 * <p/>
	 * This is similar to using the <code>--insecure</code> flag in
	 * <code>curl</code>.
	 *
	 * @return
	 */
	public static TrustManager insecureTrustManager() {
		TrustManager trustAllCerts = new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
			}
		};
		return trustAllCerts;
	}

	/**
	 * Returns a {@link TrustStrategy} that trusts <i>any</i> server
	 * certificate. That is, the server peer will not be verified, which is
	 * similar to using the {@code --insecure} flag in {@code curl}.
	 *
	 * @return
	 */
	public static TrustStrategy insecureTrustStrategy() {
		return (chain, authType) -> true;
	}
}
