package com.elastisys.scale.commons.net.retryable;

import java.util.concurrent.Callable;

/**
 * A {@link Requester} is a {@link Callable} task that carries out request
 * attempts for a {@link RetryableRequest}.
 * 
 * @see RetryableRequest
 * 
 * 
 * 
 * @param <R>
 *            The type of the response.
 */
public interface Requester<R> extends Callable<R> {

}
