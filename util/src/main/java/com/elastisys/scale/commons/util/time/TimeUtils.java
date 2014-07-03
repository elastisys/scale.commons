package com.elastisys.scale.commons.util.time;

import org.joda.time.DateTime;

/**
 * Convenience methods for working with time.
 * 
 * 
 */
public class TimeUtils {

	/**
	 * Null-safe comparison that compares if two {@link DateTime} instances
	 * represent the same time instant. In the case of two non-<code>null</code>
	 * objects, the semantics is the same as for {@link DateTime#isEqual(long)}.
	 * That is, the comparison is based solely on milliseconds, which allows
	 * timestamps with different time zones to be correctly compared (which is
	 * not the case with {@link DateTime#equals(Object)}).
	 * 
	 * @param instant1
	 *            First {@link DateTime} instance. May be <code>null</code>.
	 * @param instant2
	 *            Second {@link DateTime} instance. May be <code>null</code>.
	 * @return <code>true</code> if the {@link DateTime} instances represent the
	 *         same time instant.
	 * 
	 */
	public static boolean equal(DateTime instant1, DateTime instant2) {
		return (instant1 == instant2)
				|| ((instant1 != null) && instant1.isEqual(instant2));
	}
}
