package com.elastisys.scale.commons.net.retryable.retryhandlers;

import static java.lang.String.format;

import com.elastisys.scale.commons.net.retryable.Action;
import com.elastisys.scale.commons.net.retryable.RetryHandler;
import com.elastisys.scale.commons.net.retryable.RetryLimitExceededException;

/**
 * A {@link RetryHandler} that performs a limited number of retries before
 * responding/failing. Subclasses need to provide methods to:
 * <ul>
 * <li>Determine when a response is successful: {@link #isSuccessful(Object)}</li>
 * <li>Determine what to do when all retries have been exhausted:
 * {@link #maxRetriesExceeded(Object)}</li>
 * </ul>
 * 
 * 
 * 
 * @param <R>
 *            The response type.
 */
public abstract class AbstractLimitedRetryHandler<R> implements RetryHandler<R> {

	/** Maximum number of retries. */
	protected final int maxRetries;

	/** Delay (in ms) between retries. */
	protected final long delay;

	/** Number of retries carried out so far. */
	private int retries = 0;

	/**
	 * Constructs a new {@link AbstractLimitedRetryHandler} that will attempt a
	 * limited number of retries with a given delay introduced prior to each new
	 * attempt.
	 * 
	 * @param maxRetries
	 *            Maximum number of retries. A value less than {@code 0}
	 *            signifies an infinite number of retries.
	 * @param delay
	 *            Delay (in ms) between poll attempts.
	 */
	public AbstractLimitedRetryHandler(int maxRetries, long delay) {
		this.maxRetries = (maxRetries < 0) ? Integer.MAX_VALUE : maxRetries;
		this.delay = delay;
	}

	@Override
	public Action<R> onResponse(R response) {
		if (isSuccessful(response)) {
			return Action.respond(response);
		}
		if (this.retries < this.maxRetries) {
			return retryAfterDelay();
		}

		// retries exceeded: ask subclass how to proceed
		return maxRetriesExceeded(response);
	}

	@Override
	public Action<R> onError(Exception error) {
		if (this.retries < this.maxRetries) {
			return retryAfterDelay();
		}

		// retries exceeded: ask subclass how to proceed
		return maxRetriesExceeded(error);
	}

	private Action<R> retryAfterDelay() {
		// introduce wait-time
		introduceDelay();
		this.retries++;
		return Action.retry();
	}

	/**
	 * Strategy method that gets called for every received response to decide if
	 * the response was a successful one, according to the success criteria of
	 * this {@link RetryHandler}.
	 * 
	 * @param response
	 * @return
	 */
	public abstract boolean isSuccessful(R response);

	/**
	 * Strategy method that decides on the next action to take when
	 * {@code maxRetries} has been exceeded and the last response received was a
	 * non-successful one (according to {@link #isSuccessful(Object)}).
	 * <p/>
	 * Two sensible options are to either just return the last response (despite
	 * it being unsuccessful) or raising an error that the request failed.
	 * 
	 * @param withResponse
	 *            The non-successful response that was received on final
	 *            attempt.
	 * @return The {@link Action} to proceed with.
	 */
	public abstract Action<R> maxRetriesExceeded(R withResponse);

	/**
	 * Strategy method that decides on the next action to take when
	 * {@code maxRetries} has been exceeded and the last request failed with an
	 * error.
	 * 
	 * @param withError
	 *            The error that occurred on the final attempt.
	 * @return The {@link Action} to proceed with.
	 */
	public Action<R> maxRetriesExceeded(Exception withError) {
		String message = format("Maximum number of retries (%d) exceeded. "
				+ "Last error: %s", this.maxRetries, withError.getMessage());
		RetryLimitExceededException failureReason = new RetryLimitExceededException(
				message, withError);
		return Action.fail(failureReason);
	}

	private void introduceDelay() {
		try {
			Thread.sleep(this.delay);
		} catch (InterruptedException e) {
			throw new RuntimeException(this.getClass().getSimpleName()
					+ " interrupted: " + e.getMessage(), e);
		}
	}

}
