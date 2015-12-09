package com.elastisys.scale.commons.util.countries;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Exercises the {@link Countries} class.
 */
public class TestCountries {

	@Test
	public void countryExistsRecognizesAllExistingCountries() {
		System.out.print("[");
		for (Countries country : Countries.values()) {
			assertThat(Countries.countryExists(country.getCountryName()),
					is(true));
			System.out.print("\"" + country.getCountryName() + "\", ");
		}
		System.out.println("]");
	}

	@Test
	public void countryExistsOnNonExistingCountry() {
		assertThat(Countries.countryExists("soviet union"), is(false));
	}

	@Test
	public void countryExistsCaseInsensitive() {
		// should be case
		assertThat(Countries.countryExists("sweden"), is(true));
		assertThat(Countries.countryExists("Sweden"), is(true));

		assertThat(Countries.countryExists("united STATES"), is(true));
		assertThat(Countries.countryExists("united STATES"), is(true));

		assertThat(Countries.countryExists("the democratic Republic of Congo"),
				is(true));

	}
}
