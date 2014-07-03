package com.elastisys.scale.commons.util.time;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Verify behavior of {@link TimeUtils} class.
 * 
 * 
 */
public class TestTimeUtils {

	@Test
	public void testEqual() {
		DateTime utcInstant = DateTime.parse("2013-01-01T10:00:00.000Z");
		DateTime cetInstant = DateTime.parse("2013-01-01T11:00:00.000+01:00");
		DateTime eetInstant = DateTime.parse("2013-01-01T12:00:00.000+02:00");

		assertTrue(TimeUtils.equal(null, null));
		assertTrue(TimeUtils.equal(utcInstant, utcInstant));
		assertTrue(TimeUtils.equal(cetInstant, cetInstant));
		assertTrue(TimeUtils.equal(eetInstant, eetInstant));

		assertTrue(TimeUtils.equal(utcInstant, cetInstant));
		assertTrue(TimeUtils.equal(cetInstant, eetInstant));
		assertTrue(TimeUtils.equal(utcInstant, eetInstant));

		assertFalse(TimeUtils.equal(null, utcInstant));
		assertFalse(TimeUtils.equal(utcInstant, null));
	}
}
