package com.elastisys.scale.commons.net.alerter.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.elastisys.scale.commons.net.ssl.BasicCredentials;

/**
 * Verifies the {@link HttpAlerterConfig} class.
 */
public class TestHttpAlerterConfig {

	/**
	 * Make sure given values are remembered.
	 */
	@Test
	public void basicSanity() {
		List<String> urls = Arrays.asList("https://some.host/");
		String severityFilter = "INFO|WARN|ERROR";
		HttpAuthConfig auth = new HttpAuthConfig(
				new BasicCredentials("user", "secret"), null);
		int connectionTimeout = 1000;
		int socketTimeout = 1000;

		HttpAlerterConfig config = new HttpAlerterConfig(urls, severityFilter,
				auth, connectionTimeout, socketTimeout);
		assertThat(config.getDestinationUrls(), is(urls));
		assertThat(config.getSeverityFilter().getFilterExpression(),
				is(severityFilter));
		assertThat(config.getAuth(), is(auth));
		assertThat(config.getConnectTimeout(), is(connectionTimeout));
		assertThat(config.getSocketTimeout(), is(socketTimeout));
	}

	/**
	 * Make sure that fields with default values can be left out
	 */
	@Test
	public void createWhenRelyingOnDefaults() {
		List<String> urls = Arrays.asList("https://some.host/");
		// use defaults for severity filter (".*"), auth (no auth), and timeouts
		String severityFilter = null;
		HttpAuthConfig auth = null;
		Integer connectionTimeout = null;
		Integer socketTimeout = null;
		HttpAlerterConfig config = new HttpAlerterConfig(urls, severityFilter,
				auth, connectionTimeout, socketTimeout);

		assertThat(config.getDestinationUrls(), is(urls));
		assertThat(config.getSeverityFilter().getFilterExpression(),
				is(HttpAlerterConfig.DEFAULT_SEVERITY_FILTER));
		assertThat(config.getAuth().getBasicCredentials().isPresent(),
				is(false));
		assertThat(config.getAuth().getCertificateCredentials().isPresent(),
				is(false));
		assertThat(config.getConnectTimeout(),
				is(HttpAlerterConfig.DEFAULT_CONNECTION_TIMEOUT));
		assertThat(config.getSocketTimeout(),
				is(HttpAlerterConfig.DEFAULT_SOCKET_TIMEOUT));
	}

	@Test(expected = IllegalArgumentException.class)
	public void withMissingUrls() {
		String severityFilter = null;
		HttpAuthConfig auth = new HttpAuthConfig(
				new BasicCredentials("user", "secret"), null);
		new HttpAlerterConfig(null, severityFilter, auth);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalUrls() {
		List<String> urls = Arrays.asList("tcp://1.2.3.4");
		String severityFilter = null;
		HttpAuthConfig auth = new HttpAuthConfig(
				new BasicCredentials("user", "secret"), null);
		new HttpAlerterConfig(urls, severityFilter, auth);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalSeverityFilter() {
		List<String> urls = Arrays.asList("http://some.host/");
		String severityFilter = "+INFO|WARN";
		HttpAuthConfig auth = new HttpAuthConfig(
				new BasicCredentials("user", "secret"), null);

		new HttpAlerterConfig(urls, severityFilter, auth);
	}
}
