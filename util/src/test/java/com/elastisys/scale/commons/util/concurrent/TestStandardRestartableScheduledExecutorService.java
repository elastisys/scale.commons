package com.elastisys.scale.commons.util.concurrent;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

/**
 * Exercises the restart semantics of the
 * {@link StandardRestartableScheduledExecutorService}.
 *
 *
 */
public class TestStandardRestartableScheduledExecutorService {

    /** Object under test. */
    private StandardRestartableScheduledExecutorService executor;

    @Before
    public void onSetup() {
        this.executor = new StandardRestartableScheduledExecutorService(5);
    }

    @Test
    public void start() {
        assertThat(this.executor.isStarted(), is(false));
        assertThat(this.executor.innerExecutor(), is(nullValue()));
        this.executor.start();
        assertThat(this.executor.isStarted(), is(true));
        assertThat(this.executor.innerExecutor(), is(not(nullValue())));
    }

    @Test
    public void startWhenStarted() {
        assertThat(this.executor.isStarted(), is(false));
        assertThat(this.executor.innerExecutor(), is(nullValue()));
        this.executor.start();
        assertThat(this.executor.isStarted(), is(true));
        ExecutorService innerExecutor = this.executor.innerExecutor();
        assertThat(innerExecutor, is(not(nullValue())));

        this.executor.start();
        assertThat(this.executor.isStarted(), is(true));
        // still same executor service inside
        assertSame(this.executor.innerExecutor(), innerExecutor);
    }

    @Test
    public void stop() throws InterruptedException {
        assertThat(this.executor.isStarted(), is(false));
        assertThat(this.executor.innerExecutor(), is(nullValue()));
        this.executor.start();
        assertThat(this.executor.isStarted(), is(true));
        assertThat(this.executor.innerExecutor(), is(not(nullValue())));
        this.executor.stop(0, TimeUnit.MILLISECONDS);
        assertThat(this.executor.isStarted(), is(false));
        assertThat(this.executor.innerExecutor(), is(nullValue()));
    }

    @Test
    public void stopWhenStopped() throws InterruptedException {
        assertThat(this.executor.isStarted(), is(false));
        this.executor.stop(0, TimeUnit.MILLISECONDS);
        assertThat(this.executor.isStarted(), is(false));
    }

    @Test
    public void restart() throws InterruptedException {
        // start
        assertThat(this.executor.isStarted(), is(false));
        assertThat(this.executor.innerExecutor(), is(nullValue()));
        this.executor.start();
        assertThat(this.executor.isStarted(), is(true));
        ExecutorService firstExecutor = this.executor.innerExecutor();
        assertThat(firstExecutor, is(not(nullValue())));

        // stop
        this.executor.stop(0, TimeUnit.MILLISECONDS);
        assertThat(this.executor.isStarted(), is(false));
        assertThat(this.executor.innerExecutor(), is(nullValue()));

        // re-start
        this.executor.start();
        assertThat(this.executor.isStarted(), is(true));
        // different executor service inside after restart
        assertNotSame(this.executor.innerExecutor(), firstExecutor);
    }

    /**
     * Verifies that calling {@link ExecutorService#shutdown()} causes executor
     * to be stopped and its inner executor service to be cleared.
     */
    @Test
    public void shutdown() {
        // start
        assertThat(this.executor.isStarted(), is(false));
        assertThat(this.executor.innerExecutor(), is(nullValue()));
        this.executor.start();
        assertThat(this.executor.isStarted(), is(true));
        ExecutorService firstExecutor = this.executor.innerExecutor();
        assertThat(firstExecutor, is(not(nullValue())));

        // stop via shutdown
        this.executor.shutdown();
        assertThat(this.executor.isStarted(), is(false));
        assertThat(this.executor.innerExecutor(), is(nullValue()));
    }

    /**
     * Verifies that calling {@link ExecutorService#shutdownNow()} causes
     * executor to be stopped and its inner executor service to be cleared.
     */
    @Test
    public void shutdownNow() {
        // start
        assertThat(this.executor.isStarted(), is(false));
        assertThat(this.executor.innerExecutor(), is(nullValue()));
        this.executor.start();
        assertThat(this.executor.isStarted(), is(true));
        ExecutorService firstExecutor = this.executor.innerExecutor();
        assertThat(firstExecutor, is(not(nullValue())));

        // stop via shutdown
        this.executor.shutdownNow();
        assertThat(this.executor.isStarted(), is(false));
        assertThat(this.executor.innerExecutor(), is(nullValue()));

    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     */
    @Test(expected = IllegalStateException.class)
    public void executeOnStoppedExecutor() throws InterruptedException {
        this.executor.stop(0, TimeUnit.SECONDS);
        this.executor.execute(runnable());
    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     */
    @Test(expected = IllegalStateException.class)
    public void invokeAllOnStoppedExecutor() throws InterruptedException {
        this.executor.stop(0, TimeUnit.SECONDS);
        this.executor.invokeAll(Arrays.asList(callable()));
    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     */
    @Test(expected = IllegalStateException.class)
    public void invokeAllWithTimeoutOnStoppedExecutor() throws InterruptedException {
        this.executor.stop(0, TimeUnit.SECONDS);
        int timeout = 10;
        this.executor.invokeAll(Arrays.asList(callable()), timeout, TimeUnit.SECONDS);
    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     *
     * @throws TimeoutException
     * @throws ExecutionException
     */
    @Test(expected = IllegalStateException.class)
    public void invokeAnyOnStoppedExecutor() throws InterruptedException, ExecutionException, TimeoutException {
        this.executor.stop(0, TimeUnit.SECONDS);
        this.executor.invokeAny(Arrays.asList(callable()));
    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     *
     * @throws TimeoutException
     * @throws ExecutionException
     */
    @Test(expected = IllegalStateException.class)
    public void invokeAnyWithTimeoutOnStoppedExecutor()
            throws InterruptedException, ExecutionException, TimeoutException {
        this.executor.stop(0, TimeUnit.SECONDS);
        int timeout = 10;
        this.executor.invokeAny(Arrays.asList(callable()), timeout, TimeUnit.SECONDS);
    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     *
     * @throws InterruptedException
     */
    @Test(expected = IllegalStateException.class)
    public void invokeScheduleCallableOnStoppedExecutor() throws InterruptedException {
        this.executor.stop(0, TimeUnit.SECONDS);
        this.executor.schedule(callable(), 10, TimeUnit.SECONDS);
    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     *
     * @throws InterruptedException
     */
    @Test(expected = IllegalStateException.class)
    public void invokeScheduleRunnableOnStoppedExecutor() throws InterruptedException {
        this.executor.stop(0, TimeUnit.SECONDS);
        this.executor.schedule(runnable(), 10, TimeUnit.SECONDS);
    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     *
     * @throws InterruptedException
     */
    @Test(expected = IllegalStateException.class)
    public void invokeScheduleAtFixedRateOnStoppedExecutor() throws InterruptedException {
        this.executor.stop(0, TimeUnit.SECONDS);
        this.executor.scheduleAtFixedRate(runnable(), 10, 10, TimeUnit.SECONDS);
    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     *
     * @throws InterruptedException
     */
    @Test(expected = IllegalStateException.class)
    public void invokeSubmitCallableOnStoppedExecutor() throws InterruptedException {
        this.executor.stop(0, TimeUnit.SECONDS);
        this.executor.submit(callable());
    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     *
     * @throws InterruptedException
     */
    @Test(expected = IllegalStateException.class)
    public void invokeSubmitRunnableOnStoppedExecutor() throws InterruptedException {
        this.executor.stop(0, TimeUnit.SECONDS);
        this.executor.submit(runnable());
    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     *
     * @throws InterruptedException
     */
    @Test(expected = IllegalStateException.class)
    public void invokeSubmitRunnableWithResultOnStoppedExecutor() throws InterruptedException {
        this.executor.stop(0, TimeUnit.SECONDS);
        this.executor.submit(runnable(), "result");
    }

    /**
     * Executor operations should not be permitted when
     * {@link RestartableScheduledExecutorService} has been stopped.
     *
     * @throws InterruptedException
     */
    @Test(expected = IllegalStateException.class)
    public void invokeScheduleWithFixedDelayStoppedExecutor() throws InterruptedException {
        this.executor.stop(0, TimeUnit.SECONDS);
        this.executor.scheduleWithFixedDelay(runnable(), 10, 10, TimeUnit.SECONDS);
    }

    private Callable<Void> callable() {
        return () -> null;
    }

    private static Runnable runnable() {
        return () -> {};
    }
}
