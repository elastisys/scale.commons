package com.elastisys.scale.commons.net.retryable;

import static java.lang.String.format;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link RetryableRequest} is a request that is retried until the request is
 * considered successful or until a {@link RetryHandler} decides that no more
 * requests are to be attempted.
 * <p/>
 * Request attempts are carried out by a {@link Requester} and the retry policy
 * is implemented by a {@link RetryHandler}.
 * <p/>
 * After every request attempt, the {@link RetryHandler} gets to decide if
 * another attempt is to be made by returning an {@link Action} object. The
 * {@link Action} may either indicate that a new attempt should be made or that
 * no further retries are to be made. In the latter case, the {@link Action} may
 * either suggest returning the latest response (in case the request was
 * successful) or failing with an {@link Exception}. In case of a failure
 * {@link Action}, the cause {@link Exception} will be wrapped inside an
 * {@link ExecutionException}.
 *
 * @see Requester
 * @see RetryHandler
 *
 *
 *
 * @param <R>
 *            The response type.
 */
public class RetryableRequest<R> implements Callable<R> {
	static Logger logger = LoggerFactory.getLogger(RetryableRequest.class);

	/** The task that carries out request attempts. */
	private final Requester<R> requester;
	/**
	 * The retry policy that decides if (and when) to make a new request
	 * attempt.
	 */
	private final RetryHandler<R> retryHandler;

	/**
	 * The name of the task. No special restrictions apply to the name, which is
	 * only used in log output as a way of distinguishing tasks from each other.
	 * Could be something similar to "login request", "running state waiter".
	 */
	private final String name;

	/**
	 * Constructs a {@link RetryableRequest} without an explicit name. Instead,
	 * the task will be named after the {@link Requester} class.
	 *
	 * @param requester
	 *            The task that carries out request attempts.
	 * @param retryHandler
	 *            The retry policy that decides if (and when) to make a new
	 *            request attempt.
	 */
	public RetryableRequest(Requester<R> requester, RetryHandler<R> retryHandler) {
		this(requester, retryHandler, requester.getClass().getSimpleName());
	}

	/**
	 * Constructs a named {@link RetryableRequest}.
	 *
	 * @param requester
	 *            The task that carries out request attempts.
	 * @param retryHandler
	 *            The retry policy that decides if (and when) to make a new
	 *            request attempt.
	 * @param name
	 *            The name of the task. No special restrictions apply to the
	 *            name, which is only used in log output as a way of
	 *            distinguishing tasks from each other. Could be something
	 *            similar to "login request", "running state waiter".
	 */
	public RetryableRequest(Requester<R> requester,
			RetryHandler<R> retryHandler, String name) {
		this.requester = requester;
		this.retryHandler = retryHandler;
		this.name = name;
	}

	@Override
	public R call() throws Exception {
		int attempts = 0;
		while (true) {
			Action<R> retryAction = null;
			try {
				attempts++;
				logger.debug("{}: attempting request {}", this.name, attempts);
				R response = this.requester.call();
				logger.debug("{}: attempt response: {}", this.name, response);
				retryAction = this.retryHandler.onResponse(response);
			} catch (Exception e) {
				logger.debug("{}: attempt failed: {}", this.name,
						e.getMessage());
				retryAction = this.retryHandler.onError(e);
			}
			if (!retryAction.shouldRetry()) {
				if (retryAction.shouldRespond()) {
					logger.debug(
							"{}: retry handler decided to respond with: {}",
							this.name, retryAction.getResponse());
					return retryAction.getResponse();
				}
				Exception error = retryAction.getError();
				logger.debug(
						"{}: retry handler decided to fail with error: {}",
						this.name, error);
				throw new ExecutionException(format(
						"%s: retryable request failed after %d retries, "
								+ "no more retries will be attempted: %s",
						this.name, (attempts - 1), error.getMessage()), error);
			}
			logger.debug("{}: retry handler decided to continue", this.name);
		}
	}
}
