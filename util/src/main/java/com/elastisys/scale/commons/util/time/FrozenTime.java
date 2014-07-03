package com.elastisys.scale.commons.util.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

/**
 * A utility class that allows the application's notion of current time (as
 * returned by the Joda {@link DateTime} class) to be manipulated, for example,
 * by freezing the current time to a particular time-instant or advancing time
 * in a controlled manner.
 * <p/>
 * Controlling the current time is useful for tests/simulation when the
 * application's notion of current time needs to be controlled/manipulated.
 * 
 * 
 * 
 */
public class FrozenTime {
	/**
	 * Returns the current time as an UTC timestamp.
	 * 
	 * @return The current UTC time.
	 */
	public static DateTime now() {
		return UtcTime.now();
	}

	/**
	 * Sets the current time (as returned by the {@link DateTime}) to a fixed
	 * time instant. All subsequent requests for the current time to
	 * {@link DateTime} will return this time instant (until
	 * {@link #resumeSystemTime()} is invoked).
	 * <p/>
	 * Note: the system clock remains unaffected by this change.
	 * 
	 * @param timeInstant
	 * @return
	 */
	public static void setFixed(DateTime timeInstant) {
		DateTimeUtils.setCurrentMillisFixed(timeInstant.getMillis());
	}

	/**
	 * Advances the current time (as returned by the {@link DateTime}) by one
	 * second. All subsequent requests for the current time to {@link DateTime}
	 * will return the resulting time instant (until {@link #resumeSystemTime()}
	 * is invoked).
	 * <p/>
	 * Note: the system clock remains unaffected by this change.
	 * 
	 */
	public static void tick() {
		tick(1);
	}

	/**
	 * Advances the current time (as returned by the {@link DateTime}) by a
	 * specified number of seconds. All subsequent requests for the current time
	 * to {@link DateTime} will return the resulting time instant (until
	 * {@link #resumeSystemTime()} is invoked).
	 * <p/>
	 * Note: the system clock remains unaffected by this change.
	 * 
	 * 
	 * @param seconds
	 */
	public static void tick(int seconds) {
		setFixed(now().plusSeconds(seconds));
	}

	/**
	 * (Re)sets the current time to follow the system clock.
	 */
	public static void resumeSystemTime() {
		DateTimeUtils.setCurrentMillisSystem();
	}

}
