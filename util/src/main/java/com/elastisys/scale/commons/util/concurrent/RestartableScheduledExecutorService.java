package com.elastisys.scale.commons.util.concurrent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ScheduledExecutorService} that can be re-started.
 * <p/>
 * Extends the {@link ScheduledExecutorService} with behavior for stopping and
 * restarting.
 *
 * @see ScheduledExecutorService
 *
 *
 */
public interface RestartableScheduledExecutorService extends ScheduledExecutorService {

    /**
     * (Re)starts this {@link RestartableScheduledExecutorService}.
     * <p/>
     * Note that on a restart, any tasks that were scheduled for execution prior
     * to stopping the service have been removed and need to be added again if
     * desired.
     */
    public void start();

    /**
     * Stops this {@link RestartableScheduledExecutorService}. Any running tasks
     * will be shut down.
     *
     * @param taskTerminationGracePeriod
     *            The grace period given to running tasks to complete before
     *            they are slayed.
     * @param unit
     *            The time unit of the {@code taskTerminationGracePeriod}.
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    public void stop(int taskTerminationGracePeriod, TimeUnit unit) throws InterruptedException;

    /**
     * Returns <code>true</code> if this
     * {@link RestartableScheduledExecutorService} has been started,
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if started, <code>false</code> otherwise.
     */
    public boolean isStarted();
}
