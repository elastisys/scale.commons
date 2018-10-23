package com.elastisys.scale.commons.eventbus.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;

import com.elastisys.scale.commons.eventbus.EventBus;
import com.elastisys.scale.commons.eventbus.Subscriber;

/**
 * A configurable implementation of {@link EventBus} which supports different
 * call semantics for the {@link #post} method by delegating the
 * {@link Subscriber} method calling to a {@link Dispatcher}. The
 * {@link Dispatcher}, in turn, is free to call subscribers on different threads
 * to implement asynchronous delivery behavior.
 */
class BaseEventBus implements EventBus {
    /** The logger to use. */
    private final Logger logger;
    /**
     * Used to dispatch calls to {@link Subscriber}s. Allows different
     * subscriber call semantics to be implemented (asynchronous vs synchronous
     * delivery).
     */
    private final Dispatcher dispatcher;
    /**
     * Tracks the subscriber objects that have been registered and maps each
     * subscriber object to a set of {@link SubscriberMethod}s -- one for each
     * {@link Subscriber}-annotated method.
     */
    final Map<Object, CopyOnWriteArraySet<SubscriberMethod>> objectSubscriberMethods;
    /**
     * Tracks the registered {@link Subscriber} methods keyed by the type of
     * their event parameter. This allows an incoming post to easily be keyed to
     * its registered subscriber methods.
     */
    final Map<Class<?>, CopyOnWriteArraySet<SubscriberMethod>> eventTypeToSubscriberMethods;

    /**
     * Creates a {@link BaseEventBus}.
     *
     * @param dispatcher
     *            Used to dispatch calls to {@link Subscriber}s. Allows
     *            different subscriber call semantics to be implemented
     *            (asynchronous vs synchronous delivery).
     * @param logger
     *            The {@link Logger} to use.
     */
    public BaseEventBus(Dispatcher dispatcher, Logger logger) {
        this.dispatcher = dispatcher;
        this.logger = logger;
        this.objectSubscriberMethods = new ConcurrentHashMap<>();
        this.eventTypeToSubscriberMethods = new ConcurrentHashMap<>();
    }

    @Override
    public void register(Object object) throws IllegalArgumentException {
        Collection<Method> subscriberMethods = findSubscriberMethods(object);
        if (subscriberMethods.isEmpty()) {
            throw new NoSubscriberMethodsException(String.format("object does not contain any %s-annotated methods",
                    Subscriber.class.getSimpleName()));
        }

        this.objectSubscriberMethods.putIfAbsent(object, new CopyOnWriteArraySet<>());
        for (Method method : subscriberMethods) {
            SubscriberMethod subscriber = new SubscriberMethod(object, method);

            this.objectSubscriberMethods.get(object).add(subscriber);
            Class<?> paramType = subscriber.getParameterType();
            this.eventTypeToSubscriberMethods.putIfAbsent(paramType, new CopyOnWriteArraySet<>());
            this.eventTypeToSubscriberMethods.get(paramType).add(subscriber);
        }
    }

    @Override
    public void unregister(Object object) throws IllegalArgumentException {
        Set<SubscriberMethod> subscriberMethods = this.objectSubscriberMethods.remove(object);
        if (subscriberMethods == null) {
            throw new ObjectNotRegisteredException("object was not registered as a subscriber");
        }

        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            Class<?> eventType = subscriberMethod.getParameterType();
            this.eventTypeToSubscriberMethods.get(eventType).remove(subscriberMethod);
            // last subscriber method for this type -> remove entry altogether
            if (this.eventTypeToSubscriberMethods.get(eventType).isEmpty()) {
                this.eventTypeToSubscriberMethods.remove(eventType);
            }
        }
    }

    @Override
    public void post(Object event) {
        // Get all super-types and super-interfaces of the event. The event
        // shall be dispatched to any subscriber methods whose parameter is
        // assignable from the event class.
        List<Class<?>> eventAssignableTypes = classesAndInterfacesAssignableFrom(event.getClass());

        for (Class<?> eventType : eventAssignableTypes) {
            if (this.eventTypeToSubscriberMethods.containsKey(eventType)) {
                Set<SubscriberMethod> subscribers = this.eventTypeToSubscriberMethods.get(eventType);
                if (this.logger.isTraceEnabled()) {
                    this.logger.trace("delivering event to {} subscriber(s) ...", subscribers.size());
                }
                for (SubscriberMethod subscriber : subscribers) {
                    this.dispatcher.dispatch(subscriber, event);
                }
            }
        }
    }

    static List<Class<?>> classesAndInterfacesAssignableFrom(Class<?> eventType) {
        List<Class<?>> superTypes = new ArrayList<>();
        superTypes.add(eventType);
        superTypes.addAll(ClassUtils.getAllSuperclasses(eventType));
        superTypes.addAll(ClassUtils.getAllInterfaces(eventType));
        return superTypes;
    }

    static Collection<Method> findSubscriberMethods(Object object) {
        List<Method> subscriberMethods = new ArrayList<>();
        // all public methods of the object class (or its superclasses)
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Subscriber.class)) {
                ensureSingleParameterMethod(method);
                subscriberMethods.add(method);
            }
        }

        return subscriberMethods;
    }

    /**
     * Ensures that the given method takes a single parameter or throws an
     * {@link IllegalArgumentException}.
     *
     * @param method
     * @throws IllegalArgumentException
     */
    static void ensureSingleParameterMethod(Method method) throws IllegalArgumentException {
        int parameterCount = method.getParameterCount();
        if (parameterCount != 1) {
            throw new InvalidSubscriberMethodParameterCountException(String.format(
                    "the method %s takes %d parameters, a %s method is only supposed to take 1", method.getName(),
                    parameterCount, com.elastisys.scale.commons.eventbus.Subscriber.class.getSimpleName()));
        }
    }
}
