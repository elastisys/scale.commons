package com.elastisys.scale.commons.util.time;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Exercises the {@link FrozenTime} class.
 * 
 * 
 * 
 */
public class TestFrozenTime {

	@Before
	public void onSetup() {
		// follow system clock
		DateTimeUtils.setCurrentMillisSystem();
	}

	/**
	 * Verify that {@link FrozenTime#setFixed(DateTime)} actually freezes the
	 * current time reported by {@link DateTime} at a certain instant.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void setFixed() throws InterruptedException {
		// verify that clock follows wall clock
		DateTime t1 = UtcTime.now();
		Thread.sleep(10);
		DateTime t2 = UtcTime.now();
		assertTrue(t2.isAfter(t1));

		// set fixed time
		FrozenTime.setFixed(t1);
		assertThat(UtcTime.now(), is(t1));
		// current time should now be "frozen" at the set time
		Thread.sleep(10);
		assertThat(UtcTime.now(), is(t1));
	}

	/**
	 * Verifies the proper operation of the {@link FrozenTime#tick()} method,
	 * which should advance the notion of current time and freeze the clock.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void tick() throws InterruptedException {
		// freeze time
		DateTime t1 = UtcTime.now();
		FrozenTime.setFixed(t1);
		Thread.sleep(10);
		// current time should now be "frozen" at the set time
		assertThat(UtcTime.now(), is(t1));

		// tick
		FrozenTime.tick();
		Thread.sleep(10);
		// current time should have advanced one second and still be frozen
		assertThat(UtcTime.now(), is(t1.plusSeconds(1)));

		// tick
		FrozenTime.tick(20);
		Thread.sleep(10);
		// current time should have advanced twenty seconds and still be frozen
		assertThat(UtcTime.now(), is(t1.plusSeconds(21)));
	}

	/**
	 * Verify that {@link FrozenTime#resumeSystemTime()} actually resumes wall
	 * clock time for a frozen {@link DateTime}.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testResumeSystemTime() throws InterruptedException {
		// freeze time
		DateTime t1 = UtcTime.now();
		FrozenTime.setFixed(t1);
		Thread.sleep(10);
		// current time should now be "frozen" at the set time
		assertThat(UtcTime.now(), is(t1));

		// resume following wall clock time
		FrozenTime.resumeSystemTime();
		assertTrue(UtcTime.now().isAfter(t1));
	}

}
