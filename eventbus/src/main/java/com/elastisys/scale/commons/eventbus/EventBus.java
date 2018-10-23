package com.elastisys.scale.commons.eventbus;

/**
 * A dispatcher of events to listeners that offers an API similar to Guava's
 * event bus. It allows event objects to be posted and dispatched to listeners,
 * and provides ways for listeners to register themselves.
 * <p>
 * The EventBus allows publish-subscribe-style communication between components
 * without requiring the components to explicitly register with one another (and
 * thus be aware of each other). It is designed exclusively to replace
 * traditional Java in-process event distribution using explicit registration.
 * It is <em>not</em> a general-purpose publish-subscribe system, nor is it
 * intended for interprocess communication.
 *
 * <h2>Receiving Events</h2>
 *
 * <p>
 * To receive events, an object should:
 *
 * <ol>
 * <li>Expose a public method, known as the <i>event subscriber</i>, which
 * accepts a single argument of the type of event desired;
 * <li>Mark it with a {@link Subscriber} annotation;
 * <li>Pass itself to an EventBus instance's {@link #register(Object)} method.
 * </ol>
 *
 * <h2>Posting Events</h2>
 *
 * <p>
 * To post an event, simply provide the event object to the
 * {@link #post(Object)} method. The EventBus instance will determine the type
 * of event and route it to all registered listeners.
 *
 * <p>
 * Events are routed based on their type &mdash; an event will be delivered to
 * any subscriber for any type to which the event is <em>assignable.</em> This
 * includes implemented interfaces, all superclasses, and all interfaces
 * implemented by superclasses.
 *
 * <p>
 * When {@code post} is called, all registered subscribers for an event are run
 * in sequence, so subscribers should be reasonably quick. If an event may
 * trigger an extended process (such as a database load), spawn a thread or
 * queue it for later.
 *
 * <h2>Subscriber Methods</h2>
 *
 * <p>
 * Event subscriber methods must accept only one argument: the event.
 *
 * <p>
 * Subscribers should not, in general, throw. If they do, the EventBus will
 * catch and log the exception. This is rarely the right solution for error
 * handling and should not be relied upon; it is intended solely to help find
 * problems during development.
 *
 * <p>
 * The EventBus guarantees that it will not call a subscriber method from
 * multiple threads simultaneously, unless the method explicitly allows it by
 * bearing the {@link AllowConcurrentEvents} annotation. If this annotation is
 * not present, subscriber methods need not worry about being reentrant, unless
 * also called from outside the EventBus.
 *
 * <p>
 * This class is safe for concurrent use.
 *
 */
public interface EventBus {

    /**
     * Registers all {@link Subscriber} methods on object to receive events.
     *
     * @param object
     *            object whose {@link Subscriber} methods should be registered.
     * @throws IllegalArgumentException
     */
    void register(Object object) throws IllegalArgumentException;

    /**
     * Unregisters all {@link Subscriber} methods on a registered object.
     *
     * @param object
     *            object whose {@link Subscriber} methods should be
     *            unregistered.
     *
     * @throws IllegalArgumentException
     *             if the object was not previously registered.
     */
    void unregister(Object object) throws IllegalArgumentException;

    /**
     * Posts an event to all registered {@link Subscriber}s. This method will
     * return successfully after the event has been dispatched to all
     * subscribers, and regardless of any exceptions thrown by subscribers.
     *
     * @param event
     *            event to post.
     */
    void post(Object event);
}
