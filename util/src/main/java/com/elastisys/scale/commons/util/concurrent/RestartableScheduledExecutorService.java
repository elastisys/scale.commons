package com.elastisys.scale.commons.util.concurrent;

import java.util.concurrent.ScheduledExecutorService;

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
public interface RestartableScheduledExecutorService extends
		ScheduledExecutorService {

	/**
	 * (Re)starts this {@link RestartableScheduledExecutorService}.
	 */
	public void start();

	/**
	 * Stops this {@link RestartableScheduledExecutorService}.
	 * 
	 * @param taskTerminationGracePeriod
	 *            The grace period (in seconds) given to running tasks to
	 *            complete before they are slayed.
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public void stop(int taskTerminationGracePeriod)
			throws InterruptedException;

	/**
	 * Returns <code>true</code> if this
	 * {@link RestartableScheduledExecutorService} has been started,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if started, <code>false</code> otherwise.
	 */
	public boolean isStarted();
}
