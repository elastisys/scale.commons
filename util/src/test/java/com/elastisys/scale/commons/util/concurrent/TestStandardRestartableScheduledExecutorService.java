package com.elastisys.scale.commons.util.concurrent;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutorService;

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
		this.executor.stop(0);
		assertThat(this.executor.isStarted(), is(false));
		assertThat(this.executor.innerExecutor(), is(nullValue()));
	}

	@Test
	public void stopWhenStopped() throws InterruptedException {
		assertThat(this.executor.isStarted(), is(false));
		this.executor.stop(0);
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
		this.executor.stop(0);
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
}
