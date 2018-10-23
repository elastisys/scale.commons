package com.elastisys.scale.commons.eventbus.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.eventbus.Subscriber;

/**
 * Exercise {@link AsynchronousEventBus}.
 */
public class TestAsynchronousEventBus {
    static Logger LOG = LoggerFactory.getLogger(TestSynchronousEventBus.class);

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * The {@link AsynchronousEventBus} should deliver events on a thread
     * different from the one that was used to call {@code post}.
     */
    @Test
    public void shouldDeliverOnDifferentThread() throws InterruptedException {
        BaseEventBus eventBus = new AsynchronousEventBus(this.executor, LOG);

        final AtomicReference<Thread> calledByThread = new AtomicReference<Thread>();
        Object subscriber = new Object() {
            @Subscriber
            public void onEvent(Object event) {
                LOG.debug("wohoo");
                calledByThread.set(Thread.currentThread());
            }
        };

        eventBus.register(subscriber);
        eventBus.post("event");

        this.executor.shutdown();
        this.executor.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(calledByThread.get(), is(not(Thread.currentThread())));
    }
}
