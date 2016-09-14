package com.elastisys.scale.commons.util.usstates;

import java.util.HashSet;
import java.util.Set;

/**
 * Enumeration of all US states.
 */
public enum UsStates {
    /** State of Alabama. */
    ALABAMA("Alabama"),
    /** State of Alaska. */
    ALASKA("Alaska"),
    /** State of Arizona. */
    ARIZONA("Arizona"),
    /** State of Arkansas. */
    ARKANSAS("Arkansas"),
    /** State of California. */
    CALIFORNIA("California"),
    /** State of Colorado. */
    COLORADO("Colorado"),
    /** State of Connecticut. */
    CONNECTICUT("Connecticut"),
    /** State of Delaware. */
    DELAWARE("Delaware"),
    /** State of District Of Columbia. */
    DISTRICT_OF_COLUMBIA("District Of Columbia"),
    /** State of Florida. */
    FLORIDA("Florida"),
    /** State of Georgia. */
    GEORGIA("Georgia"),
    /** State of Hawaii. */
    HAWAII("Hawaii"),
    /** State of Idaho. */
    IDAHO("Idaho"),
    /** State of Illinois. */
    ILLINOIS("Illinois"),
    /** State of Indiana. */
    INDIANA("Indiana"),
    /** State of Iowa. */
    IOWA("Iowa"),
    /** State of Kansas. */
    KANSAS("Kansas"),
    /** State of Kentucky. */
    KENTUCKY("Kentucky"),
    /** State of Louisiana. */
    LOUISIANA("Louisiana"),
    /** State of Maine. */
    MAINE("Maine"),
    /** State of Maryland. */
    MARYLAND("Maryland"),
    /** State of Massachusetts. */
    MASSACHUSETTS("Massachusetts"),
    /** State of Michigan. */
    MICHIGAN("Michigan"),
    /** State of Minnesota. */
    MINNESOTA("Minnesota"),
    /** State of Mississippi. */
    MISSISSIPPI("Mississippi"),
    /** State of Missouri. */
    MISSOURI("Missouri"),
    /** State of Montana. */
    MONTANA("Montana"),
    /** State of Nebraska. */
    NEBRASKA("Nebraska"),
    /** State of Nevada. */
    NEVADA("Nevada"),
    /** State of New Hampshire. */
    NEW_HAMPSHIRE("New Hampshire"),
    /** State of New Jersey. */
    NEW_JERSEY("New Jersey"),
    /** State of New Mexico. */
    NEW_MEXICO("New Mexico"),
    /** State of New York. */
    NEW_YORK("New York"),
    /** State of North Carolina. */
    NORTH_CAROLINA("North Carolina"),
    /** State of North Dakota. */
    NORTH_DAKOTA("North Dakota"),
    /** State of Ohio. */
    OHIO("Ohio"),
    /** State of Oklahoma. */
    OKLAHOMA("Oklahoma"),
    /** State of Oregon. */
    OREGON("Oregon"),
    /** State of Pennsylvania. */
    PENNSYLVANIA("Pennsylvania"),
    /** State of Rhode Island. */
    RHODE_ISLAND("Rhode Island"),
    /** State of South Carolina. */
    SOUTH_CAROLINA("South Carolina"),
    /** State of South Dakota. */
    SOUTH_DAKOTA("South Dakota"),
    /** State of Tennessee. */
    TENNESSEE("Tennessee"),
    /** State of Texas. */
    TEXAS("Texas"),
    /** State of Utah. */
    UTAH("Utah"),
    /** State of Vermont. */
    VERMONT("Vermont"),
    /** State of Virginia. */
    VIRGINIA("Virginia"),
    /** State of Washington. */
    WASHINGTON("Washington"),
    /** State of West Virginia. */
    WEST_VIRGINIA("West Virginia"),
    /** State of Wisconsin. */
    WISCONSIN("Wisconsin"),
    /** State of Wyoming. */
    WYOMING("Wyoming");

    /** Holds the names of all {@link UsStates}. */
    private static final Set<String> US_STATES = new HashSet<>();

    static {
        // populate US_STATES
        for (UsStates state : UsStates.values()) {
            US_STATES.add(state.getStateName().toLowerCase());
        }
    }

    /** The state name (such as "North Carolina"). */
    private final String stateName;

    private UsStates(String stateName) {
        this.stateName = stateName;
    }

    /**
     * Returns the state name (such as "North Carolina").
     *
     * @return
     */
    public String getStateName() {
        return this.stateName;
    }

    /**
     * Determines if the given string is the name of a US state. The comparison
     * is case insensitive.
     *
     * @param stateName
     *            The name of a state. Such as 'Alabama' or 'north carolina'.
     * @return <code>true</code> if the country exists, <code>false</code>
     *         otherwise.
     */
    public static boolean stateExists(String stateName) {
        return US_STATES.contains(stateName.toLowerCase());
    }
}
