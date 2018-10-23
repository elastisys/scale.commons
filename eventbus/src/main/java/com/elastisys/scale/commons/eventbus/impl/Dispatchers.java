package com.elastisys.scale.commons.eventbus.impl;

import java.util.concurrent.Executor;

import org.slf4j.Logger;

import com.elastisys.scale.commons.eventbus.Subscriber;

/**
 * Allows different {@link Dispatcher}s to be instantiated.
 */
public class Dispatchers {

    /**
     * Creates a {@link Dispatcher} with asynchronous call semantics for
     * {@code post}. It will dispatch each event on a thread from a given
     * {@link Executor}.
     *
     * @param executor
     *            Executor used to dispatch calls to {@link Subscriber}s.
     * @return
     */
    public static Dispatcher asynchronous(Executor executor, Logger logger) {
        return new AsyncDispatcher(executor, logger);
    }

    /**
     * Creates a {@link Dispatcher} with synchronous call semantics for
     * {@code post} -- each call to a {@link Subscriber} method is made on the
     * calling thread.
     * 
     * @return
     */
    public static Dispatcher synchronous() {
        return SyncDispatcher.INSTANCE;
    }

    /**
     * A {@link Dispatcher} that uses an {@link Executor} to call the
     * {@link Subscriber} method in a non-blocking manner (on a thread different
     * from the thread calling {@code post}).
     */
    private static class AsyncDispatcher implements Dispatcher {

        /** Executor used to dispatch calls to {@link Subscriber}s. */
        private final Executor executor;
        private final Logger logger;

        private AsyncDispatcher(Executor executor, Logger logger) {
            this.executor = executor;
            this.logger = logger;
        }

        @Override
        public void dispatch(SubscriberMethod subscriber, Object event) {
            this.executor.execute(() -> {
                try {
                    subscriber.call(event);
                } catch (Exception e) {
                    this.logger.warn(String.format("event dispatch failed: %s", e.getMessage()), e);
                }
            });
        }
    }

    /**
     * A {@link Dispatcher} that calls the {@link Subscriber} in a blocking
     * manner on the same thread that called {@code post}.
     */
    private static class SyncDispatcher implements Dispatcher {
        private static final SyncDispatcher INSTANCE = new SyncDispatcher();

        private SyncDispatcher() {
        }

        @Override
        public void dispatch(SubscriberMethod subscriber, Object event) {
            subscriber.call(event);
        }
    }
}
