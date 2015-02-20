package com.elastisys.scale.commons.net.http.client;

import org.junit.Test;

import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;
import com.elastisys.scale.commons.net.ssl.KeyStoreType;
import com.google.common.base.Optional;

/**
 * Tests that exercise the {@link AuthenticatedHttpClient} class' constructor.
 */
public class TestAuthenticatedHttpClientConstruction {

	private final BasicCredentials correctBasicCredentials = new BasicCredentials(
			"admin", "adminpassword");
	private final CertificateCredentials correctCertificateCredentials = new CertificateCredentials(
			KeyStoreType.PKCS12, "/proc/cpuinfo", "somepassword");

	@Test
	public void basicAndCertCredentials() {
		new AuthenticatedHttpClient(Optional.of(this.correctBasicCredentials),
				Optional.of(this.correctCertificateCredentials));
	}

	@Test
	public void basicCredentialsOnly() {
		new AuthenticatedHttpClient(Optional.of(this.correctBasicCredentials),
				Optional.<CertificateCredentials> absent());
	}

	@Test
	public void certificateCredentialsOnly() {
		new AuthenticatedHttpClient(Optional.<BasicCredentials> absent(),
				Optional.of(this.correctCertificateCredentials));
	}

	@Test
	public void noAuthentication() {
		// test both constructors which are equivalent
		new AuthenticatedHttpClient();
		new AuthenticatedHttpClient(Optional.<BasicCredentials> absent(),
				Optional.<CertificateCredentials> absent());
	}
}
