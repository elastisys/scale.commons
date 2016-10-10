package com.elastisys.scale.commons.util.concurrent;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Standard implementation of the {@link RestartableScheduledExecutorService}.
 *
 *
 *
 */
public class StandardRestartableScheduledExecutorService implements RestartableScheduledExecutorService {

    private final int corePoolSize;
    private final ThreadFactory threadFactory;

    /**
     * The currently running executor service or <code>null</code> in case we
     * are currently in the stopped state.
     */
    private ScheduledExecutorService executorService;

    /**
     * Constructs a new {@link StandardRestartableScheduledExecutorService} with
     * a fixed number of {@link Thread}s in the {@link Thread} pool.
     * <p/>
     * On return, the created {@link RestartableScheduledExecutorService} will
     * be in an unstarted state and needs to be explicitly started via a call to
     * {@link #start()}.
     *
     * @param corePoolSize
     *            the fixed number of threads to keep in the pool, even if they
     *            are idle.
     */
    public StandardRestartableScheduledExecutorService(int corePoolSize) {
        this(corePoolSize, Executors.defaultThreadFactory());
    }

    /**
     * Constructs a new {@link StandardRestartableScheduledExecutorService} with
     * a fixed number of {@link Thread}s in the {@link Thread} pool.
     * <p/>
     * On return, the created {@link RestartableScheduledExecutorService} will
     * be in an unstarted state and needs to be explicitly started via a call to
     * {@link #start()}.
     *
     * @param corePoolSize
     *            the fixed number of threads to keep in the pool, even if they
     *            are idle.
     * @param threadFactory
     *            the factory to use when the executor creates a new thread.
     */
    public StandardRestartableScheduledExecutorService(int corePoolSize, ThreadFactory threadFactory) {
        this.corePoolSize = corePoolSize;
        this.threadFactory = threadFactory;
    }

    @Override
    public void start() {
        if (isStarted()) {
            // idempotent operation
            return;
        }
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(this.corePoolSize,
                this.threadFactory);
        threadPoolExecutor.setRemoveOnCancelPolicy(true);
        this.executorService = threadPoolExecutor;

    }

    @Override
    public void stop(int taskTerminationGracePeriod, TimeUnit unit) throws InterruptedException {
        if (!isStarted()) {
            // idempotent operation
            return;
        }

        this.executorService.shutdown();
        boolean allDone = this.executorService.awaitTermination(taskTerminationGracePeriod, unit);
        if (!allDone) {
            this.executorService.shutdownNow();
        }
        this.executorService = null;
    }

    /**
     * Returns <code>true</code> if this
     * {@link StandardRestartableScheduledExecutorService} has been started,
     * <code>false</code> otherwise.
     *
     * @return
     */
    @Override
    public boolean isStarted() {
        return this.executorService != null;
    }

    /**
     * Returns the inner (delegate) {@link ExecutorService} used by this
     * {@link StandardRestartableScheduledExecutorService}.
     *
     * @return
     */
    ExecutorService innerExecutor() {
        return this.executorService;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        checkState(isStarted(), "executor is not started");
        return this.executorService.schedule(command, delay, unit);
    }

    @Override
    public void execute(Runnable command) {
        checkState(isStarted(), "executor is not started");
        this.executorService.execute(command);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        checkState(isStarted(), "executor is not started");
        return this.executorService.schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        checkState(isStarted(), "executor is not started");
        return this.executorService.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public void shutdown() {
        checkState(isStarted(), "executor is not started");
        this.executorService.shutdown();
        this.executorService = null;
    }

    @Override
    public List<Runnable> shutdownNow() {
        checkState(isStarted(), "executor is not started");
        List<Runnable> remainingTasks = this.executorService.shutdownNow();
        this.executorService = null;
        return remainingTasks;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        checkState(isStarted(), "executor is not started");
        return this.executorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public boolean isShutdown() {
        checkState(isStarted(), "executor is not started");
        return this.executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        checkState(isStarted(), "executor is not started");
        return this.executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        checkState(isStarted(), "executor is not started");
        return this.executorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        checkState(isStarted(), "executor is not started");
        return this.executorService.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        checkState(isStarted(), "executor is not started");
        return this.executorService.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        checkState(isStarted(), "executor is not started");
        return this.executorService.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        checkState(isStarted(), "executor is not started");
        return this.executorService.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        checkState(isStarted(), "executor is not started");
        return this.executorService.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        checkState(isStarted(), "executor is not started");
        return this.executorService.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        checkState(isStarted(), "executor is not started");
        return this.executorService.invokeAny(tasks, timeout, unit);
    }

}
