package com.elastisys.scale.commons.util.countries;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for dealing with countries of the world.
 */
public class Countries {
	/**
	 * Tracks all known countries keyed by country display name in lower case
	 * (such as 'united states') and mapped to a {@link Locale} for that
	 * country.
	 */
	private final static Map<String, Locale> COUNTRIES = new HashMap<>();

	static {
		String[] countryCodes = Locale.getISOCountries();
		for (String countryCode : countryCodes) {
			Locale locale = new Locale.Builder().setRegion(countryCode).build();
			COUNTRIES.put(locale.getDisplayCountry().toLowerCase(), locale);
		}
	}

	/**
	 * Determines if the given string is the name of a country in the world. The
	 * method recognizes countries for which there exists a Java {@link Locale}.
	 * The comparison is case insensitive.
	 *
	 * @param countryDisplayName
	 *            The display name of the country. Such as 'Sweden' or 'United
	 *            States'.
	 * @return <code>true</code> if the country exists, <code>false</code>
	 *         otherwise.
	 */
	public static boolean countryExists(String countryDisplayName) {
		return COUNTRIES.containsKey(countryDisplayName.toLowerCase());
	}
}
