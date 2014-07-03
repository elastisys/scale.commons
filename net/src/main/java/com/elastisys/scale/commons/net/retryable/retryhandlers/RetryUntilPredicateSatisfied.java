package com.elastisys.scale.commons.net.retryable.retryhandlers;

import static java.lang.String.format;

import com.elastisys.scale.commons.net.retryable.Action;
import com.elastisys.scale.commons.net.retryable.Requester;
import com.elastisys.scale.commons.net.retryable.RetryHandler;
import com.elastisys.scale.commons.net.retryable.RetryLimitExceededException;
import com.google.common.base.Predicate;

/**
 * A {@link RetryHandler} that attempts to execute a request a limited number of
 * times until a response {@link Predicate} is satisfied or until the
 * {@code maxRetries} have been exhausted, whichever happens first.
 * <p/>
 * If the {@link Predicate} has not been satisfied within {@code maxRetries}, a
 * {@link RetryLimitExceededException} will be thrown.
 * 
 * 
 * 
 * @param <R>
 *            The response type of the {@link Requester}.
 */
public class RetryUntilPredicateSatisfied<R> extends
		AbstractLimitedRetryHandler<R> {

	/** The {@link Predicate} that a successful response must satisfy. */
	private Predicate<? super R> condition;

	/**
	 * Constructs a new {@link RetryUntilPredicateSatisfied} that will attempt a
	 * limited number of retries with a given delay introduced prior to each new
	 * attempt.
	 * 
	 * @param condition
	 *            The {@link Predicate} that a successful response must satisfy.
	 * @param maxRetries
	 *            Maximum number of retries. A value less than {@code 0}
	 *            signifies an infinite number of retries.
	 * @param delay
	 *            Delay (in ms) between poll attempts.
	 */
	public RetryUntilPredicateSatisfied(Predicate<? super R> condition,
			int maxRetries, long delay) {
		super(maxRetries, delay);
		this.condition = condition;
	}

	@Override
	public boolean isSuccessful(R response) {
		return this.condition.apply(response);
	}

	@Override
	public Action<R> maxRetriesExceeded(R withResponse) {
		String message = format("Maximum number of retries (%d) exceeded "
				+ "without encountering any response that "
				+ "satisfied predicate. " + "Last response was: %s",
				this.maxRetries, withResponse);
		RetryLimitExceededException failureReason = new RetryLimitExceededException(
				message);
		return Action.fail(failureReason);
	}
}
