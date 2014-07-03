package com.elastisys.scale.commons.net.retryable.retryhandlers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.elastisys.scale.commons.net.retryable.Action;
import com.elastisys.scale.commons.net.retryable.RetryHandler;
import com.elastisys.scale.commons.net.retryable.RetryLimitExceededException;
import com.google.common.base.Stopwatch;

/**
 * Exercises the {@link AbstractLimitedRetryHandler}.
 * 
 * 
 * 
 */
public class TestAbstractLimitedRetryHandler {

	private static final Action<String> RETRY = Action.retry();

	/**
	 * Test that the {@link AbstractLimitedRetryHandler} actually returns when a
	 * retry is successful.
	 */
	@Test
	public void testEventualSuccessLimitedMaxRetries() {
		String success = "SUCCESS";
		String failure = "FAILURE";

		// success on first attempt
		RetryHandler<String> retryHandler = new FailAfterExceedingMaxRetries(5,
				0);
		assertThat(retryHandler.onResponse(success),
				is(Action.respond(success)));

		// success on first retry
		retryHandler = new FailAfterExceedingMaxRetries(5, 0);
		assertThat(retryHandler.onResponse(failure), is(RETRY));
		assertThat(retryHandler.onResponse(success),
				is(Action.respond(success)));

		// success on second retry
		retryHandler = new FailAfterExceedingMaxRetries(5, 0);
		assertThat(retryHandler.onResponse(failure), is(RETRY));
		assertThat(retryHandler.onResponse(failure), is(RETRY));
		assertThat(retryHandler.onResponse(success),
				is(Action.respond(success)));

	}

	/**
	 * Verifies that the {@code maxRetries} limit is honored.
	 */
	@Test
	public void testLimitedMaxRetries() {
		String badResponse = "FAILURE";

		// allow one retry
		RetryHandler<String> retryHandler = new FailAfterExceedingMaxRetries(1,
				0);
		// should retry after first attempt
		assertThat(retryHandler.onResponse(badResponse), is(RETRY));
		// should not retry after second attempt
		Action<String> action = retryHandler.onResponse(badResponse);
		assertThat(action.shouldFail(), is(true));

		// allow two retries
		retryHandler = new FailAfterExceedingMaxRetries(2, 0);
		assertThat(retryHandler.onResponse(badResponse), is(RETRY));
		assertThat(retryHandler.onResponse(badResponse), is(RETRY));
		// should not retry after third attempt
		action = retryHandler.onResponse(badResponse);
		assertThat(action.shouldFail(), is(true));

		// allow 10 retries
		retryHandler = new FailAfterExceedingMaxRetries(10, 0);
		for (int i = 0; i < 10; i++) {
			assertThat(retryHandler.onResponse(badResponse), is(RETRY));
		}
		// after tenth retry we should fail
		assertThat(retryHandler.onResponse(badResponse).shouldFail(), is(true));
	}

	/**
	 * Verifies that the {@link AbstractLimitedRetryHandler} correctly handles
	 * failed requests (that raise {@link Exception}s).
	 */
	@Test
	public void testLimitedMaxRetriesWhenAllRequestsFail() {
		Exception error = new IllegalStateException("request failed");

		// allow one retry
		RetryHandler<String> retryHandler = new FailAfterExceedingMaxRetries(1,
				0);
		// should retry after first attempt
		assertThat(retryHandler.onError(error), is(RETRY));
		// should not retry after second attempt
		Action<String> action = retryHandler.onError(error);
		assertThat(action.shouldFail(), is(true));

		// allow two retries
		retryHandler = new FailAfterExceedingMaxRetries(2, 0);
		assertThat(retryHandler.onError(error), is(RETRY));
		assertThat(retryHandler.onError(error), is(RETRY));
		// should not retry after third attempt
		action = retryHandler.onError(error);
		assertThat(action.shouldFail(), is(true));

		// allow 10 retries
		retryHandler = new FailAfterExceedingMaxRetries(10, 0);
		for (int i = 0; i < 10; i++) {
			assertThat(retryHandler.onError(error), is(RETRY));
		}
		// after tenth retry we should fail
		assertThat(retryHandler.onError(error).shouldFail(), is(true));
	}

	/**
	 * Test using different strategies for
	 * {@link AbstractLimitedRetryHandler#maxRetriesExceeded(Object)}.
	 */
	@Test
	public void testDifferentFailureStrategies() {
		String badResponse = "FAILURE";

		// retry handler that fails after maxRetries
		RetryHandler<String> retryHandler = new FailAfterExceedingMaxRetries(1,
				0);
		assertThat(retryHandler.onResponse(badResponse), is(RETRY));
		Action<String> action = retryHandler.onResponse(badResponse);
		assertThat(action.shouldFail(), is(true));
		assertThat(action.getError(), is(RetryLimitExceededException.class));

		// retry handler that returns the last unsuccessful response after
		// maxRetries
		retryHandler = new RespondAfterExceedingMaxRetries(1, 0);
		assertThat(retryHandler.onResponse(badResponse), is(RETRY));
		assertThat(retryHandler.onResponse(badResponse),
				is(Action.respond(badResponse)));
	}

	/**
	 * Test with "unlimited" retries.
	 */
	@Test
	public void testUnlimitedMaxRetries() {
		String badResponse = "failure";

		// create a retry handler with unlimited maxRetries
		RetryHandler<String> retryHandler = new FailAfterExceedingMaxRetries(
				-1, 0);
		// make a large number of retry attempts and verify that another attempt
		// is always suggested
		for (int i = 0; i < 1000; i++) {
			assertThat(retryHandler.onResponse(badResponse), is(RETRY));
		}
	}

	/**
	 * Verify that a sufficien delay is inserted between each retry.
	 */
	@Test
	public void testDelayBetweenRetries() {
		String reply = "response";

		Stopwatch timer = Stopwatch.createUnstarted();
		int retryDelay = 10; // millis
		RetryHandler<String> retryHandler = new FailAfterExceedingMaxRetries(2,
				retryDelay);

		timer.start();
		// should introduce delay before returning on retry
		assertThat(retryHandler.onResponse(reply).shouldRetry(), is(true));
		assertTrue("expected longer delay",
				timer.elapsed(TimeUnit.MILLISECONDS) >= retryDelay);

		timer.reset();
		timer.start();
		// should introduce delat before returning on retry
		assertThat(retryHandler.onResponse(reply).shouldRetry(), is(true));
		assertTrue("expected longer delay",
				timer.elapsed(TimeUnit.MILLISECONDS) >= retryDelay);

		timer.reset();
		timer.start();
		// no delay should be introduced when decision is to fail
		assertThat(retryHandler.onResponse(reply).shouldFail(), is(true));
		assertTrue("expected shorter delay",
				timer.elapsed(TimeUnit.MILLISECONDS) < retryDelay);
	}

	private static class FailAfterExceedingMaxRetries extends
			AbstractLimitedRetryHandler<String> {

		public FailAfterExceedingMaxRetries(int maxRetries, long delay) {
			super(maxRetries, delay);
		}

		@Override
		public boolean isSuccessful(String response) {
			return response.equalsIgnoreCase("SUCCESS");
		}

		@Override
		public Action<String> maxRetriesExceeded(String withResponse) {
			return Action.fail(new RetryLimitExceededException(String.format(
					"max retries (%d) exceeded. Last response: %s",
					this.maxRetries, withResponse)));
		}
	}

	private static class RespondAfterExceedingMaxRetries extends
			AbstractLimitedRetryHandler<String> {

		public RespondAfterExceedingMaxRetries(int maxRetries, long delay) {
			super(maxRetries, delay);
		}

		@Override
		public boolean isSuccessful(String response) {
			return response.equalsIgnoreCase("SUCCESS");
		}

		@Override
		public Action<String> maxRetriesExceeded(String withResponse) {
			return Action.respond(withResponse);
		}
	}
}
