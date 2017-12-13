package com.elastisys.scale.commons.net.retryable;

import static com.elastisys.scale.commons.net.retryable.DelayStrategies.exponentialBackoff;
import static com.elastisys.scale.commons.net.retryable.DelayStrategies.fixed;
import static com.elastisys.scale.commons.net.retryable.StopStrategies.afterAttempts;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * A collection of useful {@link Retryable} factory methods.
 *
 * @see Retryable
 */
public class Retryers {

    /**
     * Creates an {@link Retryable} that will retry a {@link Callable} a limited
     * number of times until a return value is received (no exception is
     * raised). The delay between successive attempts is fixed.
     *
     * @param name
     *            The name of the task. Only for distinguishing tasks from each
     *            other in the log output.
     * @param callable
     *            The {@link Callable} task to retry.
     * @param fixedDelay
     *            The delay to use between attempts.
     * @param unit
     *            Delay unit.
     * @param maxAttempts
     *            The maximum number of attempts.
     * @return The constructed {@link Retryable}.
     */
    public static <R> Retryable<R> fixedDelayRetryer(String name, Callable<R> callable, long fixedDelay, TimeUnit unit,
            int maxAttempts) {

        return new Retryable<>(callable).name(name).delay(fixed(fixedDelay, unit)).stop(afterAttempts(maxAttempts))
                .retryOnException();
    }

    /**
     * Creates an {@link Retryable} that will retry a {@link Callable} a limited
     * number of times until a return value is received that satisfies the given
     * predicate. The delay between successive attempts is fixed.
     *
     * @param name
     *            The name of the task. Only for distinguishing tasks from each
     *            other in the log output.
     * @param callable
     *            The {@link Callable} task to retry.
     * @param fixedDelay
     *            The delay to use between attempts.
     * @param unit
     *            Delay unit.
     * @param maxAttempts
     *            The maximum number of attempts.
     * @param responsePredicate
     *            {@link Predicate} that determines if a response is to be
     *            considered successful.
     * @return The constructed {@link Retryable}.
     */
    public static <R> Retryable<R> fixedDelayRetryer(String name, Callable<R> callable, long fixedDelay, TimeUnit unit,
            int maxAttempts, Predicate<R> responsePredicate) {

        return new Retryable<>(callable).name(name).delay(fixed(fixedDelay, unit)).stop(afterAttempts(maxAttempts))
                .retryOnException().retryUntilResponse(responsePredicate);
    }

    /**
     * Creates an exponential back-off {@link Retryable} that will retry a
     * {@link Callable} a limited number of times until a return value is
     * received (no exception is raised).
     *
     * @param name
     *            The name of the task. Only for distinguishing tasks from each
     *            other in the log output.
     * @param callable
     *            The {@link Callable} task to retry.
     * @param initialDelay
     *            Delay after first attempt. This delay will grow exponentially
     *            with the number of attempts (factor: 2^1, 2^2, 2^3, ... etc).
     * @param unit
     *            Delay unit.
     * @param maxAttempts
     *            The maximum number of attempts.
     * @return The constructed {@link Retryable}.
     */
    public static <R> Retryable<R> exponentialBackoffRetryer(String name, Callable<R> callable, long initialDelay,
            TimeUnit unit, int maxAttempts) {

        return new Retryable<>(callable).name(name).delay(exponentialBackoff(initialDelay, unit))
                .stop(afterAttempts(maxAttempts)).retryOnException();
    }

    /**
     * Creates an exponential back-off {@link Retryable} that will retry a
     * {@link Callable} a limited number of times until a return value is
     * received that satisfies the given {@link Predicate}.
     *
     * @param name
     *            The name of the task. Only for distinguishing tasks from each
     *            other in the log output.
     * @param callable
     *            The {@link Callable} task to retry.
     * @param initialDelay
     *            Delay after first attempt. This delay will grow exponentially
     *            with the number of attempts (factor: 2^1, 2^2, 2^3, ... etc).
     * @param unit
     *            Delay unit.
     * @param maxAttempts
     *            The maximum number of attempts.
     * @param responsePredicate
     *            {@link Predicate} that determines if a response is to be
     *            considered successful.
     * @return The constructed {@link Retryable}.
     */
    public static <R> Retryable<R> exponentialBackoffRetryer(String name, Callable<R> callable, long initialDelay,
            TimeUnit unit, int maxAttempts, Predicate<R> responsePredicate) {

        return new Retryable<>(callable).name(name).delay(exponentialBackoff(initialDelay, unit))
                .stop(afterAttempts(maxAttempts)).retryOnException().retryUntilResponse(responsePredicate);
    }

}
