package com.elastisys.scale.commons.net.retryable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.junit.Test;

/**
 * Exercises the retryers built from {@link Retryers}.
 */
public class TestRetryers {

    @Test
    public void testFixedDelayRetryer() throws Exception {
        RuntimeException fault = new RuntimeException("failed!");

        // should succeed
        Callable<Integer> counter = new FailNTimesCounter(3, fault);
        Retryable<Integer> retryer = Retryers.fixedDelayRetryer("counter", counter, 10, MILLISECONDS, 5);
        assertThat(retryer.call(), is(4));
        assertThat(retryer.getAttempts(), is(4));
        // should be 3 retries => 3 * 10 ms delay at least
        assertTrue(retryer.getTimer().getTime(MILLISECONDS) >= 30);

        // should fail
        counter = new FailNTimesCounter(3, fault);
        retryer = Retryers.fixedDelayRetryer("counter", counter, 10, MILLISECONDS, 3);
        try {
            retryer.call();
            fail("should not succeed");
        } catch (GaveUpException e) {
            // expected
        }
        assertThat(retryer.getAttempts(), is(3));
        // should be 2 retries => 2 * 10 ms delay at least
        assertTrue(retryer.getTimer().getTime(MILLISECONDS) >= 20);
    }

    @Test
    public void testFixedDelayRetryerWithResultPredicate() throws Exception {
        RuntimeException fault = new RuntimeException("failed!");

        // should succeed
        Callable<Integer> counter = new FailNTimesCounter(3, fault);
        int maxRetries = 15;
        Retryable<Integer> retryer = Retryers.fixedDelayRetryer("counter", counter, 10, MILLISECONDS, maxRetries,
                equalTo(10));
        assertThat(retryer.call(), is(10));
        assertThat(retryer.getAttempts(), is(10));
        // should be 9 retries => 9 * 10 ms delay at least
        assertTrue(retryer.getTimer().getTime(MILLISECONDS) >= 90);

        // should fail
        counter = new FailNTimesCounter(3, fault);
        maxRetries = 8;
        retryer = Retryers.fixedDelayRetryer("counter", counter, 10, MILLISECONDS, maxRetries, equalTo(10));
        try {
            retryer.call();
            fail("should not succeed");
        } catch (GaveUpException e) {
            // expected
        }
        assertThat(retryer.getAttempts(), is(8));
        // should be 7 retries => 7 * 10 ms delay at least
        assertTrue(retryer.getTimer().getTime(MILLISECONDS) >= 70);
    }

    @Test
    public void testExponentialBackoffRetryer() throws Exception {
        RuntimeException fault = new RuntimeException("failed!");

        // should succeed
        Callable<Integer> counter = new FailNTimesCounter(3, fault);
        int initialDelay = 1;
        int maxRetries = 5;
        Retryable<Integer> retryer = Retryers.exponentialBackoffRetryer("exp-counter", counter, initialDelay,
                MILLISECONDS, maxRetries);
        assertThat(retryer.call(), is(4));
        assertThat(retryer.getAttempts(), is(4));
        // should be 3 retries => 2^3 - 1 ms delay at least
        assertTrue(retryer.getTimer().getTime(MILLISECONDS) >= 7);

        // should fail
        counter = new FailNTimesCounter(5, fault);
        maxRetries = 5;
        retryer = Retryers.exponentialBackoffRetryer("exp-counter", counter, initialDelay, MILLISECONDS, maxRetries);
        try {
            retryer.call();
            fail("should not succeed");
        } catch (GaveUpException e) {
            // expected
        }
        assertThat(retryer.getAttempts(), is(5));
        // should be 4 retries => 2^4 -1 ms delay at least
        assertTrue(retryer.getTimer().getTime(MILLISECONDS) >= 15);
    }

    @Test
    public void testExponentialBackoffRetryerWithResultPredicate() throws Exception {
        RuntimeException fault = new RuntimeException("failed!");

        // should succeed
        Callable<Integer> counter = new FailNTimesCounter(3, fault);
        int maxRetries = 15;
        int initialDelay = 1;
        Retryable<Integer> retryer = Retryers.exponentialBackoffRetryer("exp-counter", counter, initialDelay,
                MILLISECONDS, maxRetries, equalTo(10));
        assertThat(retryer.call(), is(10));
        assertThat(retryer.getAttempts(), is(10));
        // should be 9 retries => 2^9 - 1 ms delay at least
        assertTrue(retryer.getTimer().getTime(MILLISECONDS) >= 511);

        // should fail
        counter = new FailNTimesCounter(3, fault);
        maxRetries = 8;
        retryer = Retryers.exponentialBackoffRetryer("exp-counter", counter, initialDelay, MILLISECONDS, maxRetries,
                equalTo(10));
        try {
            retryer.call();
            fail("should not succeed");
        } catch (GaveUpException e) {
            // expected
        }
        assertThat(retryer.getAttempts(), is(8));
        // should be 7 retries => 2^7 - 1 ms delay at least
        assertTrue(retryer.getTimer().getTime(MILLISECONDS) >= 127);
    }

    /**
     * A counter task that will fail a specified number of times before
     * eventually producing successful results.
     */
    private static class FailNTimesCounter implements Callable<Integer> {
        /** calls thus far. */
        private int attempts = 0;
        /** Number of planned failure attempts before successful return. */
        private int plannedFailures;
        /** The exception to raise on failed attempts. */
        private Exception failure;

        public FailNTimesCounter(int plannedFailures, Exception failure) {
            this.plannedFailures = plannedFailures;
            this.failure = failure;
        }

        @Override
        public Integer call() throws Exception {
            this.attempts++;
            if (this.attempts <= this.plannedFailures) {
                throw this.failure;
            }
            return this.attempts;
        }
    }

    private static Predicate<Integer> equalTo(int value) {
        return x -> x == value;
    }
}
