package com.elastisys.scale.commons.net.retryable;

/**
 * Strategy that decides whether or not a {@link RetryableRequest} should be
 * retried.
 * 
 * @see RetryableRequest
 * 
 * 
 * @param <R>
 *            The response type of the request.
 */
public interface RetryHandler<R> {

	/**
	 * Given the response from the last request attempt, decides whether or not
	 * a new request should be attempted.
	 * <p/>
	 * In case the {@link RetryHandler} decides to make another attempt (by
	 * returning an {@link Action} with {@link Action#shouldRetry()} set to
	 * <code>true</code>), it may choose to introduce a wait time for the next
	 * request attempt by delaying the return value according to some back-off
	 * scheme.
	 * 
	 * @param response
	 *            The response from the last request attempt.
	 * @return An {@link Action} with {@link Action#shouldRetry()} set to
	 *         <code>true</code> if the {@link RetryableRequest} should be
	 *         retried, an {@link Action} with {@link Action#shouldRetry()} set
	 *         to <code>false</code> otherwise.
	 */
	Action<R> onResponse(R response);

	/**
	 * Given the error from the last request attempt, decide whether or not a
	 * new request should be attempted.
	 * <p/>
	 * In case the {@link RetryHandler} decides to make another attempt (by
	 * returning an {@link Action} with {@link Action#shouldRetry()} set to
	 * <code>true</code>), it may choose to introduce a wait time for the next
	 * request attempt by delaying the return value according to some back-off
	 * scheme.
	 * 
	 * @param error
	 *            The error from the last request attempt.
	 * @return An {@link Action} with {@link Action#shouldRetry()} set to
	 *         <code>true</code> if the {@link RetryableRequest} should be
	 *         retried, an {@link Action} with {@link Action#shouldRetry()} set
	 *         to <code>false</code> otherwise.
	 */
	Action<R> onError(Exception error);
}
