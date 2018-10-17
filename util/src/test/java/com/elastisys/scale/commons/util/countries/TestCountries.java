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
        for (Countries country : Countries.values()) {
            assertThat(Countries.countryExists(country.getCountryName()), is(true));
        }
    }

    @Test
    public void countryExistsOnNonExistingCountry() {
        assertThat(Countries.countryExists("soviet union"), is(false));
    }

    @Test
    public void countryExistsCaseInsensitive() {
        // should be case insensitive
        assertThat(Countries.countryExists("sweden"), is(true));
        assertThat(Countries.countryExists("Sweden"), is(true));
        assertThat(Countries.countryExists("SwEdeN"), is(true));
    }
}
