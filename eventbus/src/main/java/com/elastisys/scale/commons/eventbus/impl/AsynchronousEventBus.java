package com.elastisys.scale.commons.eventbus.impl;

import java.util.concurrent.Executor;

import org.slf4j.Logger;

import com.elastisys.scale.commons.eventbus.EventBus;
import com.elastisys.scale.commons.eventbus.Subscriber;

/**
 * An asynchronous {@link EventBus} implementation with non-blocking semantics
 * for the {@code post} method. It will use a provided {@link Executor} to
 * deliver events to {@link Subscriber}s on threads different from the thread
 * calling {@code post}.
 */
public class AsynchronousEventBus extends BaseEventBus {
    public AsynchronousEventBus(Executor executor, Logger logger) {
        super(Dispatchers.asynchronous(executor, logger), logger);
    }
}
