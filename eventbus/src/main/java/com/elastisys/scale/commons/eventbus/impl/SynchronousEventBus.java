package com.elastisys.scale.commons.eventbus.impl;

import org.slf4j.Logger;

import com.elastisys.scale.commons.eventbus.EventBus;
import com.elastisys.scale.commons.eventbus.Subscriber;

/**
 * A synchronous {@link EventBus} with blocking call semantics for {@code post}.
 * Each {@link Subscriber} method will be called, in sequence, on the thread
 * that called {@code post}.
 *
 */
public class SynchronousEventBus extends BaseEventBus {
    public SynchronousEventBus(Logger logger) {
        super(Dispatchers.synchronous(), logger);
    }
}
