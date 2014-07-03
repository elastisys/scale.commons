package com.elastisys.scale.commons.server;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.ws.rs.client.Client;

import org.glassfish.jersey.filter.LoggingFilter;

import com.elastisys.scale.commons.net.ssl.KeyStoreType;
import com.elastisys.scale.commons.rest.client.RestClients;
import com.google.common.base.Throwables;

/**
 * Utility class for Jersey/REST unit tests. Adds a {@link LoggingFilter} to the
 * {@link Client}s produced by the {@link RestClients} factory class.
 * 
 * 
 * 
 */
public class RestTestUtils {

	public static Client httpBasicAuth(String userName, String password) {
		Client client = RestClients.httpBasicAuth(userName, password);
		client.register(new LoggingFilter());
		return client;
	}

	public static Client httpNoAuth() {
		Client client = RestClients.httpNoAuth();
		client.register(new LoggingFilter());
		return client;
	}

	public static Client httpsBasicAuth(String userName, String password) {
		Client client = RestClients.httpsBasicAuth(userName, password);
		client.register(new LoggingFilter());
		return client;
	}

	public static Client httpsNoAuth() {
		Client client = RestClients.httpsNoAuth();
		client.register(new LoggingFilter());
		return client;
	}

	public static Client httpsCertAuth(String keyStorePath,
			String keyStorePassword, KeyStoreType keystoreType)
			throws RuntimeException {
		try (InputStream keyStoreStream = new FileInputStream(keyStorePath)) {
			KeyStore keystore = KeyStore.getInstance(keystoreType.name());
			keystore.load(keyStoreStream, keyStorePassword.toCharArray());
			Client client = RestClients.httpsCertAuth(keystore,
					keyStorePassword);
			client.register(new LoggingFilter());
			return client;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

}
