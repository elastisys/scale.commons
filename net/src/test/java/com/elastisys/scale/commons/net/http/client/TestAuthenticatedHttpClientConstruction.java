package com.elastisys.scale.commons.net.http.client;

import org.junit.Test;

import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;
import com.google.common.base.Optional;

/**
 * Tests that exercise the {@link AuthenticatedHttpClient} class' constructor.
 */
public class TestAuthenticatedHttpClientConstruction {

	private final BasicCredentials correctBasicCredentials = new BasicCredentials(
			"admin", "adminpassword");
	private final CertificateCredentials correctCertificateCredentials = new CertificateCredentials(
			"src/test/resources/security/client/client_keystore.p12",
			"clientpass", "clientpass");

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
