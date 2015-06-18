package com.elastisys.scale.commons.net.validate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

/**
 * A {@link Predicate} that checks if a given URL string is a valid web
 * {@link URL} (http or https).
 */
public class ValidHttpUrl implements Predicate<String> {

	private static final Collection<String> ALLOWED_PROTOCOLS = ImmutableSet
			.of("http", "https");

	/**
	 * Validates a given URL for correctness.
	 *
	 * @param url
	 * @return
	 */
	public static boolean validate(String url) {
		return new ValidHttpUrl().apply(url);
	}

	@Override
	public boolean apply(String url) {
		try {
			URL urlInstance = new URL(url);
			return ALLOWED_PROTOCOLS.contains(urlInstance.getProtocol());
		} catch (MalformedURLException e) {
			return false;
		}
	}
}
