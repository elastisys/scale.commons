package com.elastisys.scale.commons.net.retryable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Objects;

/**
 * A retry {@link Action} returned by a {@link RetryHandler} to indicate to a
 * {@link RetryableRequest} whether or not a new request attempt should be made.
 * <p/>
 * An {@link Action} can either be created via a {@link AutoScalerBuilder} or
 * through the static factory methods: {@link Action#shouldRetry()},
 * {@link Action#shouldRespond()} and {@link Action#shouldFail()}.
 * 
 * @see RetryHandler
 * @see RetryableRequest
 * 
 * 
 * 
 * @param <R>
 *            The response type.
 */
public class Action<R> {

	/**
	 * <code>true</code> if a new retry should be attempted, <code>false</code>
	 * otherwise.
	 */
	private final boolean retry;
	/**
	 * The response in case this is a response action. In this case,
	 * {@link #retry} must be <code>false</code> and {@link #error} must be
	 * <code>null</code>.
	 */
	private final R response;

	/**
	 * The error in case this is an error action. In this case, {@link #retry}
	 * must be <code>false</code> and {@link #response} must be
	 * <code>null</code>.
	 */
	private final Exception error;

	private Action(boolean retry, R response, Exception error) {
		this.retry = retry;
		this.response = response;
		this.error = error;

		// sanity check
		if (this.retry) {
			checkArgument((this.error == null) && (this.response == null),
					"retry ordered, but response and/or error still set");
		} else {
			checkArgument(((this.error == null) && (this.response != null))
					|| ((this.error != null) && (this.response == null)),
					"either error or response should be set, not both.");
		}
	}

	/**
	 * Returns <code>true</code> if a new retry should be attempted,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if a new retry should be attempted,
	 *         <code>false</code> otherwise.
	 */
	public boolean shouldRetry() {
		return this.retry;
	}

	/**
	 * Returns <code>true</code> if this is a failure {@link Action}. If so, the
	 * error can be retrieved via {@link #getError()}.
	 * 
	 * @return
	 */
	public boolean shouldFail() {
		return !this.retry && (this.error != null);
	}

	/**
	 * Returns <code>true</code> if this is a reponse {@link Action}. If so, the
	 * response can be retrieved via {@link #getResponse()}.
	 * 
	 * @return
	 */
	public boolean shouldRespond() {
		return !this.retry && (this.response != null);
	}

	/**
	 * Returns the response in case this is a response {@link Action}.
	 * 
	 * @return
	 */
	public R getResponse() {
		checkState(shouldRespond(),
				"attempt to get reponse from a non-response action");
		return this.response;
	}

	/**
	 * Returns the error in case this is a failure {@link Action}.
	 * 
	 * @return
	 */
	public Exception getError() {
		checkState(shouldFail(),
				"attempt to get error from a non-failure action");
		return this.error;
	}

	/**
	 * Creates a retry {@link Action}.
	 * 
	 * @return
	 */
	public static <R> Action<R> retry() {
		return new Action<R>(true, null, null);
	}

	/**
	 * Creates a failure {@link Action}.
	 * 
	 * @param withError
	 *            The failure error.
	 * @return
	 */
	public static <R> Action<R> fail(Exception withError) {
		return new Action<R>(false, null, withError);
	}

	/**
	 * Creates a response {@link Action}.
	 * 
	 * @param withResponse
	 *            The response.
	 * @return
	 */
	public static <R> Action<R> respond(R withResponse) {
		return new Action<R>(false, withResponse, null);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.retry, this.response, this.error);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Action) {
			Action<R> that = (Action<R>) obj;
			return Objects.equal(this.retry, that.retry)
					&& Objects.equal(this.response, that.response)
					&& Objects.equal(this.error, that.error);
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("retry", this.retry)
				.add("response", this.response).add("error", this.error)
				.toString();
	}
}
