package com.elastisys.scale.commons.net.retryable.retryhandlers;

import static java.lang.String.format;

import com.elastisys.scale.commons.net.retryable.Action;
import com.elastisys.scale.commons.net.retryable.Requester;
import com.elastisys.scale.commons.net.retryable.RetryHandler;
import com.elastisys.scale.commons.net.retryable.RetryLimitExceededException;
import com.elastisys.scale.commons.net.retryable.RetryableRequest;

/**
 * A {@link RetryHandler} that retries (an optionally limited number of times)
 * until a retryable request does not thrown an Exception.
 * <p/>
 * It is intended to be used in a {@link RetryableRequest} together with any
 * {@link Requester} that throws an {@link Exception} to indicate a failed
 * attempt.
 * 
 * @see RetryableRequest
 * @see Requester
 * 
 * @param <T>
 *            The return type of the {@link Requester}.
 */
public class RetryUntilNoException<T> extends AbstractLimitedRetryHandler<T> {

	/**
	 * Constructs a new retry handler that will attempt a limited number of
	 * retries with a given delay introduced prior to each new attempt.
	 * 
	 * @param maxRetries
	 *            Maximum number of retries. A value less than {@code 0}
	 *            signifies an infinite number of retries.
	 * @param delay
	 *            Delay (in ms) between poll attempts.
	 */
	public RetryUntilNoException(int maxRetries, long delay) {
		super(maxRetries, delay);
	}

	@Override
	public boolean isSuccessful(T response) {
		return true;
	}

	@Override
	public Action<T> maxRetriesExceeded(T withResponse) {
		String message = format("Maximum number of retries (%d) exceeded. "
				+ "Last error: %s", this.maxRetries, withResponse);
		RetryLimitExceededException failureReason = new RetryLimitExceededException(
				message);
		return Action.fail(failureReason);
	}

}
