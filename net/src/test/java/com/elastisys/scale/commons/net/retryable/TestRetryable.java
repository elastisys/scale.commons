package com.elastisys.scale.commons.net.retryable;

import static com.elastisys.scale.commons.net.retryable.DelayStrategies.exponentialBackoff;
import static com.elastisys.scale.commons.net.retryable.DelayStrategies.fixed;
import static com.elastisys.scale.commons.net.retryable.StopStrategies.afterAttempts;
import static com.elastisys.scale.commons.net.retryable.StopStrategies.afterTime;
import static com.google.common.base.Predicates.equalTo;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.Callables;

/**
 * Verifies the behavior of the {@link Retryable} and its interaction with the
 * different strategies for determining successful responses, how long to wait
 * between attempts, when to give up, and what errors to suppress.
 */
public class TestRetryable {

	@Before
	public void beforeTestMethod() {

	}

	/**
	 * Test defaults (retry once only) on success.
	 */
	@Test
	public void testWithDefaultsOnSuccess() throws Exception {
		Callable<String> task = Callables.returning("hello world");

		Retryable<String> retryable = new Retryable<>(task);
		String result = retryable.call();
		assertThat(result, is("hello world"));
		assertThat(retryable.getAttempts(), is(1));
	}

	/**
	 * Test defaults (retry once only) on error.
	 */
	@Test
	public void testWithDefaultsOnError() throws Exception {
		Exception fault = new RuntimeException("task failed!");
		Callable<String> task = new FailNTimesAndReturn(5, fault, "hello world");

		Retryable<String> retryable = new Retryable<>(task);
		try {
			retryable.call();
			fail("retryable was expected to fail");
		} catch (Exception e) {
			assertThat(e, is(fault));
		}
		assertThat(retryable.getAttempts(), is(1));
	}

	/**
	 * Test with exception suppression turned on.
	 */
	@Test
	public void testWithErrorSuppression() throws Exception {
		Exception fault = new RuntimeException("task failure!");
		Callable<String> task = new FailNTimesAndReturn(5, fault, "hello world");

		// suppress Exception and sub-classes of it
		Retryable<String> retryable = new Retryable<>(task)
				.retryOnError(Exception.class);
		String result = retryable.call();
		assertThat(result, is("hello world"));
		assertThat(retryable.getAttempts(), is(6));

		// using retryOnException() should be equivalent
		task = new FailNTimesAndReturn(5, fault, "hello world");
		retryable = new Retryable<>(task).retryOnException();
		result = retryable.call();
		assertThat(result, is("hello world"));
		assertThat(retryable.getAttempts(), is(6));

		// using retryOnException() should also work fine in this case
		task = new FailNTimesAndReturn(5, fault, "hello world");
		retryable = new Retryable<>(task).retryOnRuntimeException();
		result = retryable.call();
		assertThat(result, is("hello world"));
		assertThat(retryable.getAttempts(), is(6));
	}

	/**
	 * If the suppressed error does not match the raised error, the task should
	 * fail.
	 */
	@Test(expected = RuntimeException.class)
	public void testWithErrorSuppressionThatDoesNotMatchException()
			throws Exception {
		Exception fault = new RuntimeException("task failed!");
		Callable<String> task = new FailNTimesAndReturn(5, fault, "hello world");

		// no IllegalArgumentException will be thrown
		Retryable<String> retryable = new Retryable<>(task)
				.retryOnError(IllegalArgumentException.class);
		retryable.call();
	}

	@Test
	public void testWithResponsePredicate() throws Exception {
		Callable<Integer> task = new Counter();

		// will succeed on first attempt
		Retryable<Integer> retryable = new Retryable<>(task)
				.retryUntilResponse(equalTo(1));
		assertThat(retryable.call(), is(1));
		assertThat(retryable.getAttempts(), is(1));

		// needs to retry a few times
		task = new Counter();
		retryable = new Retryable<>(task).retryUntilResponse(equalTo(45));
		assertThat(retryable.call(), is(45));
		assertThat(retryable.getAttempts(), is(45));
	}

	@Test
	public void testWithResponsePredicateAndErrorSuppression() throws Exception {
		IllegalStateException fault = new IllegalStateException(
				"cannot handle low numbers!");

		// should raise 10 errors before starting to return count values
		Callable<Integer> task = new FailNTimesCounter(10, fault);
		Retryable<Integer> retryable = new Retryable<>(task).retryOnException()
				.retryUntilResponse(equalTo(15));
		assertThat(retryable.call(), is(15));
		assertThat(retryable.getAttempts(), is(15));
	}

	@Test
	public void testWithStopStrategy() throws Exception {
		IllegalStateException fault = new IllegalStateException(
				"cannot handle low numbers!");

		// stop strategy that allows us to reach the value
		Callable<Integer> task = new FailNTimesCounter(10, fault);
		Retryable<Integer> retryable = new Retryable<>(task).retryOnException()
				.retryUntilResponse(equalTo(15)).stop(afterAttempts(15));
		assertThat(retryable.call(), is(15));
		assertThat(retryable.getAttempts(), is(15));

		// stop strategy that quits after only 1 attempt
		task = new FailNTimesCounter(10, fault);
		retryable = new Retryable<>(task).retryOnException()
				.retryUntilResponse(equalTo(15)).stop(afterAttempts(1));
		try {
			retryable.call();
		} catch (GaveUpException e) {
			// expected
			assertEquals(e.getCause(), fault);
		}
		assertThat(retryable.getAttempts(), is(1));

		// stop strategy that quits after only 10 attempts
		task = new FailNTimesCounter(10, fault);
		retryable = new Retryable<>(task).retryOnException()
				.retryUntilResponse(equalTo(15)).stop(afterAttempts(10));
		try {
			retryable.call();
		} catch (GaveUpException e) {
			// expected
			// last result was an exception so it should be included in the
			// exception
			assertEquals(e.getCause(), fault);
		}
		assertThat(retryable.getAttempts(), is(10));

		// test when last attempt did not produce an exception
		task = new Counter();
		retryable = new Retryable<>(task).retryOnException()
				.retryUntilResponse(equalTo(15)).stop(afterAttempts(10));
		try {
			retryable.call();
		} catch (GaveUpException e) {
			// expected
			// last attempt returned a value so cause should be empty
			assertThat(e.getCause(), is(nullValue()));
		}
		assertThat(retryable.getAttempts(), is(10));
	}

	@Test
	public void testWithDelayStrategy() throws Exception {
		// delay 20 ms between each new attempt, but only one attempt needed so
		// no delay should have been introduced
		Callable<Integer> task = new Counter();
		Retryable<Integer> retryable = new Retryable<>(task)
				.retryUntilResponse(equalTo(1)).delay(fixed(20, MILLISECONDS));
		assertThat(retryable.call(), is(1));
		assertThat(retryable.getAttempts(), is(1));
		assertTrue(retryable.getTimer().elapsed(MILLISECONDS) < 20);

		// delay 20 ms between each new attempt, 2 attempts needed so a 20 ms
		// delay should have been introduced
		task = new Counter();
		retryable = new Retryable<>(task).retryUntilResponse(equalTo(2)).delay(
				fixed(20, MILLISECONDS));
		assertThat(retryable.call(), is(2));
		assertThat(retryable.getAttempts(), is(2));
		// delay should be at least 20 ms, but not too much higher
		assertTrue(Range.closed(20L, 30L).contains(
				retryable.getTimer().elapsed(MILLISECONDS)));

		// delay 20 ms between each new attempt, 4 attempts needed so a 60 ms
		// delay should have been introduced
		task = new Counter();
		retryable = new Retryable<>(task).retryUntilResponse(equalTo(4)).delay(
				fixed(20, MILLISECONDS));
		assertThat(retryable.call(), is(4));
		assertThat(retryable.getAttempts(), is(4));
		// delay should be at least 20 ms, but not too much higher
		assertTrue(Range.closed(60L, 70L).contains(
				retryable.getTimer().elapsed(MILLISECONDS)));
	}

	/**
	 * Setting the name of the retryable task should have no functional impact,
	 * but we try it anyway to make sure it doesn't cause any problems.
	 */
	@Test
	public void testWithName() throws Exception {
		Callable<Integer> task = new Counter();
		Retryable<Integer> retryable = new Retryable<>(task)
				.retryUntilResponse(equalTo(10)).name("await-counter=10");
		assertThat(retryable.call(), is(10));
		assertThat(retryable.getAttempts(), is(10));
	}

	/**
	 * Combines a {@link StopStrategy}, a {@link DelayStrategy}, a response
	 * {@link Predicate} and error suppression.
	 */
	@Test
	public void testCombinationWithExponentialBackoff() throws Exception {
		Exception fault = new IllegalStateException(
				"can't handle too small values!");
		Callable<Integer> task = new FailNTimesCounter(5, fault);
		Retryable<Integer> retryable = new Retryable<>(task).retryOnException()
				.retryUntilResponse(equalTo(10)).stop(afterTime(2, SECONDS))
				.delay(exponentialBackoff(1, MILLISECONDS))
				.name("backoff-counter");
		assertThat(retryable.call(), is(10));
		assertThat(retryable.getAttempts(), is(10));
		// make sure that the total exponential back-off wait time adds up to
		// the expected value
		// 10 attempts => 9 retry delays => 2^9 - 1 = 511 ms.
		long expectedDelay = (2L << 9 - 1);
		long margin = 40L;
		assertTrue(Range.closed(expectedDelay, expectedDelay + margin)
				.contains(retryable.getTimer().elapsed(MILLISECONDS)));

	}

	/**
	 * A test task that will fail a specified number of times before eventually
	 * producing a successful result.
	 */
	private static class FailNTimesAndReturn implements Callable<String> {
		/** calls thus far. */
		private int attempts = 0;
		/** Number of planned failure attempts before successful return. */
		private int plannedFailures;
		/** Value to return (eventually) */
		private String returnValue;
		/** The exception to raise on failed attempts. */
		private Exception failure;

		public FailNTimesAndReturn(int plannedFailures, Exception failure,
				String returnValue) {
			this.plannedFailures = plannedFailures;
			this.failure = failure;
			this.returnValue = returnValue;
		}

		@Override
		public String call() throws Exception {
			this.attempts++;
			if (this.attempts <= this.plannedFailures) {
				throw this.failure;
			}
			return this.returnValue;
		}
	}

	/**
	 * A test task that is a simple counter.
	 */
	private static class Counter implements Callable<Integer> {
		/** calls thus far. */
		private int attempts = 0;

		@Override
		public Integer call() throws Exception {
			this.attempts++;
			return this.attempts;
		}
	}

	/**
	 * A counter task that will fail a specified number of times before
	 * eventually producing successful results.
	 */
	private static class FailNTimesCounter implements Callable<Integer> {
		/** calls thus far. */
		private int attempts = 0;
		/** Number of planned failure attempts before successful return. */
		private int plannedFailures;
		/** The exception to raise on failed attempts. */
		private Exception failure;

		public FailNTimesCounter(int plannedFailures, Exception failure) {
			this.plannedFailures = plannedFailures;
			this.failure = failure;
		}

		@Override
		public Integer call() throws Exception {
			this.attempts++;
			if (this.attempts <= this.plannedFailures) {
				throw this.failure;
			}
			return this.attempts;
		}
	}

}
