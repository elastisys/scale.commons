package com.elastisys.scale.commons.net.retryable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Callable} that repeatedly invokes a wrapped {@link Callable} until
 * it gives a successful response or until all retry attempts have been
 * exhausted. Policies for what characterizes a successful call, after how long
 * to give up, specifying the delay between attempts, and which errors to
 * suppress are all configurable.
 * <p/>
 * The construction follows a builder pattern where policies can be added in a
 * fluent manner. The following is one example of how it could be used to create
 * a task that will be retried, at most 10 times, until a return value of
 * 'success!' has been received with a delay between attempts that increases
 * exponentially:
 *
 * <pre>
 * Callable<String> task = ...
 * Retryable(task).retryUntilResponse("success!").
 *     retryOnError(RuntimeException.class).
 *     stop(StopStrategies.afterAttempts(10)).
 *     delay(DelayStrategies.exponentialBackoff(1));
 * </pre>
 *
 * <p/>
 * If no successful response has been received before the {@link StopStrategy}
 * decides to give up, a {@link GaveUpException} is raised, which includes the
 * last result/exception that resulted from the last attempt.
 * <p/>
 * On encountering an exception that isn't included in the collection of
 * suppressed exceptions, it will simply re-raise the exception and the
 * {@link Retryable} task will fail.
 *
 * @see DelayStrategies
 * @see StopStrategies
 * @see Retryers
 *
 * @param <R>
 *            The response type.
 */
public class Retryable<R> implements Callable<R> {
    private final static Logger LOG = LoggerFactory.getLogger(Retryable.class);

    /**
     * A maximum limit to the number of characters included when logging the
     * result from a retry (intended to reduce log noise in case the
     * {@link Callable} returns a lot of data in each attempt). Longer result
     * strings will be truncated.
     */
    private final static int MAX_LOGGED_RESULT_STRING = 200;

    /** The {@link Callable} that is to be retried. */
    private final Callable<R> task;

    /**
     * The name of the task. No special restrictions apply to the name, which is
     * only used in log output as a way of distinguishing tasks from each other.
     * Could be something similar to "login-request", "await-running{i-123}".
     */
    private String name;

    /**
     * The {@link Predicate} that determines which return values are to be
     * considered successful.
     */
    private Predicate<R> successfulResponse;

    /** The strategy used to determine when to give up. */
    private StopStrategy stopStrategy;

    /** The strategy used to introduce delay between attempts. */
    private DelayStrategy delayStrategy;

    /**
     * The list of {@link Exception}s that are to be suppressed, and which we
     * are to continue retrying after.
     */
    private Set<Class<? extends Exception>> suppressedErrors;

    /** The number of attempts that have been made thus far. */
    private int attempts;
    /** Tracks the elapsed time thus far. */
    private StopWatch timer;

    /**
     * Creates a {@link Retryable} that will retry a given {@link Callable} with
     * default behavior. The default behavior, which simply calls the task once
     * (it accepts any response and doesn't suppress errors), can be overridden
     * via the policy customization methods.
     *
     * @param task
     *            The {@link Callable} to invoke.
     */
    public Retryable(Callable<R> task) {
        this.task = task;
        this.name = task.getClass().getSimpleName();

        // default is to accept any response
        this.successfulResponse = x -> true;
        // default: retry forever
        this.stopStrategy = StopStrategies.never();
        // default: no delay between attempts
        this.delayStrategy = DelayStrategies.noDelay();
        // default: no suppressed errors
        this.suppressedErrors = new HashSet<>();
    };

    /**
     * Sets the name of the task. No special restrictions apply to the name,
     * which is only used in log output as a way of distinguishing tasks from
     * each other. Could be something similar to "login-request",
     * "await-running{i-123}".
     *
     * @param taskName
     * @return
     */
    public Retryable<R> name(String taskName) {
        this.name = taskName;
        return this;
    }

    /**
     * Sets the {@link Predicate} that determines which return values are to be
     * considered successful.
     *
     * @param successfulResponse
     *            A {@link Predicate} that determines which return values are
     *            succesful.
     * @return
     */
    public Retryable<R> retryUntilResponse(Predicate<R> successfulResponse) {
        this.successfulResponse = successfulResponse;
        return this;
    }

    /**
     * Adds a certain {@link Exception} class to the set of task errors that are
     * to be suppressed and, hence, trigger a new attempt.
     *
     * @param exception
     * @return
     */
    public Retryable<R> retryOnError(Class<? extends Exception> exception) {
        this.suppressedErrors.add(exception);
        return this;
    }

    /**
     * Adds the {@link Exception} class to the set of task errors that are to be
     * suppressed and, hence, trigger a new attempt.
     *
     * @return
     */
    public Retryable<R> retryOnException() {
        return retryOnError(Exception.class);
    }

    /**
     * Adds the {@link RuntimeException} class to the set of task errors that
     * are to be suppressed and, hence, trigger a new attempt.
     *
     * @return
     */
    public Retryable<R> retryOnRuntimeException() {
        return retryOnError(RuntimeException.class);
    }

    /**
     * Sets the strategy used to determine when to give up.
     *
     * @param stopStrategy
     * @return
     */
    public Retryable<R> stop(StopStrategy stopStrategy) {
        this.stopStrategy = stopStrategy;
        return this;
    }

    /**
     * Sets the strategy used to determine the delay between successive
     * attempts.
     *
     * @param delayStrategy
     * @return
     */
    public Retryable<R> delay(DelayStrategy delayStrategy) {
        this.delayStrategy = delayStrategy;
        return this;
    }

    @Override
    public R call() throws Exception {
        this.attempts = 0;
        this.timer = StopWatch.createStarted();
        Object lastResult = null;
        try {
            while (true) {
                try {
                    this.attempts++;
                    R response = this.task.call();
                    lastResult = response;
                    if (this.successfulResponse.test(response)) {
                        return response;
                    }
                } catch (Exception e) {
                    lastResult = e;
                    throwIfNotSuppressed(e);
                } finally {
                    logResult(this.attempts, lastResult);
                }
                long elapsedTimeMillis = this.timer.getTime(MILLISECONDS);
                if (this.stopStrategy.giveUp(this.attempts, elapsedTimeMillis)) {
                    giveUp(this.attempts, elapsedTimeMillis, lastResult);
                }
                this.delayStrategy.introduceDelay(this.attempts, elapsedTimeMillis);
            }
        } finally {
            this.timer.stop();
        }
    }

    /**
     * Throws a {@link GaveUpException} after the {@link StopStrategy} has
     * decided to give up.
     *
     * @param attempts
     *            The number of attempts that were made.
     * @param elapsedTimeMillis
     *            The elapsed time in milliseconds since the first attempt.
     * @param lastResult
     *            The last result (can be a return value or an exception).
     * @throws GaveUpException
     */
    private void giveUp(int attempts, long elapsedTimeMillis, Object lastResult) throws GaveUpException {
        String message = String.format("gave up waiting for %s: " + "result from last attempt: %s", this.name,
                asString(lastResult));

        // include source exception if last result was an error
        if (Throwable.class.isAssignableFrom(lastResult.getClass())) {
            throw new GaveUpException(attempts, elapsedTimeMillis, message, Throwable.class.cast(lastResult));
        }

        throw new GaveUpException(attempts, elapsedTimeMillis, message);
    }

    private void logResult(int attempts, Object lastResult) {

        if (LOG.isDebugEnabled()) {
            String resultString = asString(lastResult);
            if (resultString.length() > MAX_LOGGED_RESULT_STRING) {
                resultString = resultString.substring(0, MAX_LOGGED_RESULT_STRING) + " ... (truncated)";
            }
            LOG.debug("{}: attempt {}: '{}'", this.name, attempts, resultString);
        }
    }

    /**
     * Returns a string representation of a given attempt result, which extracts
     * exception message if the result was an {@link Exception}.
     *
     * @param result
     * @return
     */
    private String asString(Object result) {
        if (result == null) {
            return "null/void";
        }
        if (Throwable.class.isAssignableFrom(result.getClass())) {
            Throwable exception = Throwable.class.cast(result);
            String message = exception.getMessage();
            return exception.getClass().getSimpleName() + ": " + (message != null ? message : "null");
        }
        return result.toString();
    }

    /**
     * Returns the number of attempts that have been made.
     *
     * @return
     */
    int getAttempts() {
        return this.attempts;
    }

    /**
     * Returns a timer tracking the elapsed time.
     *
     * @return
     */
    StopWatch getTimer() {
        return this.timer;
    }

    /**
     * Simply returns if a caught error is a direct instance of any of the
     * suppressed exception classes (or any of their sub-classes), otherwise it
     * throws the {@link Exception}.
     *
     * @param caughtError
     * @throws Exception
     */
    private void throwIfNotSuppressed(Exception caughtError) throws Exception {
        for (Class<? extends Exception> suppressedError : this.suppressedErrors) {
            if (suppressedError.isAssignableFrom(caughtError.getClass())) {
                return;
            }
        }
        throw caughtError;
    }
}