package com.elastisys.scale.commons.net.retryable.retryhandlers;

import static java.lang.String.format;

import org.apache.http.HttpStatus;

import com.elastisys.scale.commons.net.http.HttpGetRequester;
import com.elastisys.scale.commons.net.http.HttpRequestResponse;
import com.elastisys.scale.commons.net.retryable.Action;
import com.elastisys.scale.commons.net.retryable.RetryHandler;
import com.elastisys.scale.commons.net.retryable.RetryLimitExceededException;
import com.elastisys.scale.commons.net.retryable.RetryableRequest;

/**
 * A {@link RetryHandler} that waits for an HTTP GET to be successful (return
 * with a {@code 200} response code).
 * <p/>
 * It is intended to be used in a {@link RetryableRequest} in concert with a
 * {@link HttpGetRequester}.
 * 
 * @see RetryableRequest
 * @see HttpGetRequester
 * 
 * 
 */
public class RetryUntilHttpResponse200 extends
		AbstractLimitedRetryHandler<HttpRequestResponse> {

	/**
	 * Constructs a new retry handler that will attempt a limited number of
	 * retries with a given delay introduced prior to each new attempt.
	 * 
	 * @param maxRetries
	 *            Maximum number of retries. A value less than or equal to
	 *            {@code 0} signifies an infinite number of retries.
	 * @param delay
	 *            Delay (in ms) between poll attempts.
	 */
	public RetryUntilHttpResponse200(int maxRetries, long delay) {
		super(maxRetries, delay);
	}

	@Override
	public boolean isSuccessful(HttpRequestResponse response) {
		return response.getStatusCode() == HttpStatus.SC_OK;
	}

	@Override
	public Action<HttpRequestResponse> maxRetriesExceeded(
			HttpRequestResponse withResponse) {
		String message = format("Maximum number of retries (%d) exceeded. "
				+ "Last response: %s", this.maxRetries, withResponse);
		RetryLimitExceededException failureReason = new RetryLimitExceededException(
				message);
		return Action.fail(failureReason);
	}
}
