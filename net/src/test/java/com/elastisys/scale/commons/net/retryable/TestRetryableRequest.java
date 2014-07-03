package com.elastisys.scale.commons.net.retryable;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

/**
 * Verifies the operation of the {@link RetryableRequest} and its interactions
 * with the {@link Requester} and {@link RetryHandler}.
 * <p/>
 * It tests that {@link Requester} and {@link RetryHandler} are called as
 * expected and that the {@link Action}s suggested by the {@link RetryHandler}
 * are honored.
 * 
 * 
 * 
 */
public class TestRetryableRequest {

	private final Requester<String> requester = mock(Requester.class);
	private final RetryHandler<String> retryHandler = mock(RetryHandler.class);

	/** Object under test. */
	private RetryableRequest<String> retryableRequest;

	@Before
	public void onSetup() {
		this.retryableRequest = new RetryableRequest<String>(this.requester,
				this.retryHandler, "task1");
	}

	/**
	 * Tests behavior when {@link RetryableRequest} gets a valid response on
	 * first request.
	 * 
	 * @throws Exception
	 */
	@Test
	public void responseOnFirstAttempt() throws Exception {
		// requester will: return a valid response
		String response = "'OK'";
		when(this.requester.call()).thenReturn(response);
		// retryHandler will: respond (with response)
		when(this.retryHandler.onResponse(response)).thenReturn(
				Action.respond(response));

		// start test
		String finalResponse = this.retryableRequest.call();
		assertThat(finalResponse, is(response));

		// verify that mock calls were made
		verify(this.requester).call();
		verifyNoMoreInteractions(this.requester);
		verify(this.retryHandler).onResponse(response);
		verifyNoMoreInteractions(this.retryHandler);
	}

	/**
	 * Tests behavior when {@link RetryableRequest} never gets a response and
	 * {@link RetryHandler} eventually decides to fail with error.
	 * 
	 * @throws Exception
	 */
	@Test
	public void failureAfterMultipleAttempts() throws Exception {
		// requester will: fail, fail, fail
		RuntimeException error = new RuntimeException("request failed");
		when(this.requester.call()).thenThrow(error).thenThrow(error)
				.thenThrow(error);

		// retryHandler will: retry, retry, fail (give up)
		Action<String> retryAction = Action.retry();
		Action<String> failAction = Action
				.fail(new RetryLimitExceededException("gave up"));
		when(this.retryHandler.onError(error)).thenReturn(retryAction)
				.thenReturn(retryAction).thenReturn(failAction);

		// start test
		try {
			this.retryableRequest.call();
			fail("retryable request should eventually fail");
		} catch (ExecutionException e) {
			// expected
		}

		// verify that mock calls were made
		verify(this.requester, times(3)).call();
		verifyNoMoreInteractions(this.requester);
		verify(this.retryHandler, times(3)).onError(error);
		verifyNoMoreInteractions(this.retryHandler);
	}

	/**
	 * Tests behavior when {@link RetryableRequest} gets a response after a few
	 * failed attempts and {@link RetryHandler} decides to respond.
	 * 
	 * @throws Exception
	 */
	@Test
	public void responseAfterMultipleAttempts() throws Exception {
		// requester will: fail, fail, respond
		RuntimeException requestError = new RuntimeException("request failed");
		String response = "'OK'";
		when(this.requester.call()).thenThrow(requestError)
				.thenThrow(requestError).thenReturn(response);

		// retryHandler will: retry, retry, respond
		Action<String> retryAction = Action.retry();
		Action<String> responseAction = Action.respond(response);
		when(this.retryHandler.onError(requestError)).thenReturn(retryAction)
				.thenReturn(retryAction);
		when(this.retryHandler.onResponse(response)).thenReturn(responseAction);

		// start test
		String finalResponse = this.retryableRequest.call();
		assertThat(finalResponse, is(response));

		// verify that mock calls were made
		verify(this.requester, times(3)).call();
		verifyNoMoreInteractions(this.requester);
		verify(this.retryHandler, times(2)).onError(requestError);
		verify(this.retryHandler, times(1)).onResponse(response);
		verifyNoMoreInteractions(this.retryHandler);
	}

}
