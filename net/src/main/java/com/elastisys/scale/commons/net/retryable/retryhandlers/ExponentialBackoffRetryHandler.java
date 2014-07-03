package com.elastisys.scale.commons.net.retryable.retryhandlers;

import static java.lang.Math.pow;
import static java.lang.Math.round;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.net.retryable.Action;
import com.elastisys.scale.commons.net.retryable.Requester;
import com.elastisys.scale.commons.net.retryable.RetryHandler;
import com.elastisys.scale.commons.net.retryable.RetryableRequest;

/**
 * A {@link RetryHandler} handler to be used with a {@link RetryableRequest}
 * that uses an exponential back-off strategy (where the delay between each new
 * attempt grows exponentially).
 * <p/>
 * It will cause the {@link RetryableRequest} to retry until either a
 * {@link Requester} request attempt succeeds (i.e., does not throw an
 * exception) or until the maximum number of attempts ({@code maxRetries}) has
 * been exhausted (in which case the request will be deemed a failure).
 * 
 * @see RetryableRequest
 * 
 * 
 * @param <T>
 *            The response type of the {@link Requester}.
 */
public class ExponentialBackoffRetryHandler<T> implements RetryHandler<T> {
	static Logger logger = LoggerFactory
			.getLogger(ExponentialBackoffRetryHandler.class);

	private final int maxRetries;
	private final long initialDelay;
	private int retries = 0;

	public ExponentialBackoffRetryHandler(int maxRetries, long initialDelay) {
		this.maxRetries = maxRetries;
		this.initialDelay = initialDelay;
	}

	@Override
	public Action<T> onResponse(T response) {
		return Action.respond(response);
	}

	@Override
	public Action<T> onError(Exception error) {
		if (this.retries < this.maxRetries) {
			// introduce wait-time
			introduceDelay(delay(this.retries));
			this.retries++;
			return Action.retry();
		}
		return Action.fail(error);
	}

	private void introduceDelay(long delayInMillis) {
		try {
			logger.debug("delaying " + delayInMillis
					+ " ms before next attempt");
			Thread.sleep(delayInMillis);
		} catch (InterruptedException e) {
			throw new RuntimeException(this.getClass().getSimpleName()
					+ " interrupted: " + e.getMessage(), e);
		}
	}

	private long delay(int retries) {
		return round(pow(2, retries) * this.initialDelay);
	}
}