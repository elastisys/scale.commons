package com.elastisys.scale.commons.net.retryable.retryhandlers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.elastisys.scale.commons.net.retryable.Requester;
import com.elastisys.scale.commons.net.retryable.RetryHandler;
import com.elastisys.scale.commons.net.retryable.RetryableRequest;

/**
 * Exercises teh {@link RetryUntilNoException} {@link RetryHandler}.
 * 
 * 
 * 
 */
public class TestRetryUntilNoException {

	/**
	 * Verifies proper behavior of the {@link RetryUntilNoException} on a
	 * {@link Requester} that succeeds on the first attempt.
	 */
	@Test
	public void successOnFirstAttempt() throws Exception {
		// requester that will succeed on first call
		FailOnFirstXCalls requester = new FailOnFirstXCalls(0, "SUCCESS");

		// set up the retry handler under test with a limited number of retries
		int maxRetries = 0;
		int delay = 0;
		RetryHandler<String> retryer = retryer(maxRetries, delay);

		// run the retryable
		Callable<String> retryable = new RetryableRequest<>(requester, retryer);
		assertThat(retryable.call(), is("SUCCESS"));
		assertThat(requester.getNumInvocations(), is(1));
	}

	/**
	 * Verifies proper behavior when several retries need to be attempted.
	 */
	@Test
	public void successAfterRetries() throws Exception {
		// requester that will succeed on second call
		FailOnFirstXCalls requester = new FailOnFirstXCalls(1, "SUCCESS");
		// set up the retry handler under test with a limited number of retries
		int maxRetries = 1;
		int delay = 0;
		RetryHandler<String> retryer = retryer(maxRetries, delay);
		// run the retryable
		Callable<String> retryable = new RetryableRequest<>(requester, retryer);
		assertThat(retryable.call(), is("SUCCESS"));
		assertThat(requester.getNumInvocations(), is(2));

		// requester that will succeed after 5 calls
		requester = new FailOnFirstXCalls(4, "SUCCESS");
		// set up the retry handler under test with a limited number of retries
		maxRetries = 10;
		retryer = retryer(maxRetries, delay);
		// run the retryable
		retryable = new RetryableRequest<>(requester, retryer);
		assertThat(retryable.call(), is("SUCCESS"));
		assertThat(requester.getNumInvocations(), is(5));

		// requester that will succeed after 11 calls
		requester = new FailOnFirstXCalls(10, "SUCCESS");
		// set up the retry handler under test with a limited number of retries
		maxRetries = 10;
		retryer = retryer(maxRetries, delay);
		// run the retryable
		retryable = new RetryableRequest<>(requester, retryer);
		assertThat(retryable.call(), is("SUCCESS"));
		assertThat(requester.getNumInvocations(), is(11));

		// requester that will succeed after 21 calls
		requester = new FailOnFirstXCalls(20, "SUCCESS");
		// set up the retry handler under test with unlimited number of retries
		retryer = unlimitedRetryer(delay);
		// run the retryable
		retryable = new RetryableRequest<>(requester, retryer);
		assertThat(retryable.call(), is("SUCCESS"));
		assertThat(requester.getNumInvocations(), is(21));

	}

	/**
	 * Verifies that the {@link RetryUntilNoException} retry handler fails after
	 * {@code maxRetries} retries have been attempted without success.
	 */
	@Test(expected = ExecutionException.class)
	public void failureAfterExhaustingMaxRetries() throws Exception {
		// requester that will succeed on second call
		FailOnFirstXCalls requester = new FailOnFirstXCalls(5, "SUCCESS");
		// set up the retry handler under test with a limited number of retries
		int maxRetries = 4;
		int delay = 0;
		RetryHandler<String> retryer = retryer(maxRetries, delay);
		// run the retryable
		Callable<String> retryable = new RetryableRequest<>(requester, retryer);
		retryable.call();
	}

	private RetryUntilNoException<String> retryer(int maxRetries, int delay) {
		return new RetryUntilNoException<>(maxRetries, delay);
	}

	private RetryUntilNoException<String> unlimitedRetryer(int delay) {
		return new RetryUntilNoException<>(-1, delay);
	}

	/**
	 * A {@link Requester} that fails for a fixed number of times before
	 * succeeding by returning a fixed success value.
	 * 
	 * 
	 * 
	 */
	public static class FailOnFirstXCalls implements Requester<String> {
		private int numInvocations = 0;
		private final int failuresBeforeSuccess;

		private final String successValue;

		public FailOnFirstXCalls(int failuresBeforeSuccess, String successValue) {
			this.failuresBeforeSuccess = failuresBeforeSuccess;
			this.successValue = successValue;
		}

		@Override
		public String call() throws Exception {
			this.numInvocations++;
			if (this.numInvocations <= this.failuresBeforeSuccess) {
				throw new Exception(String.format(
						"invocation %d failed (will succeed on call %d)",
						this.numInvocations, (this.failuresBeforeSuccess + 1)));
			}
			return this.successValue;
		}

		/**
		 * Returns the number of invocations seen so far.
		 * 
		 * @return
		 */
		public int getNumInvocations() {
			return this.numInvocations;
		}
	}
}
