package com.elastisys.scale.commons.eventbus.impl;

/**
 * A {@link Dispatcher} delivers events to a {@link SubscriberMethod}. It is
 * mainly a construct to allow the {@link BaseEventBus} to be equipped with
 * different delivery semantics. A {@link Dispatcher} can, for instance,
 * implement asynchronous {@code post()} behavior by performing each delivery on
 * a separate thread or
 *
 */
public interface Dispatcher {
    /**
     * Delivers an event to a given {@link SubscriberMethod}.
     *
     * @param subscriber
     * @param event
     */
    void dispatch(SubscriberMethod subscriber, Object event);
}
