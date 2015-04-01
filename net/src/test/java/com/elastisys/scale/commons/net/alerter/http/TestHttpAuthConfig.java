package com.elastisys.scale.commons.net.alerter.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.elastisys.scale.commons.net.ssl.BasicCredentials;
import com.elastisys.scale.commons.net.ssl.CertificateCredentials;

/**
 * Tests the {@link HttpAuthConfig} class.
 */
public class TestHttpAuthConfig {
	private static final String KEYSTORE = "src/test/resources/security/server/server_keystore.p12";
	private static final String KEYSTORE_PASSWORD = "serverpassword";

	@Test
	public void basicSanity() {
		BasicCredentials basicCredentials = new BasicCredentials("user",
				"secret");

		CertificateCredentials certificateCredentials = new CertificateCredentials(
				KEYSTORE, KEYSTORE_PASSWORD);

		// no auth
		HttpAuthConfig noAuth = new HttpAuthConfig(null, null);
		assertThat(noAuth.getBasicCredentials().isPresent(), is(false));
		assertThat(noAuth.getCertificateCredentials().isPresent(), is(false));

		// basic auth
		HttpAuthConfig basicAuth = new HttpAuthConfig(basicCredentials, null);
		assertThat(basicAuth.getBasicCredentials().isPresent(), is(true));
		assertThat(basicAuth.getCertificateCredentials().isPresent(), is(false));
		assertThat(basicAuth.getBasicCredentials().get(), is(basicCredentials));

		// cert auth
		HttpAuthConfig certAuth = new HttpAuthConfig(null, certificateCredentials);
		assertThat(certAuth.getBasicCredentials().isPresent(), is(false));
		assertThat(certAuth.getCertificateCredentials().isPresent(), is(true));
		assertThat(certAuth.getCertificateCredentials().get(),
				is(certificateCredentials));

		// basic + cert auth
		HttpAuthConfig basicAndCertAuth = new HttpAuthConfig(basicCredentials,
				certificateCredentials);
		assertThat(basicAndCertAuth.getBasicCredentials().isPresent(), is(true));
		assertThat(basicAndCertAuth.getCertificateCredentials().isPresent(),
				is(true));
		assertThat(basicAndCertAuth.getBasicCredentials().get(),
				is(basicCredentials));
		assertThat(basicAndCertAuth.getCertificateCredentials().get(),
				is(certificateCredentials));

	}

}
