package com.elastisys.scale.commons.util.usstates;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.elastisys.scale.commons.util.countries.Countries;

/**
 * Exercises the {@link UsStates} class.
 */
public class TestUsStates {

    @Test
    public void countryExistsRecognizesAllExistingCountries() {
        System.out.print("[");
        for (UsStates state : UsStates.values()) {
            assertThat(UsStates.stateExists(state.getStateName()), is(true));
            System.out.print("\"" + state.getStateName() + "\", ");
        }
        System.out.println("]");
    }

    @Test
    public void stateExistsOnNonExistingState() {
        assertThat(Countries.countryExists("state of confusion"), is(false));
    }

    @Test
    public void stateExistsCaseInsensitive() {
        // should be case insensitive
        assertThat(UsStates.stateExists("north carolina"), is(true));
        assertThat(UsStates.stateExists("North Carolina"), is(true));
        assertThat(UsStates.stateExists("north CAROLINA"), is(true));
        assertThat(UsStates.stateExists("NORTH carolina"), is(true));
    }
}
