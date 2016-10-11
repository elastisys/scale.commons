package com.elastisys.scale.commons.util.concurrent;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests that exercise the {@link Waiter} class.
 * 
 * 
 * 
 */
public class TestWaiter {
    static Logger logger = LoggerFactory.getLogger(TestWaiter.class);

    private final static ExecutorService executor = Executors.newCachedThreadPool();

    @Test(expected = NullPointerException.class)
    public void verifyThatNullValueCannotBeSet() {
        Waiter<String> waiter = new Waiter<>();
        waiter.set(null);
    }

    /**
     * Verifies that {@link Thread}s will block waiting for a {@link Waiter} to
     * receive a value.
     * 
     * @throws Exception
     */
    @Test
    public void verifyBlockingBehaviorWhenNoValueSet() throws Exception {
        String value = "value";
        Waiter<String> waiter = new Waiter<>();

        Callable<String> awaiterTask = new ObjectAwaiterTask<>(waiter);
        Future<String> awaiterThread = executor.submit(awaiterTask);

        // value should not be immediately available. waiters block.
        try {
            assertThat(awaiterThread.get(50, TimeUnit.MILLISECONDS), is(value));
            fail("should not be any value available yet");
        } catch (TimeoutException e) {
            // expected
        }

        // set value and make sure waiting thread no longer blocks
        waiter.set(value);
        assertThat(awaiterThread.get(50, TimeUnit.MILLISECONDS), is(value));
    }

    /**
     * Verifies that an initial {@link Waiter} value is immediately available to
     * waiting threads.
     * 
     * @throws Exception
     */
    @Test
    public void verifyWaitersNotBlockingWhenInitialValueIsSet() throws Exception {
        Waiter<String> waiter = new Waiter<>("initial-value");

        // value should be immediately available
        Callable<String> awaiterTask = new ObjectAwaiterTask<>(waiter);
        Future<String> awaiterThread = executor.submit(awaiterTask);
        assertThat(awaiterThread.get(), is("initial-value"));
    }

    /**
     * Verifies that new values can be set on a {@link Waiter}.
     * 
     * @throws Exception
     */
    @Test
    public void verifyThatNewValuesCanBeSet() throws Exception {
        Waiter<String> waiter = new Waiter<>("A");

        assertThat(waiter.await(), is("A"));
        waiter.set("B");
        assertThat(waiter.await(), is("B"));
        waiter.set("C");
        assertThat(waiter.await(), is("C"));
    }

    /**
     * 
     * 
     * @param <T>
     *            The type of object to wait for.
     */
    private static class ObjectAwaiterTask<T> implements Callable<T> {
        private final Waiter<T> waiter;

        public ObjectAwaiterTask(Waiter<T> waiter) {
            this.waiter = waiter;
        }

        @Override
        public T call() throws Exception {
            logger.debug("Waiting ...");
            return this.waiter.await();
        }
    }
}
