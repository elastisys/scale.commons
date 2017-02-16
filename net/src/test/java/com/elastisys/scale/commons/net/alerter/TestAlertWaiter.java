package com.elastisys.scale.commons.net.alerter;

import static com.elastisys.scale.commons.net.alerter.AlertSeverity.DEBUG;
import static com.elastisys.scale.commons.net.alerter.AlertSeverity.ERROR;
import static com.elastisys.scale.commons.net.alerter.AlertSeverity.WARN;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * Exercise {@link AlertWaiter}.
 */
public class TestAlertWaiter {

    private final static Alert debugAlert = AlertBuilder.create().topic("debug").severity(DEBUG).message("debug!")
            .build();
    private final static Alert warnAlert = AlertBuilder.create().topic("warn").severity(WARN).message("warn!").build();
    private final static Alert errorAlert = AlertBuilder.create().topic("error").severity(ERROR).message("error!")
            .build();

    private final static EventBus eventBus = new AsyncEventBus(Executors.newCachedThreadPool());

    /** Predicate used to determine what constitutes a satisfying alert. */
    private final Predicate<Alert> wantErrorAlert = input -> input.getSeverity() == ERROR;

    /**
     * Waiter should return immediately if client starts waiting when a
     * satisfying alert has already been received.
     */
    @Test
    public void immediatelyAvailable() throws Exception {
        AlertWaiter waiter = new AlertWaiter(eventBus, this.wantErrorAlert);

        eventBus.post(errorAlert);

        assertThat(waiter.await(), is(errorAlert));
    }

    /**
     * {@link AlertWaiter#await()} should be possible to call multiple times.
     */
    @Test
    public void awaitIsIdempotent() throws Exception {
        AlertWaiter waiter = new AlertWaiter(eventBus, this.wantErrorAlert);

        eventBus.post(errorAlert);

        assertThat(waiter.await(), is(errorAlert));
        assertThat(waiter.await(), is(errorAlert));
        assertThat(waiter.await(), is(errorAlert));
    }

    /**
     * Verify that the {@link AlertWaiter} actually waits until another thread
     * posts a satisfying alert on the {@link EventBus}.
     */
    @Test
    public void shouldWaitForResult() throws Exception {
        Predicate<Alert> alertPredicate = input -> input.getSeverity() == ERROR;
        AlertWaiter waiter = new AlertWaiter(eventBus, alertPredicate);

        // start poster in a separate thread, which doesn't immediately post
        Runnable poster = () -> {
            Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
            eventBus.post(errorAlert);
        };
        new Thread(poster).start();

        // verify that we wait for the result to appear on the bus
        Stopwatch timer = Stopwatch.createStarted();
        assertThat(waiter.await(), is(errorAlert));
        long waitTime = timer.elapsed(TimeUnit.MILLISECONDS);
        assertTrue("expected wait time to be at least 50ms, was: " + waitTime, waitTime >= 50L);
    }

    /**
     * Verify that the {@link AlertWaiter} filters out any {@link Alert}s that
     * don't match the {@link Predicate}.
     */
    @Test
    public void predicateFilter() throws Exception {
        Predicate<Alert> alertPredicate = input -> input.getSeverity() == ERROR;
        AlertWaiter waiter = new AlertWaiter(eventBus, alertPredicate);

        Runnable poster = () -> {
            Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
            eventBus.post(debugAlert);
            eventBus.post(warnAlert);
            eventBus.post(errorAlert);
        };
        new Thread(poster).start();

        // verify that debug and warn alerts were ignored
        assertThat(waiter.await(), is(errorAlert));
    }

}
