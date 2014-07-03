package com.elastisys.scale.commons.net.retryable;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Verifies the behavior of the retry {@link Action} class.
 * 
 * 
 * 
 */
public class TestAction {

	@Test
	public void testRetryAction() {
		Action<String> action = Action.retry();
		assertTrue(action.shouldRetry());
		assertFalse(action.shouldFail());
		assertFalse(action.shouldRespond());
	}

	@Test(expected = IllegalStateException.class)
	public void verifyErrorWhenGettingResponseFromRetryAction() {
		Action.retry().getResponse();
	}

	@Test(expected = IllegalStateException.class)
	public void verifyErrorWhenGettingErrorFromRetryAction() {
		Action.retry().getError();
	}

	@Test
	public void testFailureAction() {
		Exception error = new RetryLimitExceededException();
		Action<String> action = Action.fail(error);

		assertFalse(action.shouldRetry());
		assertTrue(action.shouldFail());
		assertFalse(action.shouldRespond());
		assertThat(action.getError(), is(error));
	}

	@Test(expected = IllegalStateException.class)
	public void verifyErrorWhenGettingResponseFromFailureAction() {
		Action.fail(new RetryLimitExceededException()).getResponse();
	}

	@Test
	public void testResponseAction() {
		String response = "return value";
		Action<String> action = Action.respond(response);

		assertFalse(action.shouldRetry());
		assertFalse(action.shouldFail());
		assertTrue(action.shouldRespond());
		assertThat(action.getResponse(), is(response));
	}

	@Test(expected = IllegalStateException.class)
	public void verifyErrorWhenGettingErrorFromResponseAction() {
		Action.respond("return value").getError();
	}
}
